package kz.azan.askimam.chat.domain.model

import kz.azan.askimam.common.arch.PackagePrivate
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.user.domain.model.User
import java.time.Clock
import java.time.ZonedDateTime

class Message private constructor(
    private val clock: Clock,

    val id: Id,
    val type: Type,
    val authorId: User.Id,

    private var text: NonBlankString,

    val audio: NonBlankString? = null,

    val createdAt: ZonedDateTime = ZonedDateTime.now(clock),
    private var updatedAt: ZonedDateTime? = null,
) {
    fun updatedAt() = updatedAt

    fun text() = text

    @PackagePrivate
    fun updateText(text: NonBlankString) {
        this.text = text
        updatedAt = ZonedDateTime.now(clock)
    }

    companion object {
        fun newText(
            clock: Clock,
            id: Id,
            authorId: User.Id,
            text: NonBlankString,
        ) = Message(clock, id, Type.Text, authorId, text)

        fun newAudio(
            clock: Clock,
            id: Id,
            authorId: User.Id,
            audio: NonBlankString,
        ): Message {
            val text = NonBlankString.of("Аудио")
            return Message(clock, id, Type.Audio, authorId, text, audio)
        }

        fun restore(
            clock: Clock,
            id: Id,
            type: Type,
            authorId: User.Id,
            text: NonBlankString,
            audio: NonBlankString?,
            createdAt: ZonedDateTime,
            updatedAt: ZonedDateTime?,
        ) =
            Message(
                clock,
                id,
                type,
                authorId,
                text,
                audio,
                createdAt,
                updatedAt
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
        return id.hashCode()
    }

    data class Id(val value: Long)
    enum class Type { Text, Audio }
}
