package kz.azan.askimam.chat.domain.policy

import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.chat.domain.policy.ReturnChatToUnansweredListPolicy.Companion.forAll
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ReturnChatToUnansweredListPolicyTest : ChatFixtures() {

    @Test
    internal fun `should allow to return`() {
        assertThat(forAll.isAllowed(fixtureImam).isEmpty).isTrue
    }

    @Test
    internal fun `should not allow to return`() {
        assertThat(
            forAll.isAllowed(fixtureInquirer).get()
        ).isEqualTo(Declination.withReason("The operation is not permitted"))
    }
}