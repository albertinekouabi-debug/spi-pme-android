package com.spipme.app.core.sync

/**
 * Délai avant la n-ième nouvelle tentative : base × 2^(n-1), plafonné, plus une gigue pour éviter que
 * tous les appareils ne réessaient au même instant après une panne serveur.
 */
class BackoffPolicy(
    private val baseMs: Long = 30_000,
    private val plafondMs: Long = 3_600_000,
    private val gigueMs: (Long) -> Long = { 0L },
) {
    fun delaiMs(tentative: Int): Long {
        require(tentative >= 1) { "tentative >= 1" }
        val exposant = (tentative - 1).coerceAtMost(30)  // évite tout dépassement de Long
        val brut = baseMs * (1L shl exposant)
        val plafonne = if (brut <= 0 || brut > plafondMs) plafondMs else brut
        return plafonne + gigueMs(plafonne).coerceAtLeast(0)
    }
}
