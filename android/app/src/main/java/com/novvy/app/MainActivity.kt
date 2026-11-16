package com.novvy.app

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Novvy Android client.
 *
 * A thin, dependency-free client for the Novvy multi-agent workspace backend.
 * The user enters a server URL and a goal; the app calls POST /api/run and
 * renders the orchestrator + per-worker results.
 */
class MainActivity : Activity() {

    private lateinit var serverInput: EditText
    private lateinit var goalInput: EditText
    private lateinit var output: TextView
    private lateinit var runButton: Button
    private lateinit var progress: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(16), dp(16), dp(16))
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "Novvy"
            textSize = 28f
            setTextColor(Color.rgb(0x1E, 0x1E, 0x2E))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        }
        val subtitle = TextView(this).apply {
            text = "Multi-agent workspace"
            textSize = 14f
            setTextColor(Color.rgb(0x66, 0x66, 0x88))
            setPadding(0, dp(2), 0, dp(16))
        }
        root.addView(title)
        root.addView(subtitle)

        serverInput = EditText(this).apply {
            hint = "Backend URL, e.g. http://10.0.2.2:8000"
            setText("http://10.0.2.2:8000")
            setSingleLine()
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_URI
        }
        goalInput = EditText(this).apply {
            hint = "Describe a goal for the agents..."
            minLines = 3
            gravity = Gravity.TOP or Gravity.START
            maxLines = 6
        }

        runButton = Button(this).apply {
            text = "Run workspace"
            setOnClickListener { runWorkspace() }
        }

        progress = ProgressBar(this).apply {
            visibility = View.GONE
        }

        output = TextView(this).apply {
            textSize = 14f
            setTextColor(Color.rgb(0x22, 0x22, 0x22))
            setPadding(0, dp(16), 0, 0)
        }

        root.addView(serverInput)
        root.addView(goalInput)
        root.addView(runButton)
        root.addView(progress)

        val scroll = ScrollView(this).apply {
            isFillViewport = true
            addView(output)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
        }
        root.addView(scroll)

        setContentView(root)
    }

    private fun runWorkspace() {
        val goal = goalInput.text.toString().trim()
        if (goal.isEmpty()) {
            toast("Enter a goal first")
            return
        }
        val baseUrl = serverInput.text.toString().trim().trimEnd('/')
        if (baseUrl.isEmpty()) {
            toast("Enter a backend URL")
            return
        }

        setLoading(true)
        output.text = "Running..."

        Thread {
            val outcome = runCatching { postGoal(baseUrl, goal) }
            runOnUiThread {
                setLoading(false)
                outcome
                    .onSuccess { output.text = it }
                    .onFailure { err ->
                        output.text = "Error: ${err.message}"
                        toast("Request failed")
                    }
            }
        }.start()
    }

    private fun postGoal(baseUrl: String, goal: String): String {
        val url = URL("$baseUrl/api/run")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15000
            readTimeout = 60000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }
        val body = JSONObject().put("goal", goal).toString()
        OutputStreamWriter(conn.outputStream, "UTF-8").use { it.write(body) }

        val code = conn.responseCode
        val stream = if (code in 200..299) conn.inputStream else conn.errorStream
        val text = stream?.let {
            BufferedReader(InputStreamReader(it, "UTF-8")).use(BufferedReader::readText)
        } ?: ""
        conn.disconnect()

        if (code !in 200..299) {
            return "HTTP $code: $text"
        }
        return formatResponse(text)
    }

    private fun formatResponse(raw: String): String {
        val json = JSONObject(raw)
        val sb = StringBuilder()
        sb.append("Goal: ").append(json.optString("goal")).append('\n')
        sb.append("Provider: ").append(json.optString("provider")).append('\n')
        sb.append("Elapsed: ").append(json.optLong("elapsed_ms")).append(" ms\n\n")

        val agents = json.optJSONArray("agents")
        if (agents != null && agents.length() > 0) {
            sb.append("Agents:\n")
            for (i in 0 until agents.length()) {
                val a = agents.getJSONObject(i)
                sb.append("  • ").append(a.optString("name"))
                    .append(": ").append(a.optString("result")).append('\n')
            }
            sb.append('\n')
        }
        sb.append("Result:\n").append(json.optString("result"))
        return sb.toString()
    }

    private fun setLoading(loading: Boolean) {
        progress.visibility = if (loading) View.VISIBLE else View.GONE
        runButton.isEnabled = !loading
    }

    private fun toast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}