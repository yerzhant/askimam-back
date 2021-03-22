package kz.azan.askimam.favorite.app.usecase

import kz.azan.askimam.chat.app.service.GetCurrentUser
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.favorite.domain.model.Favorite
import kz.azan.askimam.favorite.domain.model.FavoriteRepository
import java.time.Clock
import java.time.ZonedDateTime

class AddChatToFavorites(
    private val clock: Clock,
    private val getCurrentUser: GetCurrentUser,
    private val favoriteRepository: FavoriteRepository,
) {
    operator fun invoke(id: Chat.Id) = favoriteRepository.add(
        Favorite(getCurrentUser().id, id, ZonedDateTime.now(clock))
    )
}