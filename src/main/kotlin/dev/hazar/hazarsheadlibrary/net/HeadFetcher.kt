package dev.hazar.hazarsheadlibrary.net

import dev.hazar.hazarsheadlibrary.data.HeadData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.util.logging.Logger

object HeadFetcher {
    private val LOGGER = Logger.getLogger("GithubFetcher")
    private const val PREFIX = "[HHL]: "

    private val json = Json {
        ignoreUnknownKeys = true
    }


    internal suspend fun fetchRawJson(url: String): String? = withContext(Dispatchers.IO) {
        val safeUrl: URL = try {
            URI.create(url).toURL()
        } catch (e: Exception) {
            LOGGER.warning(PREFIX + "Invalid URL: $url")
            return@withContext null
        }

        try {
            val connection = safeUrl.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("User-Agent", "HazarsHeadLibrary/1.0")

            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                LOGGER.warning(PREFIX + "json fetch failed with HTTP ${connection.responseCode}: ${connection.responseMessage}")
                return@withContext null
            }

            return@withContext connection.inputStream.bufferedReader().use { it.readText() }

        } catch (e: IOException) {
            LOGGER.warning(PREFIX + "IOException during fetch: ${e.message}")
            return@withContext null
        }
    }


    internal suspend fun fetchDefaultHeads(): List<HeadData> {
        val url = "https://raw.githubusercontent.com/Hazar122/Hazars-Head-Library/refs/heads/main/HeadAPIData/Heads.json"

        val jsonText = fetchRawJson(url)
        return json.decodeFromString(jsonText.toString())
    }

    suspend fun fetchCustomHeadsFromUrl(url: String): List<HeadData> {
        val jsonText = fetchRawJson(url)
        return json.decodeFromString(jsonText.toString())
    }

}