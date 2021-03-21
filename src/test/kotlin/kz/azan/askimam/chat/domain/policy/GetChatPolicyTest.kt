package kz.azan.askimam.chat.domain.policy

import kz.azan.askimam.chat.domain.model.Chat.Type.Private
import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.chat.domain.policy.GetChatPolicy.Companion.forImam
import kz.azan.askimam.chat.domain.policy.GetChatPolicy.Companion.forInquirer
import kz.azan.askimam.chat.domain.policy.GetChatPolicy.Companion.getFor
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GetChatPolicyTest : ChatFixtures() {

    @Test
    internal fun `should return imam's policy`() {
        assertThat(getFor(fixtureImam)).isEqualTo(forImam)
    }

    @Test
    internal fun `should return inquirer's policy`() {
        assertThat(getFor(fixtureInquirer)).isEqualTo(forInquirer)
    }

    @Test
    internal fun `should return a chat to any imam`() {
        fixtureClock()
        assertThat(forImam.isAllowed(fixtureChat(), fixtureImam).isRight).isTrue
    }

    @Test
    internal fun `should return a private chat to any imam`() {
        fixtureClock()
        assertThat(forImam.isAllowed(fixtureChat(type = Private), fixtureImam).isRight).isTrue
    }

    @Test
    internal fun `should be declined for a non imam`() {
        fixtureClock()
        assertThat(
            forImam.isAllowed(
                fixtureChat(),
                fixtureInquirer
            ).left
        ).isEqualTo(Declination.withReason("The operation is not permitted"))
    }

    @Test
    internal fun `should return a chat to an author`() {
        fixtureClock()
        assertThat(forInquirer.isAllowed(fixtureChat(type = Private), fixtureInquirer).isRight).isTrue
    }

    @Test
    internal fun `should return a public chat`() {
        fixtureClock()
        with(fixtureChat(fixtureNewReply)) {
            addTextMessage(AddMessagePolicy.forImam, fixtureMessageId, fixtureNewReply, fixtureImam)
            assertThat(forInquirer.isAllowed(this, fixtureAnotherInquirer).isRight).isTrue
        }
    }

    @Test
    internal fun `should decline the operation when not an author is trying to get a private chat`() {
        fixtureClock()
        assertThat(
            forInquirer.isAllowed(
                fixtureChat(),
                fixtureAnotherInquirer
            ).left
        ).isEqualTo(Declination.withReason("The operation is not permitted"))
    }
}