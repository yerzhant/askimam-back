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
import kz.azan.askimam.chat.domain.model.Message.Sender.Imam
import kz.azan.askimam.chat.domain.model.Message.Sender.Inquirer
import kz.azan.askimam.chat.domain.model.Message.Type.Audio
import kz.azan.askimam.chat.domain.model.Message.Type.Text
import kz.azan.askimam.common.type.NotBlankString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class ChatTest : ChatFixtures() {

    @Test
    internal fun `should create a chat`() {
        fixtureClock()

        with(fixturePublicChat()) {
            assertThat(type).isEqualTo(Public)
            assertThat(createdAt()).isEqualTo(fixtureNow)
            assertThat(updatedAt()).isEqualTo(fixtureNow)
            assertThat(isAnswered()).isFalse
            assertThat(isViewedByImam()).isFalse
            assertThat(isViewedByInquirer()).isTrue
            assertThat(askedBy).isEqualTo(fixtureInquirerId)
            assertThat(subject()).isEqualTo(fixtureSubject)

            assertThat(messages().size).isEqualTo(1)
            assertThat(messages().first().type).isEqualTo(Text)
            assertThat(messages().first().sender).isEqualTo(Inquirer)
            assertThat(messages().first().text).isEqualTo(fixtureMessage)
            assertThat(messages().first().createdAt).isEqualTo(fixtureNow)
            assertThat(messages().first().updatedAt).isNull()
            assertThat(messages().first().answeredBy).isNull()
        }

        verify {
            eventPublisher.publish(ChatCreated(fixtureSubject, fixtureMessage))
        }
    }

    @Test
    internal fun `should create a chat without a subject`() {
        fixtureClock()
        every { eventPublisher.publish(ChatCreated(null, fixtureMessage)) } returns Unit

        Chat(
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
    internal fun `should rename a subject`() {
        fixtureClock()

        fixturePublicChat().run {
            renameSubject(NotBlankString.of("New subject"))

            assertThat(subject()).isEqualTo(NotBlankString.of("New subject"))
        }
    }

    @Test
    internal fun `should add a new message`() {
        fixtureClockAndThen(30)

        fixturePublicChat().run {
            addTextMessageByInquirer(fixtureMessageId, fixtureNewMessage)

            assertThat(updatedAt()).isEqualTo(timeAfter(30))

            assertThat(messages().size).isEqualTo(2)
            assertThat(messages().last().createdAt).isEqualTo(timeAfter(30))
            assertThat(messages().last().updatedAt).isNull()
            assertThat(messages().last().type).isEqualTo(Text)
            assertThat(messages().last().sender).isEqualTo(Inquirer)
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

        fixturePublicChat().run {
            assertThat(isViewedByImam()).isFalse

            viewedByImam()

            assertThat(isViewedByImam()).isTrue
        }
    }

    @Test
    internal fun `should reset Is viewed by an imam flag after a new message`() {
        fixtureClock()

        fixturePublicChat().run {
            viewedByImam()
            addTextMessageByInquirer(Message.Id(2), fixtureNewMessage)

            assertThat(isViewedByImam()).isFalse
        }
    }

    @Test
    internal fun `should add a new reply by an imam`() {
        fixtureClockAndThen(31)

        fixturePublicChat(fixtureNewReply).run {
            addTextMessageByImam(Message.Id(2), fixtureNewReply, fixtureImamId)

            assertThat(isViewedByInquirer()).isFalse
            assertThat(updatedAt()).isEqualTo(timeAfter(31))

            assertThat(messages().size).isEqualTo(2)
            assertThat(messages().last().createdAt).isEqualTo(timeAfter(31))
            assertThat(messages().last().type).isEqualTo(Text)
            assertThat(messages().last().sender).isEqualTo(Imam)
            assertThat(messages().last().text).isEqualTo(fixtureNewReply)
            assertThat(messages().last().answeredBy).isEqualTo(fixtureImamId)
        }
    }

    @Test
    internal fun `should set Is viewed by an inquirer flag`() {
        fixtureClock()

        fixturePublicChat(fixtureNewReply).run {
            addTextMessageByImam(Message.Id(2), fixtureNewReply, fixtureImamId)
            assertThat(isViewedByInquirer()).isFalse

            viewedByInquirer()

            assertThat(isViewedByInquirer()).isTrue
        }
    }

    @Test
    internal fun `should add a new audio`() {
        val audio = NotBlankString.of("Аудио")
        fixtureClock()
        fixturePublicChat(audio).run {
            addAudioMessage(Message.Id(2), Imam, fixtureAudio, fixtureImamId)

            assertThat(messages().last().type).isEqualTo(Audio)
            assertThat(messages().last().text).isEqualTo(audio)
            assertThat(messages().last().audio).isEqualTo(fixtureAudio)
        }

        verify {
            eventPublisher.publish(MessageAdded(fixtureSubject, audio))
        }
    }

    @Test
    internal fun `should delete a message`() {
        fixtureClock()
        every { eventPublisher.publish(MessageDeleted(fixtureMessageId)) } returns Unit

        with(fixturePublicChat()) {
            deleteMessage(fixtureMessageId)

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

        with(fixturePublicChat()) {
            updateTextMessageByInquirer(fixtureMessageId, fixtureNewMessage)

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

        with(fixturePublicChat(fixtureNewReply)) {
            addTextMessageByImam(Message.Id(2), fixtureNewReply, fixtureImamId)
            updateTextMessageByImam(Message.Id(2), NotBlankString.of("Update"), fixtureImamId)

            assertThat(messages().last().text).isEqualTo(NotBlankString.of("Update"))
            assertThat(messages().last().updatedAt).isEqualTo(timeAfter(15))
        }

        verify {
            eventPublisher.publish(MessageUpdated(Message.Id(2), NotBlankString.of("Update"), timeAfter(15)))
        }
    }
}