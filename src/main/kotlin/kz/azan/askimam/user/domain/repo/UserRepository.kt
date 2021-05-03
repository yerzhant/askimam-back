package kz.azan.askimam.user.domain.repo

import io.vavr.control.Either
import io.vavr.control.Option
import kz.azan.askimam.chat.domain.model.FcmToken
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.model.User

interface UserRepository {
    fun findById(id: User.Id): Either<Declination, User>
    fun findByUsernameAndStatus(username: String?, status: Int): Either<Declination, User>
    fun saveTokens(user: User)
    fun deleteToken(fcmToken: FcmToken, user: User): Option<Declination>
}