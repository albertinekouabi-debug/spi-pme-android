package com.spipme.app.core.sync.android

import com.spipme.app.BuildConfig
import com.spipme.app.core.network.ClientAuthentifie
import com.spipme.app.core.sync.OperationEnAttente
import com.spipme.app.core.sync.PasserelleDistante
import com.spipme.app.core.sync.ResultatEnvoi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.coroutineContext

/**
 * Rejoue une opération via le client HTTP AUTHENTIFIÉ du projet (AuthInterceptor + TokenAuthenticator :
 * le rafraîchissement du jeton est donc déjà géré ; un 401 qui subsiste = session réellement expirée).
 *
 * En-têtes posés à chaque tentative, identiques d'une tentative à l'autre :
 *  - Idempotency-Key : la clé stockée dans l'opération (jamais régénérée) ;
 *  - X-Request-ID    : même valeur, pour relier journal d'audit serveur et opération locale ;
 *  - If-Match        : version lue par le client, pour les modifications.
 */
@Singleton
class PasserelleOkHttp @Inject constructor(
    @ClientAuthentifie private val client: OkHttpClient,
    private val json: Json,
) : PasserelleDistante {

    override suspend fun envoyer(operation: OperationEnAttente): ResultatEnvoi = withContext(Dispatchers.IO) {
        coroutineContext.ensureActive()
        val url = BuildConfig.API_BASE_URL.trimEnd('/') + "/" + operation.chemin.trimStart('/')
        val corps = (operation.payloadJson ?: "{}").toRequestBody("application/json".toMediaType())
        val requete = Request.Builder()
            .url(url)
            .method(operation.methode, corps)
            .header("Idempotency-Key", operation.idempotencyKey)
            .header("X-Request-ID", operation.idempotencyKey)
            .apply { operation.versionBase?.let { header("If-Match", "\"$it\"") } }
            .build()

        try {
            client.newCall(requete).execute().use { reponse ->
                val texte = reponse.body?.string().orEmpty()
                classer(reponse.code, texte)
            }
        } catch (e: IOException) {
            ResultatEnvoi.Reseau(e.message ?: "Réseau indisponible")
        }
    }

    internal fun classer(code: Int, corps: String): ResultatEnvoi = when {
        code in 200..299 -> ResultatEnvoi.Succes(idServeur = extraireId(corps))
        code == 401 -> ResultatEnvoi.AuthExpiree
        code == 412 -> extraireConflit(corps)
        code == 408 || code == 429 || code >= 500 -> ResultatEnvoi.ServeurTemporaire(code, message(corps, code))
        else -> ResultatEnvoi.Definitive(code, message(corps, code))
    }

    private fun racine(corps: String): JsonObject? =
        runCatching { json.parseToJsonElement(corps).jsonObject }.getOrNull()

    private fun extraireId(corps: String): String? =
        racine(corps)?.get("id")?.let { runCatching { it.jsonPrimitive.content }.getOrNull() }

    /** `extra` du serveur (version_serveur + donnees_serveur) conservé tel quel pour la résolution. */
    private fun extraireConflit(corps: String): ResultatEnvoi.Conflit {
        val extra = racine(corps)?.get("extra")?.let { runCatching { it.jsonObject }.getOrNull() }
        return ResultatEnvoi.Conflit(
            versionServeur = extra?.get("version_serveur")?.jsonPrimitive?.intOrNull,
            donneesServeurJson = extra?.toString(),
        )
    }

    private fun message(corps: String, code: Int): String =
        racine(corps)?.get("message")?.let { runCatching { it.jsonPrimitive.content }.getOrNull() }
            ?: "Erreur $code"
}
