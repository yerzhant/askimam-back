package kz.azan.askimam.chat.domain.model

import kz.azan.askimam.common.type.NotBlankString
import kz.azan.askimam.user.domain.model.User
import java.time.ZonedDateTime

data class Message(
    val id: Id,
    val type: Type,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime?,
    val authorId: User.Id,
    val text: NotBlankString,
    val audio: NotBlankString?,
) {

    data class Id(val value: Long)
    enum class Type { Text, Audio }

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
}