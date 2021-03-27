package kz.azan.askimam.favorite.web.dto

import kz.azan.askimam.chat.domain.model.Chat

data class AddChatToFavoritesDto(val id: Long) {
    fun chatId() = Chat.Id(id)
}