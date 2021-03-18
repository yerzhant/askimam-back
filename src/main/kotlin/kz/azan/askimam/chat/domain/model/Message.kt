package kz.azan.askimam.chat.domain.model

import kz.azan.askimam.common.type.NotBlankString
import kz.azan.askimam.user.domain.model.User
import java.time.ZonedDateTime

data class Message(
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime?,
    val type: Type,
    val sender: Sender,
    val text: NotBlankString,
    val audio: NotBlankString?,
    val answeredBy: User.Id?,
) {
    enum class Type { Text, Audio }
    enum class Sender { Imam, Inquirer }
}