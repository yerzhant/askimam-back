package kz.azan.askimam.favorite.domain.model

import io.vavr.control.Either
import io.vavr.control.Option
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.model.User

interface FavoriteRepository {
    fun findByUserId(id: User.Id): Either<Declination, List<Favorite>>
    fun add(favorite: Favorite): Option<Declination>
}
