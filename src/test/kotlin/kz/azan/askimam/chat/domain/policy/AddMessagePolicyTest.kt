package kz.azan.askimam.chat.domain.policy

import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.chat.domain.policy.AddMessagePolicy.Companion.forImam
import kz.azan.askimam.chat.domain.policy.AddMessagePolicy.Companion.forInquirer
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.User.Id
import kz.azan.askimam.user.domain.model.User.Type.Imam
import kz.azan.askimam.user.domain.model.User.Type.Inquirer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class AddMessagePolicyTest : ChatFixtures() {

    @Test
    internal fun `should allow add a new message by an imam`() {
        fixtureClock()
        assertThat(forImam.isAllowed(fixturePublicChat(), User(fixtureImamId, Imam))).isTrue
    }

    @Test
    internal fun `should not allow add a new message as it's not an imam`() {
        fixtureClock()
        assertThat(forImam.isAllowed(fixturePublicChat(), User(fixtureImamId, Inquirer))).isFalse
    }

    @Test
    internal fun `should allow an inquirer to add a message to his own chat`() {
        fixtureClock()
        assertThat(forInquirer.isAllowed(fixturePublicChat(), User(fixtureInquirerId, Inquirer))).isTrue
    }

    @Test
    internal fun `should not allow an inquirer to add a message to someone else's chat`() {
        fixtureClock()
        assertThat(forInquirer.isAllowed(fixturePublicChat(), User(Id(3), Inquirer))).isFalse
    }
}