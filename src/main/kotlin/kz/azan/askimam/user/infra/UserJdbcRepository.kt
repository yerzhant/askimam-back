package kz.azan.askimam.user.infra

import io.vavr.control.Either
import io.vavr.kotlin.Try
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.UserRepository

class UserJdbcRepository(private val dao: UserDao) : UserRepository {

    override fun findById(id: User.Id): Either<Declination, User> =
        Try { dao.findById(id.value.toInt()) }
            .map { it.orElseThrow { IllegalArgumentException("The user is not found") } }
            .toEither()
            .bimap(
                { Declination.from(it) },
                { it.toDomain() }
            )

    override fun findByUsernameAndStatus(username: String?, status: Int): Either<Declination, User> =
        Try { dao.findByUsernameAndStatus(username, status) }
            .map { it ?: throw IllegalArgumentException("The user is not found") }
            .toEither()
            .bimap(
                { Declination.from(it) },
                { it.toDomain() }
            )
}