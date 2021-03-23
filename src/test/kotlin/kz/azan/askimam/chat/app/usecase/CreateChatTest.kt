package kz.azan.askimam.chat.app.usecase

import io.mockk.every
import io.mockk.verifySequence
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.chat.domain.model.Chat.Type.Public
import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class CreateChatTest : ChatFixtures() {

    @Test
    internal fun `should create a new chat`() {
        fixtures()

        assertThat(
            CreateChat(clock, eventPublisher, getCurrentUser, chatRepository)
                (Public, fixtureMessage).isEmpty
        ).isTrue

        verifySequence {
            eventPublisher.publish(ChatCreated(null, fixtureMessage))
            chatRepository.create(any())
        }
    }

    @Test
    internal fun `should not create a new chat`() {
        fixtures()
        every { chatRepository.create(any()) } returns some(Declination.withReason("hi"))

        assertThat(
            CreateChat(clock, eventPublisher, getCurrentUser, chatRepository)
                (Public, fixtureMessage).isDefined
        ).isTrue
    }

    @Test
    internal fun `should create a new chat with a subject`() {
        fixtures()

        assertThat(
            CreateChat(clock, eventPublisher, getCurrentUser, chatRepository)
                .withSubject(Public, fixtureSubject, fixtureMessage).isEmpty
        ).isTrue

        verifySequence {
            eventPublisher.publish(ChatCreated(fixtureSubject, fixtureMessage))
            chatRepository.create(any())
        }
    }

    private fun fixtures() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureInquirer
        every { chatRepository.create(any()) } returns none()
        every { eventPublisher.publish(any()) } returns Unit
    }
}