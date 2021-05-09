package kz.azan.askimam.imamrating.app.usecase

import io.vavr.control.Option
import io.vavr.kotlin.some
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.imamrating.domain.repo.ImamRatingRepository
import kz.azan.askimam.user.domain.model.User

@UseCase
class IncreaseImamsRating(private val repo: ImamRatingRepository) {

    operator fun invoke(imamId: User.Id): Option<Declination> = repo.findById(imamId).fold(
        { some(it) },
        {
            it.increment()
            repo.save(it)
        }
    )
}
