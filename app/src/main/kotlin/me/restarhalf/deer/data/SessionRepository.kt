package me.restarhalf.deer.data

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class Session(
    val timestamp: LocalDateTime,
    val duration: Int,
    val remark: String,
    val location: String,
    val watchedMovie: Boolean,
    val climax: Boolean,
    val rating: Float,
    val mood: String,
    val props: String
)

object SessionRepository {
    private const val PREFS_NAME = "sessions_prefs"
    private const val KEY_SESSIONS = "sessions"
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    private val json = AppHttp.json

    fun encodeSessions(sessions: List<Session>): String {
        val array = buildJsonArray {
            for (session in sessions) {
                add(
                    buildJsonArray {
                        add(JsonPrimitive(session.timestamp.format(formatter)))
                        add(JsonPrimitive(session.duration))
                        add(JsonPrimitive(session.remark))
                        add(JsonPrimitive(session.location))
                        add(JsonPrimitive(session.watchedMovie))
                        add(JsonPrimitive(session.climax))
                        add(JsonPrimitive(session.rating))
                        add(JsonPrimitive(session.mood))
                        add(JsonPrimitive(session.props))
                    }
                )
            }
        }
        return json.encodeToString(JsonElement.serializer(), array)
    }

    fun decodeSessions(jsonStr: String): List<Session> {
        val element = runCatching { json.parseToJsonElement(jsonStr) }
            .getOrNull()
            ?: return emptyList()
        val array = element as? JsonArray ?: return emptyList()
        return array.mapNotNull { readSession(it) }
    }

    private fun readSession(element: JsonElement): Session? {
        if (element is JsonNull) return null
        val array = element as? JsonArray ?: return null
        var timeStr: String? = null
        var dur = 0
        var rem = ""
        var loc = ""
        var watched = false
        var climaxed = false
        var rate = 0f
        var mood = ""
        var props = ""

        for ((index, item) in array.withIndex()) {
            when (index) {
                0 -> timeStr = readStringOrNull(item)
                1 -> dur = readInt(item)
                2 -> rem = readString(item)
                3 -> loc = readString(item)
                4 -> watched = readBoolean(item)
                5 -> climaxed = readBoolean(item)
                6 -> rate = readFloat(item).coerceIn(0f, 5f)
                7 -> mood = readString(item)
                8 -> props = readString(item)
                else -> Unit
            }
        }

        val parsedTime = timeStr?.takeIf { it.isNotBlank() }
            ?.let { runCatching { LocalDateTime.parse(it, formatter) }.getOrNull() }
            ?: return null

        return Session(
            timestamp = parsedTime,
            duration = dur,
            remark = rem,
            location = loc,
            watchedMovie = watched,
            climax = climaxed,
            rating = rate,
            mood = mood,
            props = props
        )
    }

    private fun readStringOrNull(element: JsonElement?): String? {
        val primitive = element as? JsonPrimitive ?: return null
        return if (primitive.isString) primitive.content else null
    }

    private fun readString(element: JsonElement?): String {
        return readStringOrNull(element) ?: ""
    }

    private fun readInt(element: JsonElement?): Int {
        val primitive = element as? JsonPrimitive ?: return 0
        val content = primitive.content
        if (primitive.isString) return content.toIntOrNull() ?: 0
        content.toIntOrNull()?.let { return it }
        content.toDoubleOrNull()?.let { return it.toInt() }
        return if (content.equals("true", ignoreCase = true) || content == "1") 1 else 0
    }

    private fun readFloat(element: JsonElement?): Float {
        val primitive = element as? JsonPrimitive ?: return 0f
        val content = primitive.content
        if (primitive.isString) return content.toFloatOrNull() ?: 0f
        content.toFloatOrNull()?.let { return it }
        return if (content.equals("true", ignoreCase = true) || content == "1") 1f else 0f
    }

    private fun readBoolean(element: JsonElement?): Boolean {
        val primitive = element as? JsonPrimitive ?: return false
        val content = primitive.content
        if (primitive.isString) {
            return content.equals("true", ignoreCase = true) || content == "1"
        }
        if (content.equals("true", ignoreCase = true)) return true
        if (content.equals("false", ignoreCase = true)) return false
        content.toDoubleOrNull()?.let { return it != 0.0 }
        return content == "1"
    }

    suspend fun loadSessions(context: Context): List<Session> = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonStr = prefs.getString(KEY_SESSIONS, "[]") ?: "[]"
        decodeSessions(jsonStr)
    }

    suspend fun saveSessions(context: Context, sessions: List<Session>) =
        withContext(Dispatchers.IO) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit {
                putString(KEY_SESSIONS, encodeSessions(sessions))
            }
        }

    suspend fun saveSessionsJson(context: Context, jsonStr: String) = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_SESSIONS, jsonStr)
        }
    }

    suspend fun clearSessions(context: Context) = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            remove(KEY_SESSIONS)
        }
    }
}
