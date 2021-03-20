package kz.azan.askimam.chat.domain.model

import io.mockk.every
import io.mockk.mockk
import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.chat.domain.model.Chat.Type.Public
import kz.azan.askimam.common.domain.EventPublisher
import kz.azan.askimam.common.type.NotBlankString
import kz.azan.askimam.user.domain.model.User
import java.time.*

open class ChatFixtures {

    val eventPublisher = mockk<EventPublisher>()

    private val fixedClock: Clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    val clock = mockk<Clock>()

    fun fixtureClock() {
        every { clock.zone } returns fixedClock.zone
        every { clock.instant() } returns fixedClock.instant()
    }

    fun fixtureClockAndThen(vararg minutes: Long) {
        every { clock.zone } returns fixedClock.zone
        every { clock.instant() } returnsMany listOf(
            fixedClock.instant(),
            *minutes.map {
                Clock.offset(fixedClock, Duration.ofMinutes(it)).instant()
            }.toTypedArray()
        )
    }

    fun fixtureChat(newMessage: NotBlankString = fixtureNewMessage, type: Chat.Type = Public): Chat {
        val subject = fixtureSubject
        val firstMessage = fixtureMessage

        every { eventPublisher.publish(ChatCreated(subject, firstMessage)) } returns Unit
        every { eventPublisher.publish(MessageAdded(subject, newMessage)) } returns Unit

        return Chat(
            clock,
            eventPublisher,
            type,
            fixtureInquirerId,
            fixtureMessageId,
            firstMessage,
            subject,
        )
    }

    val fixtureImam = User(User.Id(1), User.Type.Imam)
    val fixtureImamId = fixtureImam.id

    val fixtureInquirer = User(User.Id(2), User.Type.Inquirer)
    val fixtureInquirerId = fixtureInquirer.id

    val fixtureAnotherInquirer = User(User.Id(20), User.Type.Inquirer)
    val fixtureAnotherImam = User(User.Id(30), User.Type.Imam)

    val fixtureSubject = NotBlankString.of("Subject")

    val fixtureMessageId = Message.Id(1)

    val fixtureMessage = NotBlankString.of("A message")
    val fixtureNewMessage = NotBlankString.of("A new message")
    val fixtureNewReply = NotBlankString.of("A new reply")

    val fixtureAudio = NotBlankString.of("audio.mp3")

    val fixtureNow: ZonedDateTime = ZonedDateTime.now(fixedClock)

    fun timeAfter(minutes: Long): ZonedDateTime = fixtureNow.plusMinutes(minutes)
}
