package com.spipme.app.core.security

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Utilisé UNIQUEMENT pour permettre la connexion hors ligne (comparer contre
 * un hash stocké localement, cf. maquette "Connexion hors ligne") — jamais
 * envoyé au serveur, qui a sa propre vérification (Django, hashers standards).
 * PBKDF2-HMAC-SHA256, 120 000 itérations (recommandation OWASP 2023+ pour PBKDF2-SHA256).
 */
object HachageMotDePasse {
    private const val ITERATIONS = 120_000
    private const val LONGUEUR_CLE = 256

    fun hacher(motDePasse: CharArray): String {
        val sel = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = deriver(motDePasse, sel)
        return "${Base64.getEncoder().encodeToString(sel)}:${Base64.getEncoder().encodeToString(hash)}"
    }

    fun verifier(motDePasse: CharArray, hachageStocke: String): Boolean {
        val parties = hachageStocke.split(":")
        if (parties.size != 2) return false
        val sel = Base64.getDecoder().decode(parties[0])
        val hashAttendu = Base64.getDecoder().decode(parties[1])
        val hashCalcule = deriver(motDePasse, sel)
        return hashCalcule.contentEquals(hashAttendu)
    }

    private fun deriver(motDePasse: CharArray, sel: ByteArray): ByteArray {
        val spec = PBEKeySpec(motDePasse, sel, ITERATIONS, LONGUEUR_CLE)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        return factory.generateSecret(spec).encoded
    }
}
