package kz.azan.askimam.chat.app.usecase

import io.mockk.every
import io.vavr.kotlin.none
import io.vavr.kotlin.right
import kz.azan.askimam.chat.domain.model.Chat.Type.Public
import kz.azan.askimam.chat.domain.model.ChatFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class CreateChatTest : ChatFixtures() {

    @Test
    internal fun `should create a new chat`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureInquirer
        every { chatRepository.generateId() } returns right(fixtureChatId)
        every { messageRepository.generateId() } returns right(fixtureMessageId1)
        every { chatRepository.create(any()) } returns none()
        every { messageRepository.add(any()) } returns none()
        every { eventPublisher.publish(any()) } returns Unit

        assertThat(
            CreateChat(clock, eventPublisher, getCurrentUser, chatRepository, messageRepository)
                (Public, fixtureMessage).isEmpty
        ).isTrue
    }

    @Test
    internal fun `should create a new chat with a subject`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureInquirer
        every { chatRepository.generateId() } returns right(fixtureChatId)
        every { messageRepository.generateId() } returns right(fixtureMessageId1)
        every { chatRepository.create(any()) } returns none()
        every { messageRepository.add(any()) } returns none()
        every { eventPublisher.publish(any()) } returns Unit

        assertThat(
            CreateChat(clock, eventPublisher, getCurrentUser, chatRepository, messageRepository)
                .withSubject(Public, fixtureSubject, fixtureMessage).isEmpty
        ).isTrue
    }
}