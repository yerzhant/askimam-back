package kz.azan.askimam.chat.domain.policy

import io.mockk.every
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.chat.domain.policy.AddMessagePolicy.Companion.forImam
import kz.azan.askimam.chat.domain.policy.AddMessagePolicy.Companion.forInquirer
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.User.Type.Inquirer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AddMessagePolicyTest : ChatFixtures() {

    @Test
    internal fun `should allow to add a new message by an imam`() {
        fixtureClock()
        every { getCurrentUser() } returnsMany listOf(
            fixtureInquirer
        )

        assertThat(forImam.isAllowed(fixtureChat(), fixtureImam).isEmpty).isTrue
    }

    @Test
    internal fun `should not allow to add a new message as it's not an imam`() {
        fixtureClock()
        every { getCurrentUser() } returnsMany listOf(
            fixtureInquirer
        )

        assertThat(
            forImam.isAllowed(
                fixtureChat(),
                User(fixtureImamId, Inquirer, NonBlankString.of("A name"))
            )
        ).isEqualTo(some(Declination.withReason("This operation is only allowed to imams")))
    }

    @Test
    internal fun `should allow an inquirer to add a message to his own chat`() {
        fixtureClock()
        every { getCurrentUser() } returnsMany listOf(
            fixtureInquirer
        )

        assertThat(forInquirer.isAllowed(fixtureChat(), fixtureInquirer).isEmpty).isTrue
    }

    @Test
    internal fun `should not allow an inquirer to add a message to someone else's chat`() {
        fixtureClock()
        every { getCurrentUser() } returnsMany listOf(
            fixtureInquirer
        )

        assertThat(
            forInquirer.isAllowed(fixtureChat(), fixtureAnotherInquirer)
        ).isEqualTo(some(Declination.withReason("You're not allowed to add a message to someone else's chat")))
    }
}