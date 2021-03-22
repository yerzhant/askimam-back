package kz.azan.askimam.favorite.domain.model

import io.vavr.control.Either
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.model.User

interface FavoriteRepository {
    fun findByUserId(id: User.Id): Either<Declination, List<Favorite>>
}
