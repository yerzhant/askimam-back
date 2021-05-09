package kz.azan.askimam.imamrating

import io.mockk.mockk
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.imamrating.domain.model.ImamRating
import kz.azan.askimam.imamrating.domain.repo.ImamRatingRepository
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.repo.UserRepository

open class ImamRatingFixtures {

    val imamId = User.Id(1)

    val imam = User(imamId, User.Type.Imam, NonBlankString.of("Some Imam"), NonBlankString.of("x"))

    val rating = ImamRating(imamId, 123)

    val repo = mockk<ImamRatingRepository>()

    val userRepo = mockk<UserRepository>()
}
