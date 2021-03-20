package kz.azan.askimam.chat.domain.policy

import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.chat.domain.policy.DeleteMessagePolicy.Companion.forImam
import kz.azan.askimam.chat.domain.policy.DeleteMessagePolicy.Companion.forInquirer
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DeleteMessagePolicyTest : ChatFixtures() {

    @Test
    internal fun `should allow to delete a message by any imam`() {
        assertThat(forImam.isAllowed(fixtureInquirerId, fixtureImam).isEmpty).isTrue
        assertThat(forImam.isAllowed(fixtureInquirerId, fixtureAnotherImam).isEmpty).isTrue
    }

    @Test
    internal fun `should not allow to non imam to delete a message`() {
        assertThat(
            forImam.isAllowed(fixtureInquirerId, fixtureInquirer)
        ).isEqualTo(some(Declination("This operation is only allowed to imams")))
    }

    @Test
    internal fun `should allow an inquirer to delete his own message`() {
        assertThat(forInquirer.isAllowed(fixtureInquirerId, fixtureInquirer).isEmpty).isTrue
    }

    @Test
    internal fun `should not allow an inquirer to delete someone else's message`() {
        assertThat(
            forInquirer.isAllowed(fixtureInquirerId, fixtureAnotherInquirer)
        ).isEqualTo(some(Declination("You're not allowed to delete someone else's message")))
    }
}