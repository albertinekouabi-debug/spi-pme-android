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
}
