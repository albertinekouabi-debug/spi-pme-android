package com.spipme.app.core.util

sealed class Resultat<out T> {
    data class Succes<out T>(val donnees: T) : Resultat<T>()
    data class Echec(val message: String, val champsInvalides: Map<String, List<String>>? = null) : Resultat<Nothing>()
}

inline fun <T> Resultat<T>.surSucces(action: (T) -> Unit): Resultat<T> {
    if (this is Resultat.Succes) action(donnees)
    return this
}

inline fun <T> Resultat<T>.surEchec(action: (String) -> Unit): Resultat<T> {
    if (this is Resultat.Echec) action(message)
    return this
}
