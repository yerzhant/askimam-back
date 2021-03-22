package kz.azan.askimam.chat.app.usecase

import io.mockk.every
import io.vavr.kotlin.none
import io.vavr.kotlin.right
import kz.azan.askimam.chat.domain.model.ChatFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class UpdateTextMessageTest : ChatFixtures() {

    @Test
    internal fun `should update a text message`() {
        every { getCurrentUser() } returns fixtureInquirer
        fixtures()

        assertThat(
            UpdateTextMessage(chatRepository)(
                fixtureChatId,
                fixtureMessageId1,
                fixtureNewMessage
            ).isEmpty
        ).isTrue
    }

    @Test
    internal fun `should not add a text message`() {
        every { getCurrentUser() } returns fixtureImam
        fixtures()

        assertThat(
            UpdateTextMessage(chatRepository)(
                fixtureChatId,
                fixtureMessageId1,
                fixtureNewReply
            ).isDefined
        ).isTrue
    }

    private fun fixtures() {
        every { chatRepository.findById(any()) } returns right(fixtureSavedChat())
        every { messageRepository.update(any()) } returns none()
        every { eventPublisher.publish(any()) } returns Unit
    }
}