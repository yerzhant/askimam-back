package kz.azan.askimam.favorite.app.usecase

import io.vavr.control.Option
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.user.domain.service.GetCurrentUser
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.favorite.domain.model.FavoriteRepository
import kz.azan.askimam.favorite.domain.policy.DeleteFavoritePolicy

@UseCase
class DeleteFavorite(
    private val getCurrentUser: GetCurrentUser,
    private val favoriteRepository: FavoriteRepository,
) {
    operator fun invoke(chatId: Chat.Id): Option<Declination> {
        val currentUser = getCurrentUser()

        return favoriteRepository.findByUserIdAndChatId(currentUser.id, chatId).fold(
            { some(it) },
            {
                DeleteFavoritePolicy.forAll.isAllowed(it, currentUser).orElse {
                    favoriteRepository.delete(it)
                }
            }
        )
    }
}