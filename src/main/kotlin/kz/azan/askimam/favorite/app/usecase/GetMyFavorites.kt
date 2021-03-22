package kz.azan.askimam.favorite.app.usecase

import io.vavr.control.Either
import kz.azan.askimam.chat.domain.service.GetCurrentUser
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.favorite.domain.model.Favorite
import kz.azan.askimam.favorite.domain.model.FavoriteRepository

class GetMyFavorites(
    private val currentUser: GetCurrentUser,
    private val favoriteRepository: FavoriteRepository,
) {
    operator fun invoke(): Either<Declination, List<Favorite>> = favoriteRepository.findByUserId(currentUser().id)
}