package com.example.ironquest

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

data class ReleaseUpdate(
    val version: String,
    val title: String,
    val url: String,
    val apkUrl: String?
)

object ReleaseUpdateChecker {
    private const val RELEASE_API = "https://api.github.com/repos/DJSVET/IronQuest/releases/latest"

    suspend fun check(currentVersion: String): ReleaseUpdate? = withContext(Dispatchers.IO) {
        val connection = (URL(RELEASE_API).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 8000
            readTimeout = 8000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("User-Agent", "IronQuest-Android")
        }
        try {
            if (connection.responseCode !in 200..299) return@withContext null
            val json = JSONObject(connection.inputStream.bufferedReader().use { it.readText() })
            if (json.optBoolean("draft", false) || json.optBoolean("prerelease", false)) return@withContext null
            val tag = json.optString("tag_name", "").removePrefix("v").removePrefix("V")
            val installed = currentVersion.removePrefix("v").removePrefix("V")
            if (tag.isBlank() || compareVersions(tag, installed) <= 0) return@withContext null
            val assets = json.optJSONArray("assets")
            var apkUrl: String? = null
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.optJSONObject(i) ?: continue
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        apkUrl = asset.optString("browser_download_url").takeIf { it.isNotBlank() }
                        break
                    }
                }
            }
            ReleaseUpdate(
                version = tag,
                title = json.optString("name", "IronQuest $tag"),
                url = json.optString("html_url", "https://github.com/DJSVET/IronQuest/releases"),
                apkUrl = apkUrl
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun compareVersions(first: String, second: String): Int {
        val a = first.split(".", "-", "+").map { it.toIntOrNull() ?: 0 }
        val b = second.split(".", "-", "+").map { it.toIntOrNull() ?: 0 }
        for (i in 0 until maxOf(a.size, b.size)) {
            val left = a.getOrElse(i) { 0 }
            val right = b.getOrElse(i) { 0 }
            if (left != right) return left.compareTo(right)
        }
        return 0
    }
}
