package kz.azan.askimam.chat.domain.model

import io.mockk.every
import io.mockk.mockk
import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.chat.domain.model.Chat.Type.Public
import kz.azan.askimam.chat.domain.model.Message.Type.Audio
import kz.azan.askimam.chat.domain.model.Message.Type.Text
import kz.azan.askimam.chat.domain.service.GetCurrentUser
import kz.azan.askimam.common.domain.EventPublisher
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.user.domain.model.User
import java.time.*

open class ChatFixtures {

    val eventPublisher = mockk<EventPublisher>()
    val getCurrentUser = mockk<GetCurrentUser>()
    val chatRepository = mockk<ChatRepository>()

    private val fixedClock: Clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    val clock = mockk<Clock>()

    fun fixtureClock() {
        every { clock.zone } returns fixedClock.zone
        every { clock.instant() } returns fixedClock.instant()
    }

    fun fixtureClockAndThen(vararg minutes: Long, nowTimes: Int = 2) {
        every { clock.zone } returns fixedClock.zone
        every { clock.instant() } returnsMany listOf(
            *Array(nowTimes) { fixedClock.instant() },
            *minutes.map {
                Clock.offset(fixedClock, Duration.ofMinutes(it)).instant()
            }.toTypedArray()
        )
    }

    fun fixtureChat(newMessage: NonBlankString = fixtureNewMessage, type: Chat.Type = Public): Chat {
        val subject = fixtureSubject
        val firstMessage = fixtureMessage

        every { eventPublisher.publish(ChatCreated(subject, firstMessage)) } returns Unit
        every { eventPublisher.publish(MessageAdded(subject, newMessage)) } returns Unit

        return Chat.newWithSubject(
            type = type,
            subject = subject,
            messageText = firstMessage,

            clock = clock,
            eventPublisher = eventPublisher,
            getCurrentUser = getCurrentUser,
        )
    }

    fun fixtureSavedChat(type: Chat.Type = Public): Chat {
        val subject = fixtureSubject
        val now = LocalDateTime.now(clock)

        return Chat.restore(
            id = fixtureChatId,
            type = type,
            subject = subject,

            askedBy = fixtureInquirerId,
            answeredBy = fixtureImamId,

            createdAt = now,
            updatedAt = now,

            isVisibleToPublic = type == Public,
            isViewedByImam = true,
            isViewedByInquirer = true,

            messages = listOf(
                Message.restore(
                    clock,
                    fixtureMessageId1,
                    Text,
                    fixtureInquirerId,
                    fixtureMessage,
                    null,
                    now,
                    null
                ),
                Message.restore(
                    clock,
                    fixtureMessageId2,
                    Text,
                    fixtureImamId,
                    fixtureMessage,
                    null,
                    now,
                    null
                ),
                Message.restore(
                    clock,
                    fixtureMessageId3,
                    Audio,
                    fixtureImamId,
                    fixtureAudioText,
                    fixtureAudio,
                    now,
                    null
                ),
            ),

            clock = clock,
            eventPublisher = eventPublisher,
            getCurrentUser = getCurrentUser,
        )
    }

    fun fixtureSavedMessage(
        id: Message.Id = fixtureMessageId1,
        text: NonBlankString = fixtureMessage,
        userId: User.Id = fixtureInquirerId,
        createdAt: LocalDateTime = timeAfter(0),
        updatedAt: LocalDateTime? = null,
    ) =
        Message.restore(
            id = id,
            type = Text,
            authorId = userId,

            text = text,
            audio = null,

            createdAt = createdAt,
            updatedAt = updatedAt,

            clock = clock,
        )

    fun fixtureSavedAudioMessage() = Message.restore(
        id = fixtureMessageId2,
        type = Audio,
        authorId = fixtureImamId,

        text = fixtureAudioText,
        audio = fixtureAudio,

        createdAt = timeAfter(0),
        updatedAt = null,

        clock = clock,
    )

    val fixtureImam = User(User.Id(1), User.Type.Imam, NonBlankString.of("Imam"))
    val fixtureImamId = fixtureImam.id

    val fixtureInquirer = User(User.Id(2), User.Type.Inquirer, NonBlankString.of("Inquirer"))
    val fixtureInquirerId = fixtureInquirer.id

    val fixtureAnotherInquirer = User(User.Id(20), User.Type.Inquirer, NonBlankString.of("Some guy"))
    val fixtureAnotherImam = User(User.Id(30), User.Type.Imam, NonBlankString.of("Some imam"))

    val fixtureChatId = Chat.Id(1)

    val fixtureSubject = Subject(NonBlankString.of("Subject"))

    val fixtureMessageId1 = Message.Id(1)
    val fixtureMessageId2 = Message.Id(2)
    val fixtureMessageId3 = Message.Id(3)

    val fixtureMessage = NonBlankString.of("A message")
    val fixtureNewMessage = NonBlankString.of("A new message")
    val fixtureNewReply = NonBlankString.of("A new reply")

    val fixtureAudio = NonBlankString.of("audio.mp3")
    val fixtureAudioText = NonBlankString.of("Аудио")

    val fixtureNow: LocalDateTime = LocalDateTime.now(fixedClock)

    fun timeAfter(minutes: Long): LocalDateTime = fixtureNow.plusMinutes(minutes)
}
