package kz.azan.askimam.chat.domain.model

import kz.azan.askimam.common.type.NotBlankString
import kz.azan.askimam.user.domain.model.User
import java.time.ZonedDateTime

data class Message(
    val id: Id,
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime?,
    val type: Type,
    val authorType: AuthorType,
    val text: NotBlankString,
    val audio: NotBlankString?,
    val answeredBy: User.Id?,
) {
    data class Id(val value: Long)

    enum class Type { Text, Audio }
    enum class AuthorType { Imam, Inquirer }
}