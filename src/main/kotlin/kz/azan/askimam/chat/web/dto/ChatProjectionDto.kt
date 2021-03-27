package kz.azan.askimam.chat.web.dto

import kz.azan.askimam.chat.app.projection.ChatProjection

data class ChatProjectionDto(
    val id: Long,
    val subject: String,
    val isFavorite: Boolean,
) {
    companion object {
        fun from(projection: ChatProjection) =
            ChatProjectionDto(
                projection.id.value,
                projection.subject.string(),
                projection.isFavorite,
            )
    }
}
