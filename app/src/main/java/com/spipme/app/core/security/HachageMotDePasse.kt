package com.spipme.app.core.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Utilisé UNIQUEMENT pour permettre la connexion hors ligne (comparer contre
 * un hash stocké localement, cf. maquette "Connexion hors ligne") — jamais
 * envoyé au serveur, qui a sa propre vérification (Django, hashers standards).
 *
 * PBKDF2-HMAC-SHA256, 600 000 itérations (recommandation OWASP Password
 * Storage Cheat Sheet pour cet algorithme).
 *
 * Format stocké : `pbkdf2-sha256$<itérations>$<sel base64>$<hash base64>`.
 * Le nombre d'itérations est porté par le hash lui-même : il pourra encore
 * évoluer sans invalider les comptes existants.
 *
 * Ancien format (`<sel>:<hash>`, 120 000 itérations) : toujours vérifiable.
 * [estObsolete] permet de le remplacer ; c'est fait à chaque connexion en
 * ligne réussie, car AuthRepositoryImpl re-hache alors le mot de passe saisi.
 */
object HachageMotDePasse {
    private const val ITERATIONS = 600_000
    private const val ITERATIONS_ANCIEN_FORMAT = 120_000
    private const val ITERATIONS_MIN = 10_000       // borne basse/haute : un hash local altéré
    private const val ITERATIONS_MAX = 5_000_000    // ne doit pas pouvoir figer l'application
    private const val LONGUEUR_CLE = 256
    private const val PREFIXE = "pbkdf2-sha256"
    private const val SEPARATEUR = "$"

    fun hacher(motDePasse: CharArray): String {
        val sel = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = deriver(motDePasse, sel, ITERATIONS)
        val b64 = Base64.getEncoder()
        return listOf(PREFIXE, ITERATIONS.toString(), b64.encodeToString(sel), b64.encodeToString(hash))
            .joinToString(SEPARATEUR)
    }

    /** Ne lève jamais d'exception : un hash absent, tronqué ou altéré donne simplement `false`. */
    fun verifier(motDePasse: CharArray, hachageStocke: String): Boolean {
        val decode = decoder(hachageStocke) ?: return false
        return try {
            val hashCalcule = deriver(motDePasse, decode.sel, decode.iterations)
            // Comparaison à temps constant (pas de fuite par le temps de réponse).
            MessageDigest.isEqual(hashCalcule, decode.hash)
        } catch (e: java.security.GeneralSecurityException) {
            false
        }
    }

    /** Vrai si ce hash n'a pas le format ou le coût actuels et doit être recalculé. */
    fun estObsolete(hachageStocke: String): Boolean {
        val decode = decoder(hachageStocke) ?: return true
        return decode.iterations < ITERATIONS || !hachageStocke.startsWith(PREFIXE + SEPARATEUR)
    }

    private class Decode(val iterations: Int, val sel: ByteArray, val hash: ByteArray)

    private fun decoder(hachageStocke: String): Decode? = try {
        val b64 = Base64.getDecoder()
        if (hachageStocke.startsWith(PREFIXE + SEPARATEUR)) {
            val parties = hachageStocke.split(SEPARATEUR)
            val iterations = parties.getOrNull(1)?.toIntOrNull()
            if (parties.size != 4 || iterations == null || iterations !in ITERATIONS_MIN..ITERATIONS_MAX) {
                null
            } else {
                Decode(iterations, b64.decode(parties[2]), b64.decode(parties[3]))
            }
        } else {
            val parties = hachageStocke.split(":")
            if (parties.size != 2) null
            else Decode(ITERATIONS_ANCIEN_FORMAT, b64.decode(parties[0]), b64.decode(parties[1]))
        }
    } catch (e: IllegalArgumentException) {
        null // Base64 invalide
    }

    private fun deriver(motDePasse: CharArray, sel: ByteArray, iterations: Int): ByteArray {
        val spec = PBEKeySpec(motDePasse, sel, iterations, LONGUEUR_CLE)
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }
}
