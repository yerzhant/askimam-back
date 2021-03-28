package kz.azan.askimam.chat.domain.policy

import io.vavr.kotlin.some
import kz.azan.askimam.chat.ChatFixtures
import kz.azan.askimam.chat.domain.model.Message.Type.Audio
import kz.azan.askimam.chat.domain.model.Message.Type.Text
import kz.azan.askimam.chat.domain.policy.UpdateMessagePolicy.Companion.forAll
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class UpdateMessagePolicyTest : ChatFixtures() {

    @Test
    internal fun `should allow an author to update his message`() {
        assertThat(forAll.isAllowed(fixtureInquirer.id, fixtureInquirer, Text).isEmpty).isTrue
    }

    @Test
    internal fun `should not allow a user to update someone else's message`() {
        assertThat(forAll.isAllowed(fixtureInquirerId, fixtureAnotherInquirer, Text))
            .isEqualTo(some(Declination.withReason("You're not allowed to edit someone else's message")))
    }

    @Test
    internal fun `should not allow to edit an audio message`() {
        assertThat(forAll.isAllowed(fixtureInquirerId, fixtureAnotherInquirer, Audio))
            .isEqualTo(some(Declination.withReason("An audio message may not be edited")))
    }
}