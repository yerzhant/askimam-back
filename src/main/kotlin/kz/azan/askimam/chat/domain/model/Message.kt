package kz.azan.askimam.chat.domain.model

import kz.azan.askimam.chat.domain.model.Message.Type.Audio
import kz.azan.askimam.chat.domain.model.Message.Type.Text
import kz.azan.askimam.common.arch.PackagePrivate
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.User.Type.Imam
import java.time.Clock
import java.time.LocalDateTime

class Message private constructor(
    private val clock: Clock,

    val id: Id?,
    val type: Type,
    private var text: NonBlankString,
    val audio: NonBlankString? = null,
    val duration: NonBlankString? = null,

    val authorId: User.Id,
    val authorType: User.Type,

    val createdAt: LocalDateTime = LocalDateTime.now(clock),
    private var updatedAt: LocalDateTime? = null,
) {
    fun updatedAt() = updatedAt

    fun text() = text

    @PackagePrivate
    fun updateText(text: NonBlankString) {
        this.text = text
        updatedAt = LocalDateTime.now(clock)
    }

    companion object {
        fun newText(
            text: NonBlankString,
            authorId: User.Id,
            authorType: User.Type,

            clock: Clock,
        ) = Message(
            id = null,
            type = Text,
            text = text,

            authorId = authorId,
            authorType = authorType,

            clock = clock,
        )

        fun newAudio(
            audio: NonBlankString,
            duration: NonBlankString,
            authorId: User.Id,

            clock: Clock,
        ): Message {
            val text = NonBlankString.of("Аудио")
            return Message(
                id = null,
                type = Audio,
                text = text,
                audio = audio,
                duration = duration,

                authorId = authorId,
                authorType = Imam,

                clock = clock,
            )
        }

        fun restore(
            id: Id,
            type: Type,
            text: NonBlankString,
            audio: NonBlankString? = null,
            duration: NonBlankString? = null,

            authorId: User.Id,
            authorType: User.Type,

            createdAt: LocalDateTime,
            updatedAt: LocalDateTime? = null,

            clock: Clock,
        ) =
            Message(
                id = id,
                type = type,
                text = text,
                audio = audio,
                duration = duration,

                authorId = authorId,
                authorType = authorType,

                createdAt = createdAt,
                updatedAt = updatedAt,

                clock = clock,
            )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }

    override fun toString(): String {
        return "Message(id=$id, type=$type, authorId=$authorId, text=$text, audio=$audio, duration=$duration, createdAt=$createdAt, updatedAt=$updatedAt)"
    }

    data class Id(val value: Long)
    enum class Type { Text, Audio }
}
