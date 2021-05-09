package kz.azan.askimam.imamrating.domain.repo

import io.vavr.control.Either
import io.vavr.control.Option
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.imamrating.domain.model.ImamRating
import kz.azan.askimam.user.domain.model.User

interface ImamRatingRepository {
    fun findById(imamId: User.Id): Either<Declination, ImamRating>
    fun save(imamRating: ImamRating): Option<Declination>
    fun findAllOrderedByRating(): Either<Declination, List<ImamRating>>
}
