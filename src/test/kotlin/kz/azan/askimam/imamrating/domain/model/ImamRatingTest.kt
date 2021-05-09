package kz.azan.askimam.imamrating.domain.model

import kz.azan.askimam.imamrating.ImamRatingFixtures
import org.assertj.core.api.Assumptions.assumeThat
import org.junit.jupiter.api.Test

internal class ImamRatingTest : ImamRatingFixtures() {

    @Test
    internal fun `should increment rating`() {
        rating.increment()

        assumeThat(rating.rating()).isEqualTo(124)
    }
}
