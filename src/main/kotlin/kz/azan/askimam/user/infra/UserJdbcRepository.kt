package kz.azan.askimam.user.infra

import io.vavr.control.Either
import io.vavr.kotlin.Try
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.UserRepository
import org.springframework.stereotype.Component

@Component
class UserJdbcRepository(
    private val dao: UserDao,
    private val fcmTokenDao: FcmTokenDao,
) : UserRepository {

    override fun findById(id: User.Id): Either<Declination, User> =
        Try { dao.findById(id.value.toInt()) }
            .map { it.orElseThrow { IllegalArgumentException("The user is not found") } }
            .map { it.toDomain(fcmTokenDao.findByUserId(it.id)) }
            .toEither()
            .bimap(
                { Declination.from(it) },
                { it }
            )

    override fun findByUsernameAndStatus(username: String?, status: Int): Either<Declination, User> =
        Try { dao.findByUsernameAndStatus(username, status) }
            .map { it ?: throw IllegalArgumentException("The user is not found") }
            .map { it.toDomain(fcmTokenDao.findByUserId(it.id)) }
            .toEither()
            .bimap(
                { Declination.from(it) },
                { it }
            )

    override fun saveTokens(user: User) {
        val tokens = user.fcmTokens.map { FcmTokenRow.from(it, user.id.value) }.toSet()
        fcmTokenDao.deleteAll(tokens)
        fcmTokenDao.saveAll(tokens)
    }
}
