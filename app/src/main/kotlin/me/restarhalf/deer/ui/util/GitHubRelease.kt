package me.restarhalf.deer.ui.util

import com.squareup.moshi.Json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.restarhalf.deer.data.AppHttp
import okhttp3.Request

data class GitHubRelease(
    @param:Json(name = "tag_name") val tagName: String?,
    @param:Json(name = "name") val releaseName: String?
)

object UpdateChecker {
    private val adapter = AppHttp.moshi.adapter(GitHubRelease::class.java)

    suspend fun fetchLatestVersion(owner: String, repo: String): String? =
        withContext(Dispatchers.IO) {
            try {
                AppHttp.client.newCall(
                    Request.Builder()
                        .url("https://api.github.com/repos/$owner/$repo/releases/latest")
                        .header("User-Agent", "MyApp/1.0")
                        .build()
                ).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext null
                    val body = resp.body.string()
                    val parsed = adapter.fromJson(body) ?: return@withContext null
                    parsed.tagName ?: parsed.releaseName
                }
            } catch (e: Exception) {
                null
            }
        }
}
