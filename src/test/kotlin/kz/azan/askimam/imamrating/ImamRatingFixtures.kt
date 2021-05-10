package kz.azan.askimam.imamrating

import io.mockk.mockk
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.imamrating.domain.model.ImamRating
import kz.azan.askimam.imamrating.domain.repo.ImamRatingRepository
import kz.azan.askimam.imamrating.infra.dao.ImamRatingDao
import kz.azan.askimam.imamrating.infra.model.ImamRatingRow
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.repo.UserRepository
import kz.azan.askimam.user.infra.dao.UserDao
import kz.azan.askimam.user.infra.model.AuthAssignmentRow
import kz.azan.askimam.user.infra.model.UserRow
import kz.azan.askimam.user.infra.model.askImamDbRole

open class ImamRatingFixtures {

    val imamId = User.Id(1)

    val imam = User(imamId, User.Type.Imam, NonBlankString.of("Some Imam"), NonBlankString.of("x"))

    val imamRow = UserRow(
        imamId.value, "x", "x", "x", 1, "x",
        setOf(AuthAssignmentRow(askImamDbRole, imamId.value.toString()))
    )

    val userRow = UserRow(imamId.value, "x", "x", "x", 1, "x", setOf())

    val rating = ImamRating(imamId, 123)

    val ratingRow = ImamRatingRow(imamId.value, 123)

    val repo = mockk<ImamRatingRepository>()

    val userRepo = mockk<UserRepository>()

    val dao = mockk<ImamRatingDao>()

    val userDao = mockk<UserDao>()
}
