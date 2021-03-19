package kz.azan.askimam.chat.domain.policy

import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.chat.domain.policy.AddMessagePolicy.Companion.imam
import kz.azan.askimam.chat.domain.policy.AddMessagePolicy.Companion.inquirer
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
        assertThat(imam.isAllowedToAddMessage(User(fixtureImamId, Imam), fixturePublicChat())).isTrue
    }

    @Test
    internal fun `should not allow add a new message as it's not an imam`() {
        fixtureClock()
        assertThat(imam.isAllowedToAddMessage(User(fixtureImamId, Inquirer), fixturePublicChat())).isFalse
    }

    @Test
    internal fun `should allow an inquirer to add a message to his own chat`() {
        fixtureClock()
        assertThat(inquirer.isAllowedToAddMessage(User(fixtureInquirerId, Inquirer), fixturePublicChat())).isTrue
    }

    @Test
    internal fun `should not allow an inquirer to add a message to someone else's chat`() {
        fixtureClock()
        assertThat(inquirer.isAllowedToAddMessage(User(Id(3), Inquirer), fixturePublicChat())).isFalse
    }
}