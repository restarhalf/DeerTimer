package me.restarhalf.deer.data

import java.time.LocalDateTime

data class SessionDraft(
    val remark: String = "",
    val location: String = "",
    val watchedMovie: Boolean = false,
    val climax: Boolean = false,
    val rating: Float = 3f,
    val mood: String = "平静",
    val props: String = "手"
) {
    fun toSession(timestamp: LocalDateTime, duration: Int): Session {
        return Session(
            timestamp = timestamp,
            duration = duration,
            remark = remark,
            location = location,
            watchedMovie = watchedMovie,
            climax = climax,
            rating = rating,
            mood = mood,
            props = props
        )
    }

    fun applyTo(session: Session): Session {
        return session.copy(
            remark = remark,
            location = location,
            watchedMovie = watchedMovie,
            climax = climax,
            rating = rating,
            mood = mood,
            props = props
        )
    }

    companion object {
        fun fromSession(session: Session): SessionDraft {
            return SessionDraft(
                remark = session.remark,
                location = session.location,
                watchedMovie = session.watchedMovie,
                climax = session.climax,
                rating = session.rating,
                mood = session.mood,
                props = session.props
            )
        }
    }
}
