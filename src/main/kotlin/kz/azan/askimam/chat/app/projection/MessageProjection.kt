package kz.azan.askimam.chat.app.projection

import kz.azan.askimam.chat.domain.model.Message

data class MessageProjection(
    val id: Message.Id,
) {
    companion object {
        fun from(message: Message) = MessageProjection(
            message.id!!,
        )
    }
}
