package kz.azan.askimam.imamrating.app.usecase

import io.vavr.collection.Seq
import io.vavr.control.Either
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.imamrating.app.projection.ImamRatingProjection
import kz.azan.askimam.imamrating.domain.repo.ImamRatingRepository
import kz.azan.askimam.user.domain.repo.UserRepository

@UseCase
class GetImamRatings(
    private val repository: ImamRatingRepository,
    private val userRepository: UserRepository,
) {

    operator fun invoke(): Either<Declination, Seq<ImamRatingProjection>> =
        repository.findAllOrderedByRating().flatMap { list ->
            Either.sequenceRight(list.map { rating ->
                userRepository.findById(rating.imamId).map { ImamRatingProjection(it, rating.rating()) }
            })
        }
}
