package kz.azan.askimam.chat.domain.model

import io.mockk.every
import io.mockk.mockk
import io.vavr.kotlin.none
import io.vavr.kotlin.right
import kz.azan.askimam.chat.app.service.GetCurrentUser
import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.chat.domain.model.Chat.Type.Public
import kz.azan.askimam.chat.domain.model.Message.Type.Audio
import kz.azan.askimam.chat.domain.model.Message.Type.Text
import kz.azan.askimam.common.domain.EventPublisher
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.user.domain.model.User
import java.time.*

open class ChatFixtures {

    val eventPublisher = mockk<EventPublisher>()
    val getCurrentUser = mockk<GetCurrentUser>()
    val chatRepository = mockk<ChatRepository>()
    val messageRepository = mockk<MessageRepository>()

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
        every { chatRepository.generateId() } returns right(fixtureChatId)
        every { chatRepository.create(any()) } returns none()
        every { chatRepository.update(any()) } returns none()
        every { messageRepository.add(fixtureSavedMessage()) } returns none()
        every { messageRepository.generateId() } returnsMany listOf(
            right(fixtureMessageId1),
            right(fixtureMessageId2),
        )

        return Chat.newWithSubject(
            clock,
            eventPublisher,
            getCurrentUser,
            chatRepository,
            messageRepository,
            type,
            subject,
            firstMessage,
        ).get()
    }

    fun fixtureSavedChat(type: Chat.Type = Public): Chat {
        fixtureClock()
        val subject = fixtureSubject
        val now = ZonedDateTime.now(clock)

        return Chat.restore(
            clock,
            eventPublisher,
            getCurrentUser,
            chatRepository,
            messageRepository,
            fixtureChatId,
            type,
            fixtureInquirerId,
            now,
            now,
            subject,
            listOf(
                Message(
                    fixtureMessageId1,
                    Text,
                    now,
                    null,
                    fixtureInquirerId,
                    fixtureMessage,
                    null
                )
            ),
            isVisibleToPublic = type == Public,
            isViewedByImam = true,
            isViewedByInquirer = false,
        )
    }

    fun fixtureSavedMessage(
        id: Message.Id = fixtureMessageId1,
        text: NonBlankString = fixtureMessage,
        userId: User.Id = fixtureInquirerId,
        createdAt: ZonedDateTime = timeAfter(0),
        updatedAt: ZonedDateTime? = null,
    ) =
        Message(
            id,
            Text,
            createdAt,
            updatedAt,
            userId,
            text,
            null,
        )

    fun fixtureSavedAudioMessage() = Message(
        fixtureMessageId2,
        Audio,
        timeAfter(0),
        null,
        fixtureImamId,
        NonBlankString.of("Аудио"),
        fixtureAudio,
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

    val fixtureMessage = NonBlankString.of("A message")
    val fixtureNewMessage = NonBlankString.of("A new message")
    val fixtureNewReply = NonBlankString.of("A new reply")

    val fixtureAudio = NonBlankString.of("audio.mp3")

    val fixtureNow: ZonedDateTime = ZonedDateTime.now(fixedClock)

    fun timeAfter(minutes: Long): ZonedDateTime = fixtureNow.plusMinutes(minutes)
}
