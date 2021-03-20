package kz.azan.askimam.chat.domain.policy

import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.chat.domain.policy.UpdateMessagePolicy.Companion.forAll
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class UpdateMessagePolicyTest : ChatFixtures() {

    @Test
    internal fun `should allow an author to update his message`() {
        assertThat(forAll.isAllowed(fixtureInquirer.id, fixtureInquirer).isEmpty).isTrue
    }

    @Test
    internal fun `should not allow a user to update someone else's message`() {
        assertThat(forAll.isAllowed(fixtureInquirerId, fixtureAnotherInquirer))
            .isEqualTo(some(Declination("You're not allowed to edit someone else's message")))
    }
}