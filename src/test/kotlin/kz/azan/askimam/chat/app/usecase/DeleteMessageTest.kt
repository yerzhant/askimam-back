package kz.azan.askimam.chat.app.usecase

import io.mockk.every
import io.vavr.kotlin.none
import io.vavr.kotlin.right
import kz.azan.askimam.chat.domain.model.ChatFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class DeleteMessageTest : ChatFixtures() {

    @Test
    internal fun `should delete a message`() {
        every { getCurrentUser() } returns fixtureInquirer
        fixtures()

        assertThat(DeleteMessage(chatRepository)(fixtureChatId, fixtureMessageId1).isEmpty).isTrue
    }

    @Test
    internal fun `should delete a message by an imam`() {
        every { getCurrentUser() } returns fixtureImam
        fixtures()

        assertThat(DeleteMessage(chatRepository)(fixtureChatId, fixtureMessageId1).isEmpty).isTrue
    }

    @Test
    internal fun `should not delete a message`() {
        every { getCurrentUser() } returns fixtureAnotherInquirer
        fixtures()

        assertThat(DeleteMessage(chatRepository)(fixtureChatId, fixtureMessageId1).isDefined).isTrue
    }

    private fun fixtures() {
        every { chatRepository.findById(any()) } returns right(fixtureSavedChat())
        every { messageRepository.delete(any()) } returns none()
        every { eventPublisher.publish(any()) } returns Unit
    }
}