package kz.azan.askimam.favorite.infra.repo

import io.vavr.control.Either
import io.vavr.control.Option
import io.vavr.kotlin.Try
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.favorite.domain.model.Favorite
import kz.azan.askimam.favorite.domain.repo.FavoriteRepository
import kz.azan.askimam.favorite.infra.dao.FavoriteDao
import kz.azan.askimam.favorite.infra.model.FavoriteRow
import kz.azan.askimam.user.domain.model.User
import org.springframework.stereotype.Component

@Component
class FavoriteJdbcRepository(private val favoriteDao: FavoriteDao) : FavoriteRepository {

    override fun findByUserId(id: User.Id): Either<Declination, List<Favorite>> =
        Try { favoriteDao.findByUserId(id.value) }
            .toEither()
            .bimap(
                { getDeclination(it) },
                { set -> set.map { it.toDomain() }.sortedByDescending { it.addedAt } }
            )

    override fun findByUserIdAndChatId(userId: User.Id, chatId: Chat.Id): Either<Declination, Favorite> =
        Try { favoriteDao.findByUserIdAndChatId(userId.value, chatId.value) }
            .toEither()
            .bimap({ getDeclination(it) }, { it.toDomain() })

    override fun add(favorite: Favorite): Option<Declination> =
        Try { favoriteDao.save(FavoriteRow.from(favorite)) }
            .fold(
                { some(getDeclination(it)) },
                { none() }
            )

    override fun delete(favorite: Favorite): Option<Declination> =
        Try { favoriteDao.delete(FavoriteRow.from(favorite)) }
            .fold(
                { some(getDeclination(it)) },
                { none() }
            )

    private fun getDeclination(throwable: Throwable) = Declination.from(throwable)
}