package me.restarhalf.deer.ui.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.restarhalf.deer.data.AppHttp
import okhttp3.Request

@Serializable
data class GitHubRelease(
    @SerialName("tag_name") val tagName: String?,
    @SerialName("name") val releaseName: String?
)

object UpdateChecker {
    private val json = AppHttp.json

    suspend fun fetchLatestVersion(owner: String, repo: String): String? =
        withContext(Dispatchers.IO) {
            try {
                AppHttp.client.newCall(
                    Request.Builder()
                        .url("https://api.github.com/repos/$owner/$repo/releases/latest")
                        .header("User-Agent", "DeerTimer/1.0")
                        .build()
                ).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext null
                    val body = resp.body.string().orEmpty()
                    if (body.isBlank()) return@withContext null
                    val parsed = runCatching { json.decodeFromString<GitHubRelease>(body) }
                        .getOrNull()
                        ?: return@withContext null
                    parsed.tagName ?: parsed.releaseName
                }
            } catch (e: Exception) {
                null
            }
        }
}
