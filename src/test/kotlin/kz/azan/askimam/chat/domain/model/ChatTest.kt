package kz.azan.askimam.chat.domain.model

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.chat.domain.model.Chat.Type.Private
import kz.azan.askimam.chat.domain.model.Chat.Type.Public
import kz.azan.askimam.chat.domain.model.Message.Sender.Imam
import kz.azan.askimam.chat.domain.model.Message.Sender.Inquirer
import kz.azan.askimam.chat.domain.model.Message.Type.Audio
import kz.azan.askimam.chat.domain.model.Message.Type.Text
import kz.azan.askimam.common.domain.EventPublisher
import kz.azan.askimam.common.type.NotBlankString
import kz.azan.askimam.inquirer.domain.model.InquirerId
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.*

internal class ChatTest {
    private val eventPublisher = mockk<EventPublisher>()

    private val fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    private val clock = mockk<Clock>()

    @Test
    internal fun `should create a chat`() {
        fixtureClock()

        with(fixturePublicChat()) {
            assertThat(type).isEqualTo(Public)
            assertThat(createdAt()).isEqualTo(fixtureNow())
            assertThat(updatedAt()).isEqualTo(fixtureNow())
            assertThat(isAnswered()).isFalse
            assertThat(isViewedByImam()).isFalse
            assertThat(isViewedByInquirer()).isTrue
            assertThat(askedBy).isEqualTo(fixtureInquirerId())
            assertThat(answeredBy()).isNull()
            assertThat(subject()).isEqualTo(fixtureSubject())

            assertThat(messages().size).isEqualTo(1)
            assertThat(messages().first().createdAt).isEqualTo(fixtureNow())
            assertThat(messages().first().updatedAt).isNull()
            assertThat(messages().first().type).isEqualTo(Text)
            assertThat(messages().first().sender).isEqualTo(Inquirer)
            assertThat(messages().first().text).isEqualTo(fixtureMessage())
        }

        verify {
            eventPublisher.publish(
                ChatCreated(
                    fixtureSubject(),
                    fixtureMessage(),
                )
            )
        }
    }

    @Test
    internal fun `should create a chat without a subject`() {
        fixtureClock()
        every { eventPublisher.publish(ChatCreated(null, fixtureMessage())) } returns Unit

        Chat(
            clock,
            eventPublisher,
            Private,
            fixtureInquirerId(),
            fixtureMessage(),
        ).run {
            assertThat(subject()).isEqualTo(fixtureMessage())
        }

        verify {
            eventPublisher.publish(
                ChatCreated(
                    null,
                    fixtureMessage(),
                )
            )
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
            addNewTextMessage(Inquirer, fixtureNewMessage())

            assertThat(updatedAt()).isEqualTo(timeAfter(30))

            assertThat(messages().size).isEqualTo(2)
            assertThat(messages().last().createdAt).isEqualTo(timeAfter(30))
            assertThat(messages().last().updatedAt).isNull()
            assertThat(messages().last().type).isEqualTo(Text)
            assertThat(messages().last().sender).isEqualTo(Inquirer)
            assertThat(messages().last().text).isEqualTo(fixtureNewMessage())
        }

        verifySequence {
            eventPublisher.publish(
                ChatCreated(
                    fixtureSubject(),
                    fixtureMessage()
                )
            )
            eventPublisher.publish(
                MessageAdded(
                    fixtureSubject(),
                    fixtureNewMessage()
                )
            )
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
            addNewTextMessage(Inquirer, fixtureNewMessage())

            assertThat(isViewedByImam()).isFalse
        }
    }

    @Test
    internal fun `should add a new reply by an imam`() {
        fixtureClockAndThen(31)

        fixturePublicChat(fixtureNewReply()).run {
            addNewTextMessage(Imam, fixtureNewReply())

            assertThat(isViewedByInquirer()).isFalse
            assertThat(updatedAt()).isEqualTo(timeAfter(31))

            assertThat(messages().size).isEqualTo(2)
            assertThat(messages().last().createdAt).isEqualTo(timeAfter(31))
            assertThat(messages().last().type).isEqualTo(Text)
            assertThat(messages().last().sender).isEqualTo(Imam)
            assertThat(messages().last().text).isEqualTo(fixtureNewReply())
        }
    }

    @Test
    internal fun `should set Is viewed by an inquirer flag`() {
        fixtureClock()

        fixturePublicChat(fixtureNewReply()).run {
            addNewTextMessage(Imam, fixtureNewReply())
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
            addNewAudioMessage(Imam, fixtureAudio())

            assertThat(messages().last().type).isEqualTo(Audio)
            assertThat(messages().last().text).isEqualTo(audio)
            assertThat(messages().last().audio).isEqualTo(fixtureAudio())
        }

        verify {
            eventPublisher.publish(MessageAdded(fixtureSubject(), audio))
        }
    }

    private fun fixtureClock() {
        every { clock.zone } returns fixedClock.zone
        every { clock.instant() } returns fixedClock.instant()
    }

    private fun fixtureClockAndThen(minutes: Long) {
        every { clock.zone } returns fixedClock.zone
        every { clock.instant() } returnsMany listOf(
            fixedClock.instant(),
            Clock.offset(fixedClock, Duration.ofMinutes(minutes)).instant(),
        )
    }

    private fun fixturePublicChat(newMessage: NotBlankString = fixtureNewMessage()): Chat {
        val subject = fixtureSubject()
        val firstMessage = fixtureMessage()

        every { eventPublisher.publish(ChatCreated(subject, firstMessage)) } returns Unit
        every { eventPublisher.publish(MessageAdded(subject, newMessage)) } returns Unit

        return Chat(
            clock,
            eventPublisher,
            Public,
            fixtureInquirerId(),
            firstMessage,
            subject,
        )
    }

    private fun fixtureInquirerId() = InquirerId(1)

    private fun fixtureSubject() = NotBlankString.of("Subject")

    private fun fixtureMessage() = NotBlankString.of("A message")

    private fun fixtureNewMessage() = NotBlankString.of("A new message")

    private fun fixtureNewReply() = NotBlankString.of("A new reply")

    private fun fixtureAudio() = NotBlankString.of("audio.mp3")

    private fun fixtureNow() = ZonedDateTime.now(fixedClock)

    private fun timeAfter(minutes: Long) = fixtureNow().plusMinutes(minutes)
}