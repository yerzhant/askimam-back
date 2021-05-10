package kz.azan.askimam.imamrating.infra.repo

import io.vavr.control.Either
import io.vavr.control.Option
import io.vavr.kotlin.Try
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.imamrating.domain.model.ImamRating
import kz.azan.askimam.imamrating.domain.repo.ImamRatingRepository
import kz.azan.askimam.imamrating.infra.dao.ImamRatingDao
import kz.azan.askimam.imamrating.infra.model.ImamRatingRow
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.infra.dao.UserDao
import kz.azan.askimam.user.infra.model.askImamDbRole
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Component

@Component
class JdbcImamRatingRepository(
    private val dao: ImamRatingDao,
    private val userDao: UserDao,
) : ImamRatingRepository {

    override fun findById(imamId: User.Id): Either<Declination, ImamRating> =
        Try { dao.findById(imamId.value) }
            .map { it.orElseGet { dao.save(ImamRatingRow(imamId.value, 0)) } }
            .toEither()
            .bimap(
                { Declination.from(it) },
                { it.toDomain() }
            )

    override fun save(imamRating: ImamRating): Option<Declination> =
        Try { dao.save(ImamRatingRow.from(imamRating)) }
            .fold(
                { some(Declination.from(it)) },
                { none() }
            )

    override fun findAllOrderedByRating(): Either<Declination, List<ImamRating>> =
        Try { dao.findAllByOrderByRating() }
            .map { list ->
                list
                    .filter { ratingRow ->
                        val user = userDao.findByIdOrNull(ratingRow.imamId.toInt())
                        user != null && user.roles.map { it.itemName }.contains(askImamDbRole)
                    }
                    .map { it.toDomain() }
            }
            .toEither()
            .bimap(
                { Declination.from(it) },
                { it }
            )
}
