package kz.azan.askimam.chat.web.dto

import kz.azan.askimam.chat.app.projection.MessageProjection
import kz.azan.askimam.chat.domain.model.Message
import java.time.LocalDateTime

data class MessageDto(
    val id: Long,
    val type: Message.Type,
    val text: String,
    val author: String?,
    val createdAt: LocalDateTime, // TODO: these two time are to be converted to zoned ones
    val updatedAt: LocalDateTime?,
) {
    companion object {
        fun from(message: MessageProjection) = MessageDto(
            message.id.value,
            message.type,
            message.text.value,
            message.author?.name?.value,
            message.createdAt,
            message.updatedAt,
        )
    }
}
