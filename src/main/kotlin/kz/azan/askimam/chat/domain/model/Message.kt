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
}