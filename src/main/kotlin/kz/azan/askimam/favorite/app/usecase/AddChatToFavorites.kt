package kz.azan.askimam.favorite.app.usecase

import io.vavr.control.Option
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.favorite.domain.model.Favorite
import kz.azan.askimam.favorite.domain.repo.FavoriteRepository
import kz.azan.askimam.user.domain.service.GetCurrentUser
import java.time.Clock
import java.time.LocalDateTime

@UseCase
class AddChatToFavorites(
    private val clock: Clock,
    private val getCurrentUser: GetCurrentUser,
    private val favoriteRepository: FavoriteRepository,
) {
    operator fun invoke(id: Chat.Id): Option<Declination> = getCurrentUser().fold(
        { some(Declination.withReason("Who are you?")) },
        {
            favoriteRepository.add(
                Favorite(null, it.id, id, LocalDateTime.now(clock))
            )
        }
    )
}