package kz.azan.askimam.imamrating.app.usecase

import io.mockk.every
import io.mockk.verify
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.imamrating.ImamRatingFixtures
import kz.azan.askimam.imamrating.app.projection.ImamRatingProjection
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GetImamRatingsTest : ImamRatingFixtures() {

    private val underTheTest = GetImamRatings(repo, userRepo)

    @Test
    internal fun `should get the ratings`() {
        every { repo.findAllOrderedByRating() } returns right(listOf(rating))
        every { userRepo.findById(imamId) } returns right(imam)

        val result = underTheTest()

        assertThat(result.get().first()).isEqualTo(ImamRatingProjection(imam, 123))

        verify {
            repo.findAllOrderedByRating()
            userRepo.findById(imamId)
        }
    }

    @Test
    internal fun `should not get the ratings`() {
        every { repo.findAllOrderedByRating() } returns right(listOf(rating))
        every { userRepo.findById(imamId) } returns left(Declination.withReason("x"))

        val result = underTheTest()

        assertThat(result.left.reason.value).isEqualTo("x")
    }

    @Test
    internal fun `should not get the ratings either`() {
        every { repo.findAllOrderedByRating() } returns left(Declination.withReason("x"))

        val result = underTheTest()

        assertThat(result.left.reason.value).isEqualTo("x")
    }
}
