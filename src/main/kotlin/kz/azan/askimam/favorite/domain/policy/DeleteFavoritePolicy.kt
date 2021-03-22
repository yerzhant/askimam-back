package kz.azan.askimam.favorite.domain.policy

import io.vavr.control.Option
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.favorite.domain.model.Favorite
import kz.azan.askimam.user.domain.model.User

fun interface DeleteFavoritePolicy {
    fun isAllowed(favorite: Favorite, user: User): Option<Declination>

    companion object {
        val forAll = DeleteFavoritePolicy { favorite, user ->
            if (favorite.userId == user.id)
                none()
            else
                some(Declination.withReason("You're not allowed to delete this favorite"))
        }
    }
}