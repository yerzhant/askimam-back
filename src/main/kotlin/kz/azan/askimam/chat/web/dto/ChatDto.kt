package kz.azan.askimam.chat.web.dto

import kz.azan.askimam.chat.app.projection.ChatProjection

data class ChatDto(
    val id: Long,
    val askedBy: Long,
    val subject: String,
    val isFavorite: Boolean,
    val messages: List<MessageDto>?,
) {
    companion object {
        fun from(chat: ChatProjection) =
            ChatDto(
                chat.id.value,
                chat.askedBy.value,
                chat.subject.string(),
                chat.isFavorite,
                chat.messages?.map { MessageDto.from(it) }?.toJavaList(),
            )
    }
}