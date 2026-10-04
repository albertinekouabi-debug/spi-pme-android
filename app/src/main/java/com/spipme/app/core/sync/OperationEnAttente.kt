package com.spipme.app.core.sync

/**
 * Cycle de vie d'une opération locale en attente de synchronisation.
 *
 *   EN_ATTENTE ──envoi──▶ EN_COURS ──succès──▶ (supprimée)
 *        ▲                    │
 *        └──échec transitoire─┘   (nouvelle tentative après un délai croissant)
 *
 *   CONFLIT          : le serveur a évolué depuis la lecture (412) — attend une décision de l'utilisateur.
 *   ECHEC_DEFINITIF  : refus métier/validation (4xx) ou trop de tentatives — attend une action de l'utilisateur.
 */
enum class StatutOperation { EN_ATTENTE, EN_COURS, CONFLIT, ECHEC_DEFINITIF }

/** Préfixe des identifiants temporaires d'entités créées hors ligne (`@local:<uuid>`). */
const val PREFIXE_ID_LOCAL = "@local:"

/**
 * Une écriture métier différée. Elle décrit la requête HTTP à rejouer : le moteur reste générique et
 * ne connaît aucun endpoint. Les corrections financières sont TOUJOURS des opérations « append-only »
 * (annulation, avoir, contre-écriture) : jamais un PATCH local susceptible d'écraser l'historique.
 *
 * @property idempotencyKey créée UNE fois à la saisie et conservée à l'identique à chaque tentative :
 *   c'est ce qui permet au serveur de dédupliquer un rejeu dont la réponse s'est perdue.
 * @property entityId id serveur (texte) ou `@local:<uuid>` tant que l'entité n'existe que sur l'appareil.
 * @property refLocaleCreee pour une création : la référence `@local:` à remplacer par l'id serveur reçu.
 * @property versionBase version lue par le client (modification) → envoyée en `If-Match`.
 */
data class OperationEnAttente(
    val id: Long = 0,
    val idempotencyKey: String,
    val type: String,
    val entityType: String,
    val entityId: String,
    val methode: String,
    val chemin: String,
    val payloadJson: String? = null,
    val versionBase: Int? = null,
    val refLocaleCreee: String? = null,
    val creeLe: Long,
    val tentatives: Int = 0,
    val derniereTentativeLe: Long? = null,
    val prochaineTentativeLe: Long = 0,
    val statut: StatutOperation = StatutOperation.EN_ATTENTE,
    val messageErreur: String? = null,
    val conflitJson: String? = null,
) {
    /** Deux opérations d'une même entité sont rejouées dans l'ordre : une en échec bloque les suivantes. */
    val cleEntite: String get() = "$entityType:$entityId"
}
