package kz.azan.askimam.chat.domain.model

import io.mockk.every
import io.mockk.verify
import io.vavr.kotlin.left
import io.vavr.kotlin.none
import io.vavr.kotlin.right
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.chat.domain.event.MessageDeleted
import kz.azan.askimam.chat.domain.event.MessageUpdated
import kz.azan.askimam.chat.domain.model.Chat.Type.Private
import kz.azan.askimam.chat.domain.policy.UpdateMessagePolicy
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ChatRepositoryTest : ChatFixtures() {

    @Test
    internal fun `should not create a chat - message id generation issue`() {
        fixtureClock()
        every { messageRepository.generateId() } returns left(Declination.withReason("id error"))

        val chat = Chat.new(
            clock,
            eventPublisher,
            chatRepository,
            messageRepository,
            fixtureChatId,
            Private,
            fixtureInquirerId,
            fixtureMessage,
        )

        assertThat(chat.left).isEqualTo(Declination.withReason("id error"))

        verify(exactly = 0) {
            chatRepository.create(any())
            messageRepository.add(any())
            eventPublisher.publish(any())
        }
    }

    @Test
    internal fun `should not create a chat - chat repo error`() {
        fixtureClock()
        every { messageRepository.generateId() } returns right(fixtureMessageId1)
        every { chatRepository.create(any()) } returns some(Declination.withReason("chat create error"))

        val chat = Chat.new(
            clock,
            eventPublisher,
            chatRepository,
            messageRepository,
            fixtureChatId,
            Private,
            fixtureInquirerId,
            fixtureMessage,
        )

        assertThat(chat.left).isEqualTo(Declination.withReason("chat create error"))

        verify(exactly = 0) {
            messageRepository.add(any())
            eventPublisher.publish(any())
        }
    }

    @Test
    internal fun `should not create a chat - message repo error`() {
        fixtureClock()
        every { messageRepository.generateId() } returns right(fixtureMessageId1)
        every { chatRepository.create(any()) } returns none()
        every { messageRepository.add(any()) } returns some(Declination.withReason("message add error"))

        val chat = Chat.new(
            clock,
            eventPublisher,
            chatRepository,
            messageRepository,
            fixtureChatId,
            Private,
            fixtureInquirerId,
            fixtureMessage,
        )

        assertThat(chat.left).isEqualTo(Declination.withReason("message add error"))

        verify(exactly = 0) {
            eventPublisher.publish(any())
        }
    }

    @Test
    internal fun `should create a chat`() {
        fixtureClock()
        every { messageRepository.generateId() } returns right(fixtureMessageId1)
        every { chatRepository.create(any()) } returns none()
        every { messageRepository.add(any()) } returns none()
        every { eventPublisher.publish(any()) } returns Unit

        val chat = Chat.new(
            clock,
            eventPublisher,
            chatRepository,
            messageRepository,
            fixtureChatId,
            Private,
            fixtureInquirerId,
            fixtureMessage,
        )

        assertThat(chat.isRight).isTrue
    }

    @Test
    internal fun `should not update a subject - chat repo error`() {
        fixtureClock()
        val chat = fixtureChat()
        every { chatRepository.update(chat) } returns some(Declination.withReason("chat error"))

        chat.run {
            val option = updateSubject(Subject.from("New subject"), fixtureInquirer)

            assertThat(option).isEqualTo(some(Declination.withReason("chat error")))
        }
    }

    @Test
    internal fun `should not add a new message - msg id generation error`() {
        fixtureClockAndThen(30)
        every { messageRepository.generateId() } returnsMany listOf(
            right(fixtureMessageId1),
            left(Declination.withReason("msg id error"))
        )
        every { chatRepository.create(any()) } returns none()
        every { messageRepository.add(fixtureSavedMessage()) } returns none()
        every { eventPublisher.publish(any()) } returns Unit
        val chat = Chat.new(
            clock,
            eventPublisher,
            chatRepository,
            messageRepository,
            fixtureChatId,
            Private,
            fixtureInquirerId,
            fixtureMessage,
        ).get()

        chat.run {
            val result = addTextMessage(fixtureNewMessage, fixtureInquirer)

            assertThat(result).isEqualTo(some(Declination.withReason("msg id error")))
            assertThat(messages()).hasSize(1)
        }

        verify(exactly = 0) {
            chatRepository.update(any())
            messageRepository.add(fixtureSavedMessage(fixtureMessageId2))
            eventPublisher.publish(MessageAdded(fixtureSubject, fixtureNewMessage))
        }
    }

    @Test
    internal fun `should not add a new message - chat update error`() {
        fixtureClockAndThen(30)
        every { messageRepository.generateId() } returnsMany listOf(
            right(fixtureMessageId1),
            right(fixtureMessageId2),
        )
        every { chatRepository.create(any()) } returns none()
        every { chatRepository.update(any()) } returns some(Declination.withReason("chat update error"))
        every { messageRepository.add(fixtureSavedMessage()) } returns none()
        every { eventPublisher.publish(any()) } returns Unit
        val chat = Chat.new(
            clock,
            eventPublisher,
            chatRepository,
            messageRepository,
            fixtureChatId,
            Private,
            fixtureInquirerId,
            fixtureMessage,
        ).get()

        chat.run {
            val result = addTextMessage(fixtureNewMessage, fixtureInquirer)

            assertThat(result).isEqualTo(some(Declination.withReason("chat update error")))
            assertThat(messages()).hasSize(1)
        }

        verify(exactly = 0) {
            messageRepository.add(fixtureSavedMessage(fixtureMessageId2))
            eventPublisher.publish(MessageAdded(fixtureSubject, fixtureNewMessage))
        }
    }

    @Test
    internal fun `should not add a new message - msg add error`() {
        fixtureClockAndThen(30)
        every { messageRepository.generateId() } returnsMany listOf(
            right(fixtureMessageId1),
            right(fixtureMessageId2),
        )

        every { chatRepository.create(any()) } returns none()
        every { messageRepository.add(fixtureSavedMessage()) } returns none()
        every { eventPublisher.publish(any()) } returns Unit

        every { chatRepository.update(any()) } returns none()
        every {
            messageRepository.add(fixtureSavedMessage(fixtureMessageId2))
        } returns some(Declination.withReason("msg add error"))

        val chat = Chat.new(
            clock,
            eventPublisher,
            chatRepository,
            messageRepository,
            fixtureChatId,
            Private,
            fixtureInquirerId,
            fixtureMessage,
        ).get()

        chat.run {
            val result = addTextMessage(fixtureNewMessage, fixtureInquirer)

            assertThat(result).isEqualTo(some(Declination.withReason("msg add error")))
            assertThat(messages()).hasSize(1)
        }

        verify(exactly = 0) {
            eventPublisher.publish(MessageAdded(fixtureSubject, fixtureNewMessage))
        }
    }

    @Test
    internal fun `should occur an error when viewed by an imam`() {
        fixtureClock()
        val chat = fixtureChat()
        every { chatRepository.update(chat) } returns some(Declination.withReason("update error"))

        chat.run {
            assertThat(viewedByImam(fixtureImam)).isEqualTo(some(Declination.withReason("update error")))
        }
    }

    @Test
    internal fun `should set Is viewed by an inquirer flag`() {
        fixtureClock()
        val chat = fixtureChat(fixtureNewReply)
        every { chatRepository.update(any()) } returns some(Declination.withReason("update error"))

        chat.run {
            assertThat(viewedByInquirer(fixtureInquirer)).isEqualTo(some(Declination.withReason("update error")))
        }
    }

    @Test
    internal fun `should not delete a message - id not found`() {
        fixtureClock()

        with(fixtureChat()) {
            val option = deleteMessage(fixtureMessageId2, fixtureInquirer)

            assertThat(option).isEqualTo(some(Declination.withReason("Invalid id")))
            assertThat(messages().size).isEqualTo(1)
        }

        verify(exactly = 0) {
            messageRepository.delete(any())
            eventPublisher.publish(MessageDeleted(fixtureMessageId1))
        }
    }

    @Test
    internal fun `should not delete a message - delete error`() {
        fixtureClock()
        every { messageRepository.delete(fixtureSavedMessage()) } returns some(Declination.withReason("delete error"))

        with(fixtureChat()) {
            val option = deleteMessage(fixtureMessageId1, fixtureInquirer)

            assertThat(option).isEqualTo(some(Declination.withReason("delete error")))
            assertThat(messages().size).isEqualTo(1)
        }

        verify(exactly = 0) {
            eventPublisher.publish(MessageDeleted(fixtureMessageId1))
        }
    }

    @Test
    internal fun `should not update a message - invalid id`() {
        fixtureClockAndThen(10)

        with(fixtureChat()) {
            val option = updateTextMessage(
                fixtureMessageId2,
                fixtureInquirer,
                fixtureNewMessage,
                UpdateMessagePolicy.forAll
            )

            assertThat(option).isEqualTo(some(Declination.withReason("Invalid id")))
            assertThat(messages().first().text).isEqualTo(fixtureMessage)
            assertThat(messages().first().updatedAt).isNull()
        }

        verify(exactly = 0) {
            messageRepository.update(any())
            eventPublisher.publish(MessageUpdated(fixtureMessageId1, fixtureNewMessage, timeAfter(10)))
        }
    }

    @Test
    internal fun `should not update a message - update error`() {
        fixtureClockAndThen(10)
        every { messageRepository.update(any()) } returns some(Declination.withReason("update error"))

        with(fixtureChat()) {
            val option = updateTextMessage(
                fixtureMessageId1,
                fixtureInquirer,
                fixtureNewMessage,
                UpdateMessagePolicy.forAll
            )

            assertThat(option).isEqualTo(some(Declination.withReason("update error")))
        }

        verify(exactly = 0) {
            eventPublisher.publish(MessageUpdated(fixtureMessageId1, fixtureNewMessage, timeAfter(10)))
        }
    }
}