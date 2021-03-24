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
import kz.azan.askimam.common.type.NonBlankString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

internal class ChatTest : ChatFixtures() {

    @Test
    internal fun `should create a chat`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureInquirer

        with(fixtureChat()) {
            assertThat(type).isEqualTo(Public)
            assertThat(subject()).isEqualTo(fixtureSubject)

            assertThat(createdAt).isEqualTo(fixtureNow)
            assertThat(updatedAt()).isEqualTo(fixtureNow)

            assertThat(isVisibleToPublic()).isFalse
            assertThat(isViewedByImam()).isFalse
            assertThat(isViewedByInquirer()).isTrue

            assertThat(askedBy).isEqualTo(fixtureInquirerId)
            assertThat(answeredBy()).isNull()

            assertThat(inquirerFcmToken()).isEqualTo(fixtureInquirerFcmToken)
            assertThat(imamFcmToken()).isNull()

            assertThat(messages().size).isEqualTo(1)

            assertThat(messages().first().type).isEqualTo(Text)
            assertThat(messages().first().authorId).isEqualTo(fixtureInquirerId)
            assertThat(messages().first().text()).isEqualTo(fixtureMessage)

            assertThat(messages().first().createdAt).isEqualTo(fixtureNow)
            assertThat(messages().first().updatedAt()).isNull()
        }

        verifySequence {
            eventPublisher.publish(ChatCreated(fixtureSubject, fixtureMessage))
        }
    }

    @Test
    internal fun `should create a chat without a subject`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureInquirer
        every { eventPublisher.publish(ChatCreated(null, fixtureMessage)) } returns Unit

        Chat.new(
            type = Private,
            messageText = fixtureMessage,
            inquirerFcmToken = fixtureInquirerFcmToken,

            clock = clock,
            eventPublisher = eventPublisher,
            getCurrentUser = getCurrentUser,
        ).run {
            assertThat(subject()).isNull()
            assertThat(subjectText()).isEqualTo(Subject(fixtureMessage))
        }

        verify {
            eventPublisher.publish(ChatCreated(null, fixtureMessage))
        }
    }

    @Test
    internal fun `should update a subject by an author`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureInquirer
        val chat = fixtureChat()

        chat.run {
            val option = updateSubject(Subject.from("New subject"))

            assertThat(option.isEmpty).isTrue
            assertThat(subject()).isEqualTo(Subject.from("New subject"))
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

        chat.run {
            val option = updateSubject(Subject.from("New subject"))

            assertThat(option.isEmpty).isTrue
            assertThat(subject()).isEqualTo(Subject.from("New subject"))
        }
    }

    @Test
    internal fun `should add a new message`() {
        fixtureClockAndThen(30)
        every { getCurrentUser() } returns fixtureInquirer
        val chat = fixtureChat()

        chat.run {
            val result = addTextMessage(fixtureNewMessage, fixtureInquirerFcmToken)

            assertThat(result.isEmpty).isTrue
            assertThat(updatedAt()).isEqualTo(timeAfter(30))

            assertThat(messages().size).isEqualTo(2)
            assertThat(messages().last().createdAt).isEqualTo(timeAfter(30))
            assertThat(messages().last().updatedAt()).isNull()
            assertThat(messages().last().type).isEqualTo(Text)
            assertThat(messages().last().text()).isEqualTo(fixtureNewMessage)
        }

        verifySequence {
            eventPublisher.publish(ChatCreated(fixtureSubject, fixtureMessage))
            eventPublisher.publish(MessageAdded(fixtureSubject, fixtureNewMessage))
        }
    }

    @Test
    internal fun `should replace an inquirer's fcm token on adding a new message from another device`() {
        fixtureClockAndThen(30)
        every { getCurrentUser() } returns fixtureInquirer
        val chat = fixtureChat()

        chat.run {
            val result = addTextMessage(fixtureNewMessage, FcmToken.from("555"))

            assertThat(result.isEmpty).isTrue
            assertThat(inquirerFcmToken()).isEqualTo(FcmToken.from("555"))
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

        chat.run {
            assertThat(isViewedByImam()).isFalse
            assertThat(viewedByImam().isEmpty).isTrue
            assertThat(isViewedByImam()).isTrue
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

        chat.run {
            viewedByImam()
            val result = addTextMessage(fixtureNewMessage, fixtureInquirerFcmToken)

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

        fixtureChat(fixtureNewReply).run {
            val option = addTextMessage(fixtureNewReply, fixtureImamFcmToken)

            assertThat(option.isEmpty).isTrue
            assertThat(isVisibleToPublic()).isTrue
            assertThat(isViewedByInquirer()).isFalse
            assertThat(answeredBy()).isEqualTo(fixtureImamId)
            assertThat(imamFcmToken()).isEqualTo(fixtureImamFcmToken)
            assertThat(updatedAt()).isEqualTo(timeAfter(31))

            assertThat(messages().size).isEqualTo(2)
            assertThat(messages().last().createdAt).isEqualTo(timeAfter(31))
            assertThat(messages().last().type).isEqualTo(Text)
            assertThat(messages().last().text()).isEqualTo(fixtureNewReply)
            assertThat(messages().last().authorId).isEqualTo(fixtureImamId)
        }
    }

    @Test
    internal fun `should replace an fcm token when adding a new reply by another imam`() {
        fixtureClockAndThen(31)
        every { getCurrentUser() } returnsMany listOf(
            fixtureInquirer,
            fixtureAnotherImam
        )

        fixtureChat(fixtureNewReply).run {
            addTextMessage(fixtureNewReply, FcmToken.from("777"))

            assertThat(imamFcmToken()).isEqualTo(FcmToken.from("777"))
        }
    }

    @Test
    internal fun `should return a chat by an imam to not answered`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureImam

        with(fixtureSavedChat()) {
            assertThat(returnToUnansweredList().isEmpty).isTrue
            assertThat(answeredBy()).isNull()
            assertThat(imamFcmToken()).isNull()
        }
    }

    @Test
    internal fun `should not make a message visible to public if it's private`() {
        fixtureClockAndThen(31)
        every { getCurrentUser() } returnsMany listOf(
            fixtureInquirer,
            fixtureImam
        )

        fixtureChat(fixtureNewReply, Private).run {
            val option = addTextMessage(fixtureNewReply, fixtureImamFcmToken)

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
        val chat = fixtureChat(fixtureNewReply)

        chat.run {
            addTextMessage(fixtureNewReply, fixtureImamFcmToken)
            assertThat(isViewedByInquirer()).isFalse

            assertThat(viewedByInquirer().isEmpty).isTrue

            assertThat(isViewedByInquirer()).isTrue
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

        fixtureChat(audio).run {
            val option = addAudioMessage(fixtureAudio, fixtureImamFcmToken)

            assertThat(option.isEmpty).isTrue
            assertThat(imamFcmToken()).isEqualTo(fixtureImamFcmToken)

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
        every { getCurrentUser() } returns fixtureInquirer
        every { eventPublisher.publish(MessageDeleted(fixtureMessageId1)) } returns Unit

        with(fixtureSavedChat()) {
            val option = deleteMessage(fixtureMessageId1)

            assertThat(option.isEmpty).isTrue
            assertThat(messages()).hasSize(2)
        }

        verify {
            eventPublisher.publish(MessageDeleted(fixtureMessageId1))
        }
    }

    @Test
    internal fun `should not delete last message`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureImam
        every { eventPublisher.publish(any()) } returns Unit

        with(fixtureSavedChat()) {
            assertThat(deleteMessage(fixtureMessageId1).isEmpty).isTrue
            assertThat(deleteMessage(fixtureMessageId3).isEmpty).isTrue
            assertThat(deleteMessage(fixtureMessageId2).isDefined).isTrue
        }
    }

    @Test
    internal fun `should delete a message by an imam`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureImam
        every { eventPublisher.publish(MessageDeleted(fixtureMessageId1)) } returns Unit

        with(fixtureSavedChat()) {
            val option = deleteMessage(fixtureMessageId1)

            assertThat(option.isEmpty).isTrue
            assertThat(messages()).hasSize(2)
        }

        verify {
            eventPublisher.publish(MessageDeleted(fixtureMessageId1))
        }
    }

    @Test
    internal fun `should update a message by an inquirer`() {
        fixtureClockAndThen(10, nowTimes = 1)
        every { getCurrentUser() } returns fixtureInquirer
        every {
            eventPublisher.publish(
                MessageUpdated(
                    fixtureMessageId1,
                    fixtureNewMessage,
                    timeAfter(10)
                )
            )
        } returns Unit

        with(fixtureSavedChat()) {
            val option = updateTextMessage(fixtureMessageId1, fixtureNewMessage, FcmToken.from("444"))

            assertThat(option.isEmpty).isTrue
            assertThat(inquirerFcmToken()).isEqualTo(FcmToken.from("444"))
            assertThat(messages().first().text()).isEqualTo(fixtureNewMessage)
            assertThat(messages().first().updatedAt()).isEqualTo(timeAfter(10))
        }

        verify {
            eventPublisher.publish(MessageUpdated(fixtureMessageId1, fixtureNewMessage, timeAfter(10)))
        }
    }

    @Test
    internal fun `should update a message by an imam`() {
        fixtureClockAndThen(11, nowTimes = 1)
        every { getCurrentUser() } returns fixtureImam
        every {
            eventPublisher.publish(
                MessageUpdated(
                    fixtureMessageId2,
                    NonBlankString.of("Update"),
                    timeAfter(11)
                )
            )
        } returns Unit

        with(fixtureSavedChat()) {
            val option = updateTextMessage(fixtureMessageId2, NonBlankString.of("Update"), FcmToken.from("111"))

            assertThat(option.isEmpty).isTrue
            assertThat(imamFcmToken()).isEqualTo(FcmToken.from("111"))
            assertThat(messages()[1].text()).isEqualTo(NonBlankString.of("Update"))
            assertThat(messages()[1].updatedAt()).isEqualTo(timeAfter(11))
        }

        verify {
            eventPublisher.publish(MessageUpdated(fixtureMessageId2, NonBlankString.of("Update"), timeAfter(11)))
        }
    }

    @Test
    internal fun `should restore a saved chat`() {
        fixtureClock()
        val now = LocalDateTime.now(clock)
        val messages =
            listOf(
                Message.restore(
                    id = fixtureMessageId1,
                    type = Text,
                    authorId = fixtureInquirerId,

                    text = fixtureMessage,
                    audio = null,

                    createdAt = now,
                    updatedAt = null,

                    clock = clock,
                )
            )

        with(
            Chat.restore(
                id = fixtureChatId1,
                type = Public,
                subject = fixtureSubject,

                askedBy = fixtureInquirerId,
                answeredBy = null,

                inquirerFcmToken = fixtureInquirerFcmToken,
                imamFcmToken = null,

                createdAt = now,
                updatedAt = now.plusMinutes(10),

                isVisibleToPublic = false,
                isViewedByImam = true,
                isViewedByInquirer = true,

                messages = messages,

                clock = clock,
                eventPublisher = eventPublisher,
                getCurrentUser = getCurrentUser,
            )
        ) {
            assertThat(type).isEqualTo(Public)
            assertThat(askedBy).isEqualTo(fixtureInquirerId)
            assertThat(answeredBy()).isNull()
            assertThat(createdAt).isEqualTo(now)
            assertThat(updatedAt()).isEqualTo(now.plusMinutes(10))
            assertThat(subject()).isEqualTo(fixtureSubject)
            assertThat(isVisibleToPublic()).isFalse
            assertThat(isViewedByImam()).isTrue
            assertThat(isViewedByInquirer()).isTrue
            assertThat(messages()).hasSize(1)
            assertThat(messages()).isEqualTo(messages)
        }
    }
}