package kz.azan.askimam.chat.app.usecase

import io.mockk.every
import io.mockk.verify
import io.vavr.kotlin.left
import io.vavr.kotlin.none
import io.vavr.kotlin.right
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ReturnChatToUnansweredListTest : ChatFixtures() {

    @Test
    internal fun `should return to the list`() {
        val chat = fixtures()

        assertThat(ReturnChatToUnansweredList(chatRepository)(fixtureChatId).isEmpty).isTrue

        verify { chatRepository.update(chat) }
    }

    @Test
    internal fun `should not return to the list - id not found`() {
        fixtures()
        every { chatRepository.findById(fixtureChatId) } returns left(Declination.withReason("x"))

        assertThat(ReturnChatToUnansweredList(chatRepository)(fixtureChatId).isDefined).isTrue
    }

    @Test
    internal fun `should not return to the list - update error`() {
        fixtures()
        every { chatRepository.update(any()) } returns some(Declination.withReason("x"))

        assertThat(ReturnChatToUnansweredList(chatRepository)(fixtureChatId).isDefined).isTrue
    }

    private fun fixtures(): Chat {
        fixtureClock()
        val chat = fixtureSavedChat()
        every { getCurrentUser() } returns fixtureImam
        every { chatRepository.findById(fixtureChatId) } returns right(chat)
        every { chatRepository.update(chat) } returns none()
        return chat
    }
}