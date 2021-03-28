package kz.azan.askimam.favorite.app.usecase

import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.favorite.domain.model.Favorite
import kz.azan.askimam.favorite.domain.model.FavoriteRepository
import kz.azan.askimam.user.domain.service.GetCurrentUser
import java.time.Clock
import java.time.LocalDateTime

@UseCase
class AddChatToFavorites(
    private val clock: Clock,
    private val getCurrentUser: GetCurrentUser,
    private val favoriteRepository: FavoriteRepository,
) {
    operator fun invoke(id: Chat.Id) = favoriteRepository.add(
        Favorite(null, getCurrentUser().id, id, LocalDateTime.now(clock))
    )
}