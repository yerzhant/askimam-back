package kz.azan.askimam.chat.domain.model

import kz.azan.askimam.common.type.NotBlankString
import kz.azan.askimam.imam.domain.model.ImamId
import java.time.ZonedDateTime

data class Message(
    val createdAt: ZonedDateTime,
    val updatedAt: ZonedDateTime?,
    val type: Type,
    val sender: Sender,
    val text: NotBlankString,
    val audio: NotBlankString?,
    val answeredBy: ImamId?,
) {
    enum class Type { Text, Audio }
    enum class Sender { Imam, Inquirer }
}