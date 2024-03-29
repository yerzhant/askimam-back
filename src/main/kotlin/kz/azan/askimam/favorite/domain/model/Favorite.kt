package kz.azan.askimam.favorite.domain.model

import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.user.domain.model.User
import java.time.LocalDateTime

data class Favorite(
    val id: Id?, // The mapper does not support a composite key
    val userId: User.Id,
    val chatId: Chat.Id,
    val addedAt: LocalDateTime,
) {

    data class Id(val value: Long)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Favorite

        return id == other.id
    }

    override fun hashCode(): Int {
        return id?.hashCode() ?: 0
    }
}
