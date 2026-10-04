package com.spipme.app.core.util

sealed class Resultat<out T> {
    data class Succes<out T>(val donnees: T) : Resultat<T>()
    data class Echec(
        val message: String,
        val champsInvalides: Map<String, List<String>>? = null,
        /** Vrai si l'échec vient de l'absence de réseau (IOException) : seul cas où un cache local peut répondre. */
        val reseau: Boolean = false,
        /**
         * Vrai si la saisie n'a pas pu partir mais a été CONSERVÉE en file d'attente (hors ligne) : l'UI doit
         * l'annoncer comme « enregistré, synchronisation en attente », pas comme une erreur.
         */
        val enFile: Boolean = false,
    ) : Resultat<Nothing>()
}

inline fun <T> Resultat<T>.surSucces(action: (T) -> Unit): Resultat<T> {
    if (this is Resultat.Succes) action(donnees)
    return this
}

inline fun <T> Resultat<T>.surEchec(action: (String) -> Unit): Resultat<T> {
    if (this is Resultat.Echec) action(message)
    return this
}
