package kz.azan.askimam.favorite.infra

import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.favorite.domain.model.Favorite
import kz.azan.askimam.user.domain.model.User
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("FAVORITE")
data class FavoriteRow(
    val id: Long?,
    val userId: Long,
    val chatId: Long,
    val addedAt: LocalDateTime,
) {
    companion object {
        fun from(favorite: Favorite) = FavoriteRow(
            favorite.id?.value,
            favorite.userId.value,
            favorite.chatId.value,
            favorite.addedAt,
        )
    }

    fun toDomain() = Favorite(
        if (id != null) Favorite.Id(id) else null,
        User.Id(userId),
        Chat.Id(chatId),
        addedAt
    )
}
