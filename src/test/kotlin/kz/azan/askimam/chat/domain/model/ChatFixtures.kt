package kz.azan.askimam.chat.domain.model

import io.mockk.every
import io.mockk.mockk
import kz.azan.askimam.chat.app.projection.ChatProjection
import kz.azan.askimam.chat.app.usecase.GetChat
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
    val getChat = mockk<GetChat>()
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

            inquirerFcmToken = fixtureInquirerFcmToken,

            clock = clock,
            eventPublisher = eventPublisher,
            getCurrentUser = getCurrentUser,
        )
    }

    fun fixtureSavedChat(type: Chat.Type = Public, id: Chat.Id = fixtureChatId1): Chat {
        val subject = fixtureSubject
        val now = LocalDateTime.now(clock)

        return Chat.restore(
            id = id,
            type = type,
            subject = subject,

            askedBy = fixtureInquirerId,
            answeredBy = fixtureImamId,

            inquirerFcmToken = fixtureInquirerFcmToken,
            imamFcmToken = fixtureImamFcmToken,

            createdAt = now,
            updatedAt = now,

            isVisibleToPublic = type == Public,
            isViewedByImam = true,
            isViewedByInquirer = true,

            messages = listOf(
                Message.restore(
                    id = fixtureMessageId1,
                    type = Text,
                    text = fixtureMessage,
                    audio = null,

                    authorId = fixtureInquirerId,
                    authorType = fixtureInquirer.type,

                    createdAt = now,
                    updatedAt = null,

                    clock = clock,
                ),
                Message.restore(
                    id = fixtureMessageId2,
                    type = Text,
                    text = fixtureMessage,
                    audio = null,

                    authorId = fixtureImamId,
                    authorType = fixtureImam.type,

                    createdAt = now,
                    updatedAt = null,

                    clock = clock,
                ),
                Message.restore(
                    id = fixtureMessageId3,
                    type = Audio,
                    text = fixtureAudioText,
                    audio = fixtureAudio,

                    authorId = fixtureImamId,
                    authorType = fixtureImam.type,

                    createdAt = now,
                    updatedAt = null,

                    clock = clock,
                ),
            ),

            clock = clock,
            eventPublisher = eventPublisher,
            getCurrentUser = getCurrentUser,
        )
    }

    fun fixtureSavedTwoChats(type: Chat.Type = Public): List<Chat> {
        val subject = fixtureSubject
        val now = LocalDateTime.now(clock)
        val later = timeAfter(10)

        return listOf(
            Chat.restore(
                id = fixtureChatId1,
                type = type,
                subject = subject,

                askedBy = fixtureInquirerId,
                answeredBy = fixtureImamId,

                inquirerFcmToken = fixtureInquirerFcmToken,
                imamFcmToken = fixtureImamFcmToken,

                createdAt = now,
                updatedAt = now,

                isVisibleToPublic = type == Public,
                isViewedByImam = true,
                isViewedByInquirer = true,

                messages = listOf(
                    Message.restore(
                        id = fixtureMessageId1,
                        type = Text,
                        text = fixtureMessage,
                        audio = null,

                        authorId = fixtureInquirerId,
                        authorType = fixtureInquirer.type,

                        createdAt = now,
                        updatedAt = null,

                        clock = clock,
                    ),
                ),

                clock = clock,
                eventPublisher = eventPublisher,
                getCurrentUser = getCurrentUser,
            ),
            Chat.restore(
                id = fixtureChatId2,
                type = type,
                subject = null,

                askedBy = fixtureInquirerId,
                answeredBy = null,

                inquirerFcmToken = fixtureInquirerFcmToken,
                imamFcmToken = null,

                createdAt = later,
                updatedAt = later,

                isVisibleToPublic = type == Public,
                isViewedByImam = false,
                isViewedByInquirer = true,

                messages = listOf(
                    Message.restore(
                        id = fixtureMessageId1,
                        type = Text,
                        text = fixtureMessage,
                        audio = null,

                        authorId = fixtureInquirerId,
                        authorType = fixtureInquirer.type,

                        createdAt = later,
                        updatedAt = null,

                        clock = clock,
                    ),
                ),

                clock = clock,
                eventPublisher = eventPublisher,
                getCurrentUser = getCurrentUser,
            ),
        )
    }

    fun fixtureSavedMessage(
        id: Message.Id = fixtureMessageId1,
        text: NonBlankString = fixtureMessage,
        userId: User.Id = fixtureInquirerId,
        userType: User.Type = fixtureInquirer.type,
        createdAt: LocalDateTime = timeAfter(0),
        updatedAt: LocalDateTime? = null,
    ) =
        Message.restore(
            id = id,
            type = Text,
            text = text,
            audio = null,

            authorId = userId,
            authorType = userType,

            createdAt = createdAt,
            updatedAt = updatedAt,

            clock = clock,
        )

    fun fixtureSavedAudioMessage() = Message.restore(
        id = fixtureMessageId2,
        type = Audio,
        text = fixtureAudioText,
        audio = fixtureAudio,

        authorId = fixtureImamId,
        authorType = fixtureImam.type,

        createdAt = timeAfter(0),
        updatedAt = null,

        clock = clock,
    )

    val fixtureImam = User(User.Id(1), User.Type.Imam, NonBlankString.of("Imam"))
    val fixtureImamId = fixtureImam.id
    val fixtureImamFcmToken = FcmToken(NonBlankString.of("123"))

    val fixtureInquirer = User(User.Id(2), User.Type.Inquirer, NonBlankString.of("Inquirer"))
    val fixtureInquirerId = fixtureInquirer.id
    val fixtureInquirerFcmToken = FcmToken(NonBlankString.of("456"))

    val fixtureAnotherInquirer = User(User.Id(20), User.Type.Inquirer, NonBlankString.of("Some guy"))
    val fixtureAnotherImam = User(User.Id(30), User.Type.Imam, NonBlankString.of("Some imam"))

    val fixtureChatId1 = Chat.Id(1)
    val fixtureChatId2 = Chat.Id(2)

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

    fun listOfChatProjectionsFixture(): List<ChatProjection> {
        fixtureClock()
        return fixtureSavedTwoChats().map { ChatProjection(it.id!!, it.subjectText()) }
    }
}
