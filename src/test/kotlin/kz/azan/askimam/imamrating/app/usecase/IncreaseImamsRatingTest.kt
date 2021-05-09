package kz.azan.askimam.imamrating.app.usecase

import io.mockk.every
import io.mockk.verifySequence
import io.vavr.kotlin.left
import io.vavr.kotlin.none
import io.vavr.kotlin.right
import io.vavr.kotlin.some
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.imamrating.ImamRatingFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class IncreaseImamsRatingTest : ImamRatingFixtures() {

    private val underTheTest = IncreaseImamsRating(repo)

    @Test
    internal fun `should increment imam's rating`() {
        every { repo.findById(imamId) } returns right(rating)
        every { repo.save(rating.copy(rating = 124)) } returns none()

        val result = underTheTest(imamId)

        assertThat(result.isEmpty).isTrue

        verifySequence {
            repo.findById(imamId)
            repo.save(rating.copy(rating = 124))
        }
    }

    @Test
    internal fun `should not increment imam's rating - not saved`() {
        every { repo.findById(imamId) } returns right(rating)
        every { repo.save(rating.copy(rating = 124)) } returns some(Declination.withReason("x"))

        val result = underTheTest(imamId)

        assertThat(result.get().reason.value).isEqualTo("x")
    }

    @Test
    internal fun `should not increment imam's rating - not found or some other db error`() {
        every { repo.findById(imamId) } returns left(Declination.withReason("x"))

        val result = underTheTest(imamId)

        assertThat(result.get().reason.value).isEqualTo("x")
    }
}
