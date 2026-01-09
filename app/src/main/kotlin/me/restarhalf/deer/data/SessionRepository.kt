package me.restarhalf.deer.data

import android.content.Context
import androidx.core.content.edit
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okio.Buffer
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

    fun encodeSessions(sessions: List<Session>): String {
        val buffer = Buffer()
        val writer = JsonWriter.of(buffer)
        writer.beginArray()
        for (session in sessions) {
            writer.beginArray()
            writer.value(session.timestamp.format(formatter))
            writer.value(session.duration)
            writer.value(session.remark)
            writer.value(session.location)
            writer.value(session.watchedMovie)
            writer.value(session.climax)
            writer.value(session.rating)
            writer.value(session.mood)
            writer.value(session.props)
            writer.endArray()
        }
        writer.endArray()
        return buffer.readUtf8()
    }

    fun decodeSessions(jsonStr: String): List<Session> {
        val reader = JsonReader.of(Buffer().writeUtf8(jsonStr))
        return runCatching {
            val list = mutableListOf<Session>()
            reader.beginArray()
            while (reader.hasNext()) {
                val session = readSession(reader)
                if (session != null) list.add(session)
            }
            reader.endArray()
            list
        }.getOrElse { emptyList() }
    }

    private fun readSession(reader: JsonReader): Session? {
        if (reader.peek() == JsonReader.Token.NULL) {
            reader.nextNull<Unit>()
            return null
        }

        reader.beginArray()
        var index = 0

        var timeStr: String? = null
        var dur = 0
        var rem = ""
        var loc = ""
        var watched = false
        var climaxed = false
        var rate = 0f
        var mood = ""
        var props = ""

        while (reader.hasNext()) {
            when (index) {
                0 -> timeStr = readStringOrNull(reader)
                1 -> dur = readInt(reader)
                2 -> rem = readString(reader)
                3 -> loc = readString(reader)
                4 -> watched = readBoolean(reader)
                5 -> climaxed = readBoolean(reader)
                6 -> rate = readFloat(reader).coerceIn(0f, 5f)
                7 -> mood = readString(reader)
                8 -> props = readString(reader)
                else -> reader.skipValue()
            }
            index++
        }
        reader.endArray()

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

    private fun readStringOrNull(reader: JsonReader): String? {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> {
                reader.nextNull<Unit>()
                null
            }

            JsonReader.Token.STRING -> reader.nextString()
            else -> {
                reader.skipValue()
                null
            }
        }
    }

    private fun readString(reader: JsonReader): String {
        return readStringOrNull(reader) ?: ""
    }

    private fun readInt(reader: JsonReader): Int {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> {
                reader.nextNull<Unit>()
                0
            }

            JsonReader.Token.NUMBER -> reader.nextDouble().toInt()
            JsonReader.Token.STRING -> reader.nextString().toIntOrNull() ?: 0
            JsonReader.Token.BOOLEAN -> if (reader.nextBoolean()) 1 else 0
            else -> {
                reader.skipValue()
                0
            }
        }
    }

    private fun readFloat(reader: JsonReader): Float {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> {
                reader.nextNull<Unit>()
                0f
            }

            JsonReader.Token.NUMBER -> reader.nextDouble().toFloat()
            JsonReader.Token.STRING -> reader.nextString().toFloatOrNull() ?: 0f
            JsonReader.Token.BOOLEAN -> if (reader.nextBoolean()) 1f else 0f
            else -> {
                reader.skipValue()
                0f
            }
        }
    }

    private fun readBoolean(reader: JsonReader): Boolean {
        return when (reader.peek()) {
            JsonReader.Token.NULL -> {
                reader.nextNull<Unit>()
                false
            }

            JsonReader.Token.BOOLEAN -> reader.nextBoolean()
            JsonReader.Token.NUMBER -> reader.nextDouble() != 0.0
            JsonReader.Token.STRING -> {
                val s = reader.nextString()
                s.equals("true", ignoreCase = true) || s == "1"
            }

            else -> {
                reader.skipValue()
                false
            }
        }
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

    fun saveSessionsJson(context: Context, jsonStr: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            putString(KEY_SESSIONS, jsonStr)
        }
    }

    fun clearSessions(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit {
            remove(KEY_SESSIONS)
        }
    }
}
