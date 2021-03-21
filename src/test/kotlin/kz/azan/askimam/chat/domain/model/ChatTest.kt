package kz.azan.askimam.chat.domain.model

import io.mockk.every
import io.mockk.verify
import io.mockk.verifySequence
import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.chat.domain.event.MessageDeleted
import kz.azan.askimam.chat.domain.event.MessageUpdated
import kz.azan.askimam.chat.domain.model.Chat.Type.Private
import kz.azan.askimam.chat.domain.model.Chat.Type.Public
import kz.azan.askimam.chat.domain.model.Message.Type.Audio
import kz.azan.askimam.chat.domain.model.Message.Type.Text
import kz.azan.askimam.chat.domain.policy.AddMessagePolicy
import kz.azan.askimam.chat.domain.policy.DeleteMessagePolicy
import kz.azan.askimam.chat.domain.policy.UpdateChatPolicy
import kz.azan.askimam.chat.domain.policy.UpdateMessagePolicy
import kz.azan.askimam.common.type.NotBlankString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.ZonedDateTime

internal class ChatTest : ChatFixtures() {

    @Test
    internal fun `should create a chat`() {
        fixtureClock()

        with(fixtureChat()) {
            assertThat(type).isEqualTo(Public)
            assertThat(createdAt()).isEqualTo(fixtureNow)
            assertThat(updatedAt()).isEqualTo(fixtureNow)
            assertThat(isVisibleToPublic()).isFalse
            assertThat(isViewedByImam()).isFalse
            assertThat(isViewedByInquirer()).isTrue
            assertThat(askedBy).isEqualTo(fixtureInquirerId)
            assertThat(subject()).isEqualTo(fixtureSubject)

            assertThat(messages().size).isEqualTo(1)
            assertThat(messages().first().type).isEqualTo(Text)
            assertThat(messages().first().authorId).isEqualTo(fixtureInquirerId)
            assertThat(messages().first().text).isEqualTo(fixtureMessage)
            assertThat(messages().first().createdAt).isEqualTo(fixtureNow)
            assertThat(messages().first().updatedAt).isNull()
        }

        verify {
            eventPublisher.publish(ChatCreated(fixtureSubject, fixtureMessage))
        }
    }

    @Test
    internal fun `should create a chat without a subject`() {
        fixtureClock()
        every { eventPublisher.publish(ChatCreated(null, fixtureMessage)) } returns Unit

        Chat.new(
            clock,
            eventPublisher,
            Private,
            fixtureInquirerId,
            fixtureMessageId,
            fixtureMessage,
        ).run {
            assertThat(subject()).isEqualTo(fixtureMessage)
        }

        verify {
            eventPublisher.publish(ChatCreated(null, fixtureMessage))
        }
    }

    @Test
    internal fun `should rename a subject by an author`() {
        fixtureClock()

        fixtureChat().run {
            val option = renameSubject(NotBlankString.of("New subject"), UpdateChatPolicy.forInquirer, fixtureInquirer)

            assertThat(option.isEmpty).isTrue
            assertThat(subject()).isEqualTo(NotBlankString.of("New subject"))
        }
    }

    @Test
    internal fun `should rename a subject by an imam`() {
        fixtureClock()

        fixtureChat().run {
            val option = renameSubject(NotBlankString.of("New subject"), UpdateChatPolicy.forImam, fixtureImam)

            assertThat(option.isEmpty).isTrue
            assertThat(subject()).isEqualTo(NotBlankString.of("New subject"))
        }
    }

    @Test
    internal fun `should add a new message`() {
        fixtureClockAndThen(30)

        fixtureChat().run {
            val result = addTextMessage(
                AddMessagePolicy.forInquirer,
                fixtureMessageId,
                fixtureNewMessage,
                fixtureInquirer
            )

            assertThat(result.isEmpty).isTrue
            assertThat(updatedAt()).isEqualTo(timeAfter(30))

            assertThat(messages().size).isEqualTo(2)
            assertThat(messages().last().createdAt).isEqualTo(timeAfter(30))
            assertThat(messages().last().updatedAt).isNull()
            assertThat(messages().last().type).isEqualTo(Text)
            assertThat(messages().last().text).isEqualTo(fixtureNewMessage)
        }

        verifySequence {
            eventPublisher.publish(ChatCreated(fixtureSubject, fixtureMessage))
            eventPublisher.publish(MessageAdded(fixtureSubject, fixtureNewMessage))
        }
    }

    @Test
    internal fun `should be viewed by an imam`() {
        fixtureClock()

        fixtureChat().run {
            assertThat(isViewedByImam()).isFalse

            viewedByImam()

            assertThat(isViewedByImam()).isTrue
        }
    }

    @Test
    internal fun `should reset Is viewed by an imam flag after a new message`() {
        fixtureClock()

        fixtureChat().run {
            viewedByImam()
            val result = addTextMessage(
                AddMessagePolicy.forInquirer,
                Message.Id(2),
                fixtureNewMessage,
                fixtureInquirer
            )

            assertThat(result.isEmpty).isTrue
            assertThat(isViewedByImam()).isFalse
        }
    }

    @Test
    internal fun `should add a new reply by an imam`() {
        fixtureClockAndThen(31)

        fixtureChat(fixtureNewReply).run {
            val option = addTextMessage(AddMessagePolicy.forImam, Message.Id(2), fixtureNewReply, fixtureImam)

            assertThat(option.isEmpty).isTrue
            assertThat(isVisibleToPublic()).isTrue
            assertThat(isViewedByInquirer()).isFalse
            assertThat(updatedAt()).isEqualTo(timeAfter(31))

            assertThat(messages().size).isEqualTo(2)
            assertThat(messages().last().createdAt).isEqualTo(timeAfter(31))
            assertThat(messages().last().type).isEqualTo(Text)
            assertThat(messages().last().text).isEqualTo(fixtureNewReply)
            assertThat(messages().last().authorId).isEqualTo(fixtureImamId)
        }
    }

    @Test
    internal fun `should not make a message visible to public if it's private`() {
        fixtureClockAndThen(31)

        fixtureChat(fixtureNewReply, Private).run {
            val option = addTextMessage(AddMessagePolicy.forImam, Message.Id(2), fixtureNewReply, fixtureImam)

            assertThat(option.isEmpty).isTrue
            assertThat(isVisibleToPublic()).isFalse
        }
    }

    @Test
    internal fun `should set Is viewed by an inquirer flag`() {
        fixtureClock()

        fixtureChat(fixtureNewReply).run {
            addTextMessage(AddMessagePolicy.forImam, Message.Id(2), fixtureNewReply, fixtureImam)
            assertThat(isViewedByInquirer()).isFalse

            viewedByInquirer()

            assertThat(isViewedByInquirer()).isTrue
        }
    }

    @Test
    internal fun `should add a new audio`() {
        val audio = NotBlankString.of("Аудио")
        fixtureClock()
        fixtureChat(audio).run {
            val option = addAudioMessage(AddMessagePolicy.forImam, Message.Id(2), fixtureAudio, fixtureImam)

            assertThat(option.isEmpty).isTrue
            assertThat(messages().last().type).isEqualTo(Audio)
            assertThat(messages().last().text).isEqualTo(audio)
            assertThat(messages().last().audio).isEqualTo(fixtureAudio)
        }

        verify {
            eventPublisher.publish(MessageAdded(fixtureSubject, audio))
        }
    }

    @Test
    internal fun `should delete a message by an inquirer`() {
        fixtureClock()
        every { eventPublisher.publish(MessageDeleted(fixtureMessageId)) } returns Unit

        with(fixtureChat()) {
            val option = deleteMessage(fixtureMessageId, DeleteMessagePolicy.forInquirer, fixtureInquirer)

            assertThat(option.isEmpty).isTrue
            assertThat(messages().size).isZero
        }

        verify {
            eventPublisher.publish(MessageDeleted(fixtureMessageId))
        }
    }

    @Test
    internal fun `should delete a message by an imam`() {
        fixtureClock()
        every { eventPublisher.publish(MessageDeleted(fixtureMessageId)) } returns Unit

        with(fixtureChat()) {
            val option = deleteMessage(fixtureMessageId, DeleteMessagePolicy.forImam, fixtureImam)

            assertThat(option.isEmpty).isTrue
            assertThat(messages().size).isZero
        }

        verify {
            eventPublisher.publish(MessageDeleted(fixtureMessageId))
        }
    }

    @Test
    internal fun `should update a message by an inquirer`() {
        fixtureClockAndThen(10)
        every {
            eventPublisher.publish(
                MessageUpdated(
                    fixtureMessageId,
                    fixtureNewMessage,
                    timeAfter(10)
                )
            )
        } returns Unit

        with(fixtureChat()) {
            val option = updateTextMessage(
                fixtureMessageId,
                fixtureInquirer,
                fixtureNewMessage,
                UpdateMessagePolicy.forAll
            )

            assertThat(option.isEmpty).isTrue
            assertThat(messages().first().text).isEqualTo(fixtureNewMessage)
            assertThat(messages().first().updatedAt).isEqualTo(timeAfter(10))
        }

        verify {
            eventPublisher.publish(MessageUpdated(fixtureMessageId, fixtureNewMessage, timeAfter(10)))
        }
    }

    @Test
    internal fun `should update a message by an imam`() {
        fixtureClockAndThen(11, 15)
        every {
            eventPublisher.publish(
                MessageUpdated(
                    Message.Id(2),
                    NotBlankString.of("Update"),
                    timeAfter(15)
                )
            )
        } returns Unit

        with(fixtureChat(fixtureNewReply)) {
            addTextMessage(AddMessagePolicy.forImam, Message.Id(2), fixtureNewReply, fixtureImam)
            val option = updateTextMessage(
                Message.Id(2),
                fixtureImam,
                NotBlankString.of("Update"),
                UpdateMessagePolicy.forAll
            )

            assertThat(option.isEmpty).isTrue
            assertThat(messages().last().text).isEqualTo(NotBlankString.of("Update"))
            assertThat(messages().last().updatedAt).isEqualTo(timeAfter(15))
        }

        verify {
            eventPublisher.publish(MessageUpdated(Message.Id(2), NotBlankString.of("Update"), timeAfter(15)))
        }
    }

    @Test
    internal fun `should restore a saved chat`() {
        fixtureClock()
        val now = ZonedDateTime.now(clock)
        val messages =
            mutableListOf(
                Message(
                    fixtureMessageId,
                    Text,
                    now,
                    null,
                    fixtureInquirerId,
                    fixtureMessage,
                    null
                )
            )

        with(
            Chat.restore(
                clock,
                eventPublisher,
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
            assertThat(createdAt()).isEqualTo(now)
            assertThat(updatedAt()).isEqualTo(now.plusMinutes(10))
            assertThat(subject()).isEqualTo(fixtureSubject)
            assertThat(isVisibleToPublic()).isTrue
            assertThat(isViewedByImam()).isTrue
            assertThat(isViewedByInquirer()).isFalse
            assertThat(messages()).hasSize(1)
            assertThat(messages()).isEqualTo(messages)
        }
    }
}