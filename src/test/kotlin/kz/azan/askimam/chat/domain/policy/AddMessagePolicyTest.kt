package kz.azan.askimam.chat.domain.policy

import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.chat.domain.policy.AddMessagePolicy.Companion.forImam
import kz.azan.askimam.chat.domain.policy.AddMessagePolicy.Companion.forInquirer
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.User.Id
import kz.azan.askimam.user.domain.model.User.Type.Imam
import kz.azan.askimam.user.domain.model.User.Type.Inquirer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AddMessagePolicyTest : ChatFixtures() {

    @Test
    internal fun `should allow to add a new message by an imam`() {
        fixtureClock()
        assertThat(forImam.isAllowed(fixtureChat(), User(fixtureImamId, Imam)).isEmpty).isTrue
    }

    @Test
    internal fun `should not allow to add a new message as it's not an imam`() {
        fixtureClock()
        assertThat(
            forImam.isAllowed(
                fixtureChat(),
                User(fixtureImamId, Inquirer)
            )
        ).isEqualTo(some(Declination.withReason("This operation is only allowed to imams")))
    }

    @Test
    internal fun `should allow an inquirer to add a message to his own chat`() {
        fixtureClock()
        assertThat(forInquirer.isAllowed(fixtureChat(), User(fixtureInquirerId, Inquirer)).isEmpty).isTrue
    }

    @Test
    internal fun `should not allow an inquirer to add a message to someone else's chat`() {
        fixtureClock()
        assertThat(
            forInquirer.isAllowed(
                fixtureChat(),
                User(Id(3), Inquirer)
            )
        ).isEqualTo(some(Declination.withReason("You're not allowed to add a message to someone else's chat")))
    }
}