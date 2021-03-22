package kz.azan.askimam.favorite.domain.model

import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.user.domain.model.User
import java.time.ZonedDateTime

data class Favorite(
    val userId: User.Id,
    val chatId: Chat.Id,
    val addedAt: ZonedDateTime,
)
