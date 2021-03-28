package kz.azan.askimam.chat.domain.policy

import kz.azan.askimam.chat.ChatFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class GetUnansweredChatsPolicyTest : ChatFixtures() {

    @Test
    internal fun `should allow to view`() {
        assertThat(GetUnansweredChatsPolicy.forAll.isAllowed(fixtureImam).isEmpty).isTrue
    }

    @Test
    internal fun `should not allow to view`() {
        assertThat(GetUnansweredChatsPolicy.forAll.isAllowed(fixtureInquirer).isDefined).isTrue
    }
}