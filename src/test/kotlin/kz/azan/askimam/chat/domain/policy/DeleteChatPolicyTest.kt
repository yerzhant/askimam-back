package kz.azan.askimam.chat.domain.policy

import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.chat.domain.policy.DeleteChatPolicy.Companion.forImam
import kz.azan.askimam.chat.domain.policy.DeleteChatPolicy.Companion.forInquirer
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DeleteChatPolicyTest : ChatFixtures() {

    @Test
    internal fun `should allow to delete a chat by any imam`() {
        fixtureClock()
        assertThat(forImam.isAllowed(fixtureChat(), fixtureImam).isEmpty).isTrue
    }

    @Test
    internal fun `should not allow to delete a chat by not an imam through the imam's policy`() {
        fixtureClock()
        assertThat(
            forImam.isAllowed(
                fixtureChat(),
                fixtureInquirer
            )
        ).isEqualTo(some(Declination.withReason("Only imams are allowed to fulfil this operation")))
    }

    @Test
    internal fun `should allow to delete a chat by its author`() {
        fixtureClock()
        assertThat(forInquirer.isAllowed(fixtureChat(), fixtureInquirer).isEmpty).isTrue
    }

    @Test
    internal fun `should not allow to delete a chat by not its author`() {
        fixtureClock()
        assertThat(
            forInquirer.isAllowed(
                fixtureChat(),
                fixtureAnotherInquirer
            )
        ).isEqualTo(some(Declination.withReason("Operation is not permitted")))
    }
}