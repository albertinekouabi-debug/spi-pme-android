package com.spipme.app.domain.repository

import com.spipme.app.core.util.Resultat
import com.spipme.app.domain.model.Secteur

interface SecteurRepository {
    suspend fun mesSecteurs(): Resultat<List<Secteur>>
}

