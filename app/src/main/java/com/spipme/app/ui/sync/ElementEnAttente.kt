package com.spipme.app.ui.sync

import com.spipme.app.core.sync.LibelleOperation
import com.spipme.app.core.sync.OperationEnAttente

/** Saisie hors ligne pas encore synchronisée, affichée dans les listes avec la mention « en attente ». */
data class ElementEnAttente(val id: Long, val titre: String, val detail: String)

fun OperationEnAttente.versElementEnAttente() =
    ElementEnAttente(id, LibelleOperation.titre(type), LibelleOperation.detail(type, payloadJson))
