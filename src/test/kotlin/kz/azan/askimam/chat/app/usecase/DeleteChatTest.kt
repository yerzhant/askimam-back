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
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

internal class DeleteChatTest : ChatFixtures() {

    @Test
    internal fun `should delete a chat by an author`() {
        val chat = fixtures()

        Assertions.assertThat(DeleteChat(getCurrentUser, chatRepository)(fixtureChatId).isEmpty).isTrue

        verify {
            chatRepository.delete(chat)
        }
    }

    @Test
    internal fun `should not delete a chat by an author - delete error`() {
        val chat = fixtures()
        every { chatRepository.delete(chat) } returns some(Declination.withReason("error"))

        Assertions.assertThat(DeleteChat(getCurrentUser, chatRepository)(fixtureChatId).isDefined).isTrue
    }

    @Test
    internal fun `should delete a chat by an imam`() {
        val chat = fixtures()
        every { getCurrentUser() } returns fixtureImam

        Assertions.assertThat(DeleteChat(getCurrentUser, chatRepository)(fixtureChatId).isEmpty).isTrue

        verify {
            chatRepository.delete(chat)
        }
    }

    @Test
    internal fun `should not delete someone else's chat`() {
        fixtureClock()
        val chat = fixtureSavedChat()
        every { getCurrentUser() } returns fixtureAnotherInquirer
        every { chatRepository.findById(fixtureChatId) } returns right(chat)

        Assertions.assertThat(DeleteChat(getCurrentUser, chatRepository)(fixtureChatId).isDefined).isTrue

        verify(exactly = 0) {
            chatRepository.delete(chat)
        }
    }

    @Test
    internal fun `should not find a chat`() {
        every { getCurrentUser() } returns fixtureInquirer
        every { chatRepository.findById(fixtureChatId) } returns left(Declination.withReason("Not found"))

        Assertions.assertThat(DeleteChat(getCurrentUser, chatRepository)(fixtureChatId).isDefined).isTrue

        verify(exactly = 0) {
            chatRepository.delete(any())
        }
    }

    private fun fixtures(): Chat {
        fixtureClock()
        val chat = fixtureSavedChat()
        every { getCurrentUser() } returns fixtureInquirer
        every { chatRepository.findById(fixtureChatId) } returns right(chat)
        every { chatRepository.delete(chat) } returns none()
        return chat
    }
}
