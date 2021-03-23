package kz.azan.askimam.chat.domain.model

import io.mockk.every
import io.mockk.verify
import io.mockk.verifySequence
import io.vavr.kotlin.none
import io.vavr.kotlin.right
import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.chat.domain.event.MessageDeleted
import kz.azan.askimam.chat.domain.event.MessageUpdated
import kz.azan.askimam.chat.domain.model.Chat.Type.Private
import kz.azan.askimam.chat.domain.model.Chat.Type.Public
import kz.azan.askimam.chat.domain.model.Message.Type.Audio
import kz.azan.askimam.chat.domain.model.Message.Type.Text
import kz.azan.askimam.common.type.NonBlankString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class ChatTest : ChatFixtures() {

    @Test
    internal fun `should create a chat`() {
        fixtureClock()
        every { getCurrentUser() } returnsMany listOf(
            fixtureInquirer
        )

        val chat = fixtureChat()
        with(chat) {
            assertThat(type).isEqualTo(Public)
            assertThat(createdAt).isEqualTo(fixtureNow)
            assertThat(updatedAt()).isEqualTo(fixtureNow)
            assertThat(isVisibleToPublic()).isFalse
            assertThat(isViewedByImam()).isFalse
            assertThat(isViewedByInquirer()).isTrue
            assertThat(askedBy).isEqualTo(fixtureInquirerId)
            assertThat(subject()).isEqualTo(fixtureSubject)

            assertThat(messages().size).isEqualTo(1)
            assertThat(messages().first().type).isEqualTo(Text)
            assertThat(messages().first().authorId).isEqualTo(fixtureInquirerId)
            assertThat(messages().first().text()).isEqualTo(fixtureMessage)
            assertThat(messages().first().createdAt).isEqualTo(fixtureNow)
            assertThat(messages().first().updatedAt()).isNull()

        }

        verifySequence {
            chatRepository.generateId()
            messageRepository.generateId()
            chatRepository.create(chat)
            messageRepository.add(fixtureSavedMessage())
            eventPublisher.publish(ChatCreated(fixtureSubject, fixtureMessage))
        }
    }

    @Test
    internal fun `should create a chat without a subject`() {
        fixtureClock()
        every { getCurrentUser() } returnsMany listOf(
            fixtureInquirer
        )
        every { eventPublisher.publish(ChatCreated(null, fixtureMessage)) } returns Unit
        every { chatRepository.generateId() } returns right(fixtureChatId)
        every { chatRepository.create(any()) } returns none()
        every { messageRepository.generateId() } returns right(fixtureMessageId1)
        every { messageRepository.add(any()) } returns none()

        Chat.new(
            clock,
            eventPublisher,
            getCurrentUser,
            chatRepository,
            messageRepository,
            Private,
            fixtureMessage,
        ).run {
            assertThat(get().subject()).isNull()
        }

        verify {
            eventPublisher.publish(ChatCreated(null, fixtureMessage))
        }
    }

    @Test
    internal fun `should update a subject by an author`() {
        fixtureClock()
        every { getCurrentUser() } returnsMany listOf(
            fixtureInquirer
        )
        val chat = fixtureChat()
        every { chatRepository.update(chat) } returns none()

        chat.run {
            val option = updateSubject(Subject.from("New subject"))

            assertThat(option.isEmpty).isTrue
            assertThat(subject()).isEqualTo(Subject.from("New subject"))
        }

        verifySequence {
            chatRepository.generateId()
            chatRepository.create(chat)
            chatRepository.update(chat)
        }
    }

    @Test
    internal fun `should update a subject by an imam`() {
        fixtureClock()
        every { getCurrentUser() } returnsMany listOf(
            fixtureInquirer,
            fixtureImam
        )
        val chat = fixtureChat()
        every { chatRepository.update(chat) } returns none()

        chat.run {
            val option = updateSubject(Subject.from("New subject"))

            assertThat(option.isEmpty).isTrue
            assertThat(subject()).isEqualTo(Subject.from("New subject"))
        }

        verifySequence {
            chatRepository.generateId()
            chatRepository.create(chat)
            chatRepository.update(chat)
        }
    }

    @Test
    internal fun `should add a new message`() {
        fixtureClockAndThen(30)
        every { getCurrentUser() } returnsMany listOf(
            fixtureInquirer
        )
        every { messageRepository.add(fixtureSavedMessage(fixtureMessageId2)) } returns none()
        val chat = fixtureChat()

        chat.run {
            val result = addTextMessage(fixtureNewMessage)

            assertThat(result.isEmpty).isTrue
            assertThat(updatedAt()).isEqualTo(timeAfter(30))

            assertThat(messages().size).isEqualTo(2)
            assertThat(messages().last().createdAt).isEqualTo(timeAfter(30))
            assertThat(messages().last().updatedAt()).isNull()
            assertThat(messages().last().type).isEqualTo(Text)
            assertThat(messages().last().text()).isEqualTo(fixtureNewMessage)
        }

        verifySequence {
            chatRepository.generateId()
            messageRepository.generateId()
            chatRepository.create(chat)
            messageRepository.add(fixtureSavedMessage())
            eventPublisher.publish(ChatCreated(fixtureSubject, fixtureMessage))
            messageRepository.generateId()
            chatRepository.update(chat)
            messageRepository.add(fixtureSavedMessage(fixtureMessageId2))
            eventPublisher.publish(MessageAdded(fixtureSubject, fixtureNewMessage))
        }
    }

    @Test
    internal fun `should be viewed by an imam`() {
        fixtureClock()
        every { getCurrentUser() } returnsMany listOf(
            fixtureInquirer,
            fixtureImam
        )
        val chat = fixtureChat()
        every { chatRepository.update(chat) } returns none()

        chat.run {
            assertThat(isViewedByImam()).isFalse
            assertThat(viewedByImam().isEmpty).isTrue
            assertThat(isViewedByImam()).isTrue
        }

        verify {
            chatRepository.update(chat)
        }
    }

    @Test
    internal fun `should reset Is viewed by an imam flag after a new message`() {
        fixtureClock()
        every { getCurrentUser() } returnsMany listOf(
            fixtureInquirer,
            fixtureImam,
            fixtureInquirer,
        )
        val chat = fixtureChat()
        every { chatRepository.update(chat) } returns none()
        every { messageRepository.add(fixtureSavedMessage(fixtureMessageId2)) } returns none()

        chat.run {
            viewedByImam()
            val result = addTextMessage(fixtureNewMessage)

            assertThat(result.isEmpty).isTrue
            assertThat(isViewedByImam()).isFalse
        }
    }

    @Test
    internal fun `should add a new reply by an imam`() {
        fixtureClockAndThen(31)
        every { getCurrentUser() } returnsMany listOf(
            fixtureInquirer,
            fixtureImam
        )
        every { messageRepository.add(fixtureSavedMessage(fixtureMessageId2)) } returns none()

        fixtureChat(fixtureNewReply).run {
            val option = addTextMessage(fixtureNewReply)

            assertThat(option.isEmpty).isTrue
            assertThat(isVisibleToPublic()).isTrue
            assertThat(isViewedByInquirer()).isFalse
            assertThat(updatedAt()).isEqualTo(timeAfter(31))

            assertThat(messages().size).isEqualTo(2)
            assertThat(messages().last().createdAt).isEqualTo(timeAfter(31))
            assertThat(messages().last().type).isEqualTo(Text)
            assertThat(messages().last().text()).isEqualTo(fixtureNewReply)
            assertThat(messages().last().authorId).isEqualTo(fixtureImamId)
        }
    }

    @Test
    internal fun `should not make a message visible to public if it's private`() {
        fixtureClockAndThen(31)
        every { getCurrentUser() } returnsMany listOf(
            fixtureInquirer,
            fixtureImam
        )
        every { messageRepository.add(fixtureSavedMessage(fixtureMessageId2)) } returns none()

        fixtureChat(fixtureNewReply, Private).run {
            val option = addTextMessage(fixtureNewReply)

            assertThat(option.isEmpty).isTrue
            assertThat(isVisibleToPublic()).isFalse
        }
    }

    @Test
    internal fun `should set Is viewed by an inquirer flag`() {
        fixtureClock()
        every { getCurrentUser() } returnsMany listOf(
            fixtureInquirer,
            fixtureImam,
            fixtureInquirer,
        )
        every { messageRepository.add(fixtureSavedMessage(fixtureMessageId2)) } returns none()
        val chat = fixtureChat(fixtureNewReply)

        chat.run {
            addTextMessage(fixtureNewReply)
            assertThat(isViewedByInquirer()).isFalse
            assertThat(viewedByInquirer().isEmpty).isTrue
            assertThat(isViewedByInquirer()).isTrue
        }

        verify(exactly = 2) {
            chatRepository.update(chat)
        }
    }

    @Test
    internal fun `should add a new audio`() {
        val audio = NonBlankString.of("Аудио")
        fixtureClock()
        every { getCurrentUser() } returnsMany listOf(
            fixtureInquirer,
            fixtureImam
        )
        every { messageRepository.add(fixtureSavedAudioMessage()) } returns none()

        fixtureChat(audio).run {
            val option = addAudioMessage(fixtureAudio)

            assertThat(option.isEmpty).isTrue
            assertThat(messages().last().type).isEqualTo(Audio)
            assertThat(messages().last().text()).isEqualTo(audio)
            assertThat(messages().last().audio).isEqualTo(fixtureAudio)
        }

        verify {
            eventPublisher.publish(MessageAdded(fixtureSubject, audio))
        }
    }

    @Test
    internal fun `should delete a message by an inquirer`() {
        fixtureClock()
        every { getCurrentUser() } returnsMany listOf(
            fixtureInquirer
        )
        every { messageRepository.delete(fixtureSavedMessage()) } returns none()
        every { eventPublisher.publish(MessageDeleted(fixtureMessageId1)) } returns Unit

        with(fixtureChat()) {
            val option = deleteMessage(fixtureMessageId1)

            assertThat(option.isEmpty).isTrue
            assertThat(messages().size).isZero
        }

        verifySequence {
            messageRepository.generateId()
            messageRepository.add(fixtureSavedMessage())
            eventPublisher.publish(ChatCreated(fixtureSubject, fixtureMessage))
            messageRepository.delete(fixtureSavedMessage())
            eventPublisher.publish(MessageDeleted(fixtureMessageId1))
        }
    }

    @Test
    internal fun `should delete a message by an imam`() {
        fixtureClock()
        every { getCurrentUser() } returnsMany listOf(
            fixtureInquirer,
            fixtureImam
        )
        every { messageRepository.delete(fixtureSavedMessage()) } returns none()
        every { eventPublisher.publish(MessageDeleted(fixtureMessageId1)) } returns Unit

        with(fixtureChat()) {
            val option = deleteMessage(fixtureMessageId1)

            assertThat(option.isEmpty).isTrue
            assertThat(messages().size).isZero
        }

        verify {
            eventPublisher.publish(MessageDeleted(fixtureMessageId1))
        }
    }

    @Test
    internal fun `should update a message by an inquirer`() {
        fixtureClockAndThen(10)
        every { getCurrentUser() } returnsMany listOf(
            fixtureInquirer
        )
        every {
            messageRepository.update(
                fixtureSavedMessage(
                    updatedAt = timeAfter(10),
                    text = fixtureNewMessage
                )
            )
        } returns none()
        every {
            eventPublisher.publish(
                MessageUpdated(
                    fixtureMessageId1,
                    fixtureNewMessage,
                    timeAfter(10)
                )
            )
        } returns Unit

        with(fixtureChat()) {
            val option = updateTextMessage(fixtureMessageId1, fixtureNewMessage)

            assertThat(option.isEmpty).isTrue
            assertThat(messages().first().text()).isEqualTo(fixtureNewMessage)
            assertThat(messages().first().updatedAt()).isEqualTo(timeAfter(10))
        }

        verifySequence {
            messageRepository.generateId()
            messageRepository.add(fixtureSavedMessage())
            eventPublisher.publish(ChatCreated(fixtureSubject, fixtureMessage))
            messageRepository.update(fixtureSavedMessage(text = fixtureNewMessage, updatedAt = timeAfter(10)))
            eventPublisher.publish(MessageUpdated(fixtureMessageId1, fixtureNewMessage, timeAfter(10)))
        }
    }

    @Test
    internal fun `should update a message by an imam`() {
        fixtureClockAndThen(11, 15)
        every { getCurrentUser() } returnsMany listOf(
            fixtureInquirer,
            fixtureImam,
            fixtureImam
        )
        every { messageRepository.add(fixtureSavedMessage(fixtureMessageId2)) } returns none()
        every { messageRepository.update(fixtureSavedMessage(id = fixtureMessageId2)) } returns none()
        every {
            eventPublisher.publish(
                MessageUpdated(
                    fixtureMessageId2,
                    NonBlankString.of("Update"),
                    timeAfter(15)
                )
            )
        } returns Unit

        with(fixtureChat(fixtureNewReply)) {
            addTextMessage(fixtureNewReply)
            val option = updateTextMessage(fixtureMessageId2, NonBlankString.of("Update"))

            assertThat(option.isEmpty).isTrue
            assertThat(messages().last().text()).isEqualTo(NonBlankString.of("Update"))
            assertThat(messages().last().updatedAt()).isEqualTo(timeAfter(15))
        }

        verify {
            eventPublisher.publish(MessageUpdated(fixtureMessageId2, NonBlankString.of("Update"), timeAfter(15)))
        }
    }

    @Test
    internal fun `should restore a saved chat`() {
        fixtureClock()
        val now = LocalDateTime.now(clock)
        val messages =
            listOf(
                Message.restore(
                    clock,
                    fixtureMessageId1,
                    Text,
                    fixtureInquirerId,
                    fixtureMessage,
                    null,
                    now,
                    null,
                )
            )

        with(
            Chat.restore(
                clock,
                eventPublisher,
                getCurrentUser,
                chatRepository,
                messageRepository,
                fixtureChatId,
                Public,
                fixtureInquirerId,
                now,
                now.plusMinutes(10),
                fixtureSubject,
                messages,
                isVisibleToPublic = true,
                isViewedByImam = true,
                isViewedByInquirer = false,
            )
        ) {
            assertThat(type).isEqualTo(Public)
            assertThat(askedBy).isEqualTo(fixtureInquirerId)
            assertThat(createdAt).isEqualTo(now)
            assertThat(updatedAt()).isEqualTo(now.plusMinutes(10))
            assertThat(subject()).isEqualTo(fixtureSubject)
            assertThat(isVisibleToPublic()).isTrue
            assertThat(isViewedByImam()).isTrue
            assertThat(isViewedByInquirer()).isFalse
            assertThat(messages()).hasSize(1)
            assertThat(messages()).isEqualTo(messages)
        }

        verify(exactly = 0) {
            chatRepository.create(any())
            chatRepository.update(any())
            messageRepository.add(any())
            messageRepository.update(any())
        }
    }
}