package kz.azan.askimam.chat.app.usecase

import io.mockk.every
import io.mockk.verify
import io.mockk.verifyOrder
import io.vavr.kotlin.left
import io.vavr.kotlin.none
import io.vavr.kotlin.right
import io.vavr.kotlin.some
import kz.azan.askimam.chat.ChatFixtures
import kz.azan.askimam.chat.domain.event.MessageDeleted
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DeleteChatTest : ChatFixtures() {

    @Test
    internal fun `should delete a chat by an author`() {
        val chat = fixtures()

        assertThat(DeleteChat(getCurrentUser, chatRepository)(fixtureChatId1).isEmpty).isTrue

        verifyOrder {
            eventPublisher.publish(MessageDeleted(fixtureMessageId1, null))
            eventPublisher.publish(MessageDeleted(fixtureMessageId2, null))
            eventPublisher.publish(MessageDeleted(fixtureMessageId3, fixtureAudio))
            chatRepository.delete(chat)
        }
    }

    @Test
    internal fun `should not delete a chat by an author - delete error`() {
        val chat = fixtures()
        every { chatRepository.delete(chat) } returns some(Declination.withReason("error"))

        assertThat(DeleteChat(getCurrentUser, chatRepository)(fixtureChatId1).isDefined).isTrue
    }

    @Test
    internal fun `should delete a chat by an imam`() {
        val chat = fixtures()
        every { getCurrentUser() } returns some(fixtureImam)

        assertThat(DeleteChat(getCurrentUser, chatRepository)(fixtureChatId1).isEmpty).isTrue

        verify {
            chatRepository.delete(chat)
        }
    }

    @Test
    internal fun `should not delete someone else's chat`() {
        fixtureClock()
        val chat = fixtureSavedChat()
        every { getCurrentUser() } returns some(fixtureAnotherInquirer)
        every { chatRepository.findById(fixtureChatId1) } returns right(chat)

        assertThat(DeleteChat(getCurrentUser, chatRepository)(fixtureChatId1).isDefined).isTrue

        verify(exactly = 0) {
            chatRepository.delete(chat)
        }
    }

    @Test
    internal fun `should not find a chat`() {
        every { getCurrentUser() } returns some(fixtureInquirer)
        every { chatRepository.findById(fixtureChatId1) } returns left(Declination.withReason("Not found"))

        assertThat(DeleteChat(getCurrentUser, chatRepository)(fixtureChatId1).isDefined).isTrue

        verify(exactly = 0) {
            chatRepository.delete(any())
        }
    }

    private fun fixtures(): Chat {
        fixtureClock()
        val chat = fixtureSavedChat()
        every { getCurrentUser() } returns some(fixtureInquirer)
        every { chatRepository.findById(fixtureChatId1) } returns right(chat)
        every { chatRepository.delete(chat) } returns none()
        every { eventPublisher.publish(any()) } returns Unit
        return chat
    }
}
