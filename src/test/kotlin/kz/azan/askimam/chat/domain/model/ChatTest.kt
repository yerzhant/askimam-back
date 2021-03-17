package kz.azan.askimam.chat.domain.model

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.mockk.verifySequence
import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.common.domain.Notifications
import kz.azan.askimam.common.type.NotBlankString
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.*

internal class ChatTest {
    private val notifications = mockk<Notifications>()

    private val fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    private val clock = mockk<Clock>()

    @Test
    internal fun `should create a chat`() {
        fixtureClock()

        with(fixturePublicChat()) {
            assertThat(type).isEqualTo(Chat.Type.Public)
            assertThat(createdAt()).isEqualTo(ZonedDateTime.now(fixedClock))
            assertThat(updatedAt()).isEqualTo(ZonedDateTime.now(fixedClock))
            assertThat(isAnswered()).isFalse
            assertThat(isViewedByImam()).isFalse
            assertThat(subject()).isEqualTo(fixtureSubject())

            assertThat(messages().size).isEqualTo(1)
            assertThat(messages().first().createdAt).isEqualTo(ZonedDateTime.now(fixedClock))
            assertThat(messages().first().updatedAt).isNull()
            assertThat(messages().first().type).isEqualTo(Message.Type.Text)
            assertThat(messages().first().sender).isEqualTo(Message.Sender.Inquirer)
            assertThat(messages().first().text).isEqualTo(fixtureMessage())
        }

        verify {
            notifications.notify(
                ChatCreated(
                    fixtureSubject(),
                    fixtureMessage()
                )
            )
        }
    }

    @Test
    internal fun `should create a chat without a subject`() {
        val aMessage = fixtureMessage()

        fixtureClock()
        every { notifications.notify(ChatCreated(null, aMessage)) } returns Unit

        Chat(clock, notifications, Chat.Type.Private, aMessage).run {
            assertThat(subject()).isEqualTo(aMessage)
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
            addNewTextMessage(Message.Sender.Inquirer, fixtureNewMessage())

            assertThat(updatedAt()).isEqualTo(zonedDateTimeAfter(30))

            assertThat(messages().size).isEqualTo(2)
            assertThat(messages().last().createdAt).isEqualTo(zonedDateTimeAfter(30))
            assertThat(messages().last().type).isEqualTo(Message.Type.Text)
            assertThat(messages().last().sender).isEqualTo(Message.Sender.Inquirer)
            assertThat(messages().last().text).isEqualTo(fixtureNewMessage())
        }

        verifySequence {
            notifications.notify(
                ChatCreated(
                    fixtureSubject(),
                    fixtureMessage()
                )
            )
            notifications.notify(
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
    internal fun `should reset Is viewed by imam flag after a new message`() {
        fixtureClock()

        fixturePublicChat().run {
            viewedByImam()
            addNewTextMessage(Message.Sender.Inquirer, fixtureNewMessage())

            assertThat(isViewedByImam()).isFalse
        }
    }

    @Test
    internal fun `should add a new reply by an imam`() {
        fixtureClockAndThen(31)

        fixturePublicChat(fixtureNewReply()).run {
            addNewTextMessage(Message.Sender.Imam, fixtureNewReply())

            assertThat(isViewedByInquirer()).isFalse
            assertThat(updatedAt()).isEqualTo(zonedDateTimeAfter(31))

            assertThat(messages().size).isEqualTo(2)
            assertThat(messages().last().createdAt).isEqualTo(zonedDateTimeAfter(31))
            assertThat(messages().last().type).isEqualTo(Message.Type.Text)
            assertThat(messages().last().sender).isEqualTo(Message.Sender.Imam)
            assertThat(messages().last().text).isEqualTo(fixtureNewReply())
        }
    }

    @Test
    internal fun `should set Is viewed by inquirer flag`() {
        fixtureClock()

        fixturePublicChat(fixtureNewReply()).run {
            addNewTextMessage(Message.Sender.Imam, fixtureNewReply())
            assertThat(isViewedByInquirer()).isFalse

            viewedByInquirer()

            assertThat(isViewedByInquirer()).isTrue
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

        every { notifications.notify(ChatCreated(subject, firstMessage)) } returns Unit
        every { notifications.notify(MessageAdded(subject, newMessage)) } returns Unit

        return Chat(
            clock,
            notifications,
            Chat.Type.Public,
            subject,
            firstMessage,
        )
    }

    private fun fixtureSubject() = NotBlankString.of("Subject")

    private fun fixtureNewMessage() = NotBlankString.of("A new message")

    private fun fixtureMessage() = NotBlankString.of("A message")

    private fun fixtureNewReply() = NotBlankString.of("A new reply")

    private fun zonedDateTimeAfter(minutes: Long) = ZonedDateTime.now(fixedClock).plusMinutes(minutes)
}