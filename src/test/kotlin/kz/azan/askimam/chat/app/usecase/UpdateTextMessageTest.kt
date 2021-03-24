package kz.azan.askimam.chat.app.usecase

import io.mockk.every
import io.mockk.verify
import io.vavr.kotlin.left
import io.vavr.kotlin.none
import io.vavr.kotlin.right
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class UpdateTextMessageTest : ChatFixtures() {

    @Test
    internal fun `should update a text message`() {
        every { getCurrentUser() } returns fixtureInquirer
        fixtures()

        assertThat(
            UpdateTextMessage(chatRepository)(
                fixtureChatId1,
                fixtureMessageId1,
                fixtureNewMessage,
                fixtureInquirerFcmToken,
            ).isEmpty
        ).isTrue

        verify { chatRepository.update(any()) }
    }

    @Test
    internal fun `should not update a text message - id not found`() {
        every { getCurrentUser() } returns fixtureInquirer
        every { chatRepository.findById(any()) } returns left(Declination.withReason("x"))

        assertThat(
            UpdateTextMessage(chatRepository)(
                fixtureChatId1,
                fixtureMessageId1,
                fixtureNewMessage,
                fixtureInquirerFcmToken,
            ).isDefined
        ).isTrue
    }

    @Test
    internal fun `should not update a text message - update error`() {
        fixtures()
        every { getCurrentUser() } returns fixtureInquirer
        every { chatRepository.update(any()) } returns some(Declination.withReason("x"))

        assertThat(
            UpdateTextMessage(chatRepository)(
                fixtureChatId1,
                fixtureMessageId1,
                fixtureNewMessage,
                fixtureInquirerFcmToken,
            ).isDefined
        ).isTrue

        verify { chatRepository.update(any()) }
    }

    @Test
    internal fun `should not update a text message by an imam`() {
        every { getCurrentUser() } returns fixtureImam
        fixtures()

        assertThat(
            UpdateTextMessage(chatRepository)(
                fixtureChatId1,
                fixtureMessageId1,
                fixtureNewReply,
                fixtureImamFcmToken,
            ).isDefined
        ).isTrue
    }

    private fun fixtures() {
        fixtureClock()
        every { chatRepository.findById(any()) } returns right(fixtureSavedChat())
        every { chatRepository.update(any()) } returns none()
        every { eventPublisher.publish(any()) } returns Unit
    }
}