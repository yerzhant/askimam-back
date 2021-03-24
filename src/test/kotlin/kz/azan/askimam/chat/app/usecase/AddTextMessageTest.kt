package kz.azan.askimam.chat.app.usecase

import io.mockk.every
import io.mockk.verifySequence
import io.vavr.kotlin.left
import io.vavr.kotlin.none
import io.vavr.kotlin.right
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class AddTextMessageTest : ChatFixtures() {

    @Test
    internal fun `should add a text message`() {
        every { getCurrentUser() } returns fixtureInquirer
        fixtures()

        assertThat(
            AddTextMessage(chatRepository)(
                fixtureChatId1,
                fixtureNewMessage,
                fixtureInquirerFcmToken
            ).isEmpty
        ).isTrue

        verifySequence {
            chatRepository.findById(any())
            eventPublisher.publish(MessageAdded(fixtureSubject, fixtureNewMessage))
            chatRepository.update(any())
        }
    }

    @Test
    internal fun `should not add a text message - chat not found`() {
        every { getCurrentUser() } returns fixtureInquirer
        every { chatRepository.findById(any()) } returns left(Declination.withReason("not found"))

        assertThat(
            AddTextMessage(chatRepository)(
                fixtureChatId1,
                fixtureNewMessage,
                fixtureInquirerFcmToken
            ).isDefined
        ).isTrue
    }

    @Test
    internal fun `should not add a text message - update error`() {
        every { getCurrentUser() } returns fixtureInquirer
        fixtures()
        every { chatRepository.update(any()) } returns some(Declination.withReason("not found"))

        assertThat(
            AddTextMessage(chatRepository)(
                fixtureChatId1,
                fixtureNewMessage,
                fixtureInquirerFcmToken
            ).isDefined
        ).isTrue
    }

    @Test
    internal fun `should add a text message by an imam`() {
        every { getCurrentUser() } returns fixtureImam
        fixtures()

        assertThat(
            AddTextMessage(chatRepository)(
                fixtureChatId1,
                fixtureNewMessage,
                fixtureInquirerFcmToken
            ).isEmpty
        ).isTrue
    }

    @Test
    internal fun `should not add a text message by a public reader`() {
        every { getCurrentUser() } returns fixtureAnotherInquirer
        fixtures()

        assertThat(
            AddTextMessage(chatRepository)(
                fixtureChatId1,
                fixtureNewMessage,
                fixtureInquirerFcmToken
            ).isDefined
        ).isTrue
    }

    @Test
    internal fun `should not add a text message as a chat is not found`() {
        every { getCurrentUser() } returns fixtureInquirer
        fixtures()
        every { chatRepository.findById(any()) } returns left(Declination.withReason("not found"))

        assertThat(
            AddTextMessage(chatRepository)(
                fixtureChatId1,
                fixtureNewMessage,
                fixtureInquirerFcmToken
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