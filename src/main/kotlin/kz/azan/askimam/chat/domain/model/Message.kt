package kz.azan.askimam.chat.domain.model

import kz.azan.askimam.common.arch.PackagePrivate
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.user.domain.model.User
import java.time.Clock
import java.time.LocalDateTime

class Message private constructor(
    private val clock: Clock,

    val id: Id?,
    val type: Type,
    val authorId: User.Id,

    private var text: NonBlankString,

    val audio: NonBlankString? = null,

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
            authorId: User.Id,
            text: NonBlankString,

            clock: Clock,
        ) = Message(
            id = null,
            type = Type.Text,
            authorId = authorId,

            text = text,

            clock = clock,
        )

        fun newAudio(
            authorId: User.Id,
            audio: NonBlankString,

            clock: Clock,
        ): Message {
            val text = NonBlankString.of("Аудио")
            return Message(
                id = null,
                type = Type.Audio,
                authorId = authorId,

                text = text,
                audio = audio,

                clock = clock,
            )
        }

        fun restore(
            id: Id,
            type: Type,
            authorId: User.Id,

            text: NonBlankString,
            audio: NonBlankString?,

            createdAt: LocalDateTime,
            updatedAt: LocalDateTime?,

            clock: Clock,
        ) =
            Message(
                id = id,
                type = type,
                authorId = authorId,

                text = text,
                audio = audio,

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
        return "Message(id=$id, type=$type, authorId=$authorId, text=$text, audio=$audio, createdAt=$createdAt, updatedAt=$updatedAt)"
    }

    data class Id(val value: Long)
    enum class Type { Text, Audio }
}
