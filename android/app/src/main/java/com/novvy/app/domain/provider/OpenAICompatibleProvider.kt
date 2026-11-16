package com.novvy.app.domain.provider

import android.util.Log
import com.novvy.app.domain.model.ProviderKind
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * A small OpenAI-compatible provider (works with OpenAI, Groq, OpenRouter,
 * Ollama and any other /chat/completions endpoint). Uses only the platform
 * HTTP stack, so no extra dependencies are required.
 */
class OpenAICompatibleProvider(
    override val kind: ProviderKind,
    private val apiKey: String,
    private val baseUrl: String,
    private val model: String,
) : AIProvider {

    override suspend fun complete(system: String, user: String): String = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply {
            put("model", model)
            put(
                "messages",
                JSONArray()
                    .put(JSONObject().put("role", "system").put("content", system))
                    .put(JSONObject().put("role", "user").put("content", user)),
            )
        }
        val conn = (URL(baseUrl.trimEnd('/') + "/chat/completions").openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 60000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            if (apiKey.isNotBlank()) setRequestProperty("Authorization", "Bearer $apiKey")
        }
        try {
            OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(payload.toString()) }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.let { BufferedReader(InputStreamReader(it, "UTF-8")).use(BufferedReader::readText) } ?: ""
            if (code !in 200..299) {
                Log.w("Novvy", "provider $kind error $code: $text")
                return@withContext "⚠️ $kind returned HTTP $code. Check your API key and model."
            }
            val json = JSONObject(text)
            val choices = json.optJSONArray("choices") ?: return@withContext "(empty response)"
            val message = choices.getJSONObject(0).optJSONObject("message") ?: return@withContext "(empty message)"
            message.optString("content").ifBlank { "(no content)" }
        } catch (e: Exception) {
            Log.w("Novvy", "provider $kind failed", e)
            "⚠️ Could not reach $kind: ${e.message ?: "unknown error"}"
        } finally {
            conn.disconnect()
        }
    }
}