package kz.azan.askimam.chat.domain.policy

import io.mockk.every
import io.vavr.kotlin.some
import kz.azan.askimam.chat.ChatFixtures
import kz.azan.askimam.chat.domain.policy.UpdateChatPolicy.Companion.forImam
import kz.azan.askimam.chat.domain.policy.UpdateChatPolicy.Companion.forInquirer
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class UpdateChatPolicyTest : ChatFixtures() {

    @Test
    internal fun `should allow to edit a chat by any imam`() {
        fixtureClock()
        every { getCurrentUser() } returns some(fixtureInquirer)

        assertThat(forImam.isAllowed(fixtureChat(), fixtureImam).isEmpty).isTrue
    }

    @Test
    internal fun `should not allow to edit a chat by anyone when used in conjunction with imam's policy`() {
        fixtureClock()
        every { getCurrentUser() } returns some(fixtureInquirer)

        assertThat(
            forImam.isAllowed(
                fixtureChat(),
                fixtureInquirer
            )
        ).isEqualTo(some(Declination.withReason("Only imams are allowed to do this")))
    }

    @Test
    internal fun `should allow to edit a chat by its author`() {
        fixtureClock()
        every { getCurrentUser() } returns some(fixtureInquirer)

        assertThat(forInquirer.isAllowed(fixtureChat(), fixtureInquirer).isEmpty).isTrue
    }

    @Test
    internal fun `should not allow not an author to edit a chat`() {
        fixtureClock()
        every { getCurrentUser() } returns some(fixtureInquirer)

        assertThat(
            forInquirer.isAllowed(
                fixtureChat(),
                fixtureAnotherInquirer
            )
        ).isEqualTo(some(Declination.withReason("You're not allowed to edit this chat")))
    }
}