package com.spipme.app.core.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HachageMotDePasseTest {

    @Test
    fun `un mot de passe correct est verifie avec succes`() {
        val motDePasse = "MotDePasse#2026".toCharArray()
        val hash = HachageMotDePasse.hacher(motDePasse)

        assertTrue(HachageMotDePasse.verifier("MotDePasse#2026".toCharArray(), hash))
    }

    @Test
    fun `un mot de passe incorrect echoue la verification`() {
        val hash = HachageMotDePasse.hacher("MotDePasse#2026".toCharArray())

        assertFalse(HachageMotDePasse.verifier("MauvaisMotDePasse".toCharArray(), hash))
    }

    @Test
    fun `deux hachages du meme mot de passe sont differents (sel aleatoire)`() {
        val motDePasse = "MotDePasse#2026".toCharArray()
        val hash1 = HachageMotDePasse.hacher(motDePasse)
        val hash2 = HachageMotDePasse.hacher(motDePasse)

        assertNotEquals(hash1, hash2) // sels différents -> hachages différents
        assertTrue(HachageMotDePasse.verifier(motDePasse, hash1))
        assertTrue(HachageMotDePasse.verifier(motDePasse, hash2))
    }

    @Test
    fun `un hachage malforme est rejete sans exception`() {
        assertFalse(HachageMotDePasse.verifier("peu importe".toCharArray(), "hash_invalide_sans_separateur"))
    }

    @Test
    fun `un hachage a separateurs mais base64 invalide est rejete sans exception`() {
        assertFalse(HachageMotDePasse.verifier("x".toCharArray(), "@@@:###"))
        assertFalse(HachageMotDePasse.verifier("x".toCharArray(), "pbkdf2-sha256\$600000\$@@@\$###"))
    }

    @Test
    fun `un nombre d'iterations hors bornes est rejete`() {
        assertFalse(HachageMotDePasse.verifier("x".toCharArray(), "pbkdf2-sha256\$1\$QUJD\$QUJD"))
        assertFalse(HachageMotDePasse.verifier("x".toCharArray(), "pbkdf2-sha256\$999999999\$QUJD\$QUJD"))
    }

    @Test
    fun `un hachage vide ou tronque est rejete`() {
        assertFalse(HachageMotDePasse.verifier("x".toCharArray(), ""))
        assertFalse(HachageMotDePasse.verifier("x".toCharArray(), "pbkdf2-sha256\$600000\$QUJD"))
    }

    @Test
    fun `un nouveau hachage utilise le format versionne et n'est pas obsolete`() {
        val hash = HachageMotDePasse.hacher("MotDePasse#2026".toCharArray())
        assertTrue(hash.startsWith("pbkdf2-sha256\$600000\$"))
        assertFalse(HachageMotDePasse.estObsolete(hash))
    }

    @Test
    fun `l'ancien format sel-deux-points reste verifiable et est signale obsolete`() {
        // Hash produit par l'ancienne implémentation : PBKDF2-SHA256, 120 000 itérations.
        val sel = ByteArray(16) { it.toByte() }
        val spec = javax.crypto.spec.PBEKeySpec("MotDePasse#2026".toCharArray(), sel, 120_000, 256)
        val cle = javax.crypto.SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        val b64 = java.util.Base64.getEncoder()
        val ancien = "${b64.encodeToString(sel)}:${b64.encodeToString(cle)}"

        assertTrue(HachageMotDePasse.verifier("MotDePasse#2026".toCharArray(), ancien))
        assertFalse(HachageMotDePasse.verifier("Autre".toCharArray(), ancien))
        assertTrue(HachageMotDePasse.estObsolete(ancien))
    }
}
