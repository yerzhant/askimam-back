package kz.azan.askimam.favorite.app.projection

import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.favorite.domain.model.Favorite

data class FavoriteProjection(
    val id: Favorite.Id,
    val chatId: Chat.Id,
    val subject: Subject,
)
