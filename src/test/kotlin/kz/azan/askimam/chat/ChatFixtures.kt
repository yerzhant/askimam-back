package kz.azan.askimam.chat

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.mockk.mockk
import io.vavr.control.Either
import kz.azan.askimam.chat.app.projection.ChatProjection
import kz.azan.askimam.chat.app.usecase.GetChat
import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.Chat.Type.Public
import kz.azan.askimam.chat.domain.model.FcmToken
import kz.azan.askimam.chat.domain.model.Message
import kz.azan.askimam.chat.domain.model.Message.Type.Audio
import kz.azan.askimam.chat.domain.model.Message.Type.Text
import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.chat.domain.repo.ChatRepository
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.event.domain.service.EventPublisher
import kz.azan.askimam.event.infra.service.FcmService
import kz.azan.askimam.favorite.app.usecase.GetMyFavorites
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.repo.UserRepository
import kz.azan.askimam.user.domain.service.GetCurrentUser
import java.time.*
import java.time.format.DateTimeFormatter

open class ChatFixtures {

    @MockkBean
    lateinit var fcmService: FcmService

    val eventPublisher = mockk<EventPublisher>()
    val getCurrentUser = mockk<GetCurrentUser>()
    val chatRepository = mockk<ChatRepository>()
    val userRepository = mockk<UserRepository>()
    val getMyFavorites = mockk<GetMyFavorites>()
    val getChat = mockk<GetChat>()

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
        every { eventPublisher.publish(MessageAdded(subject, newMessage, fixtureInquirerId)) } returns Unit
        every {
            eventPublisher.publish(
                MessageAdded(
                    subject,
                    newMessage,
                    fixtureInquirerId,
                    fixtureImamId,
                )
            )
        } returns Unit

        return Chat.newWithSubject(
            type = type,
            subject = subject,
            messageText = firstMessage,

            inquirerFcmToken = fixtureInquirerFcmToken,

            clock = clock,
            eventPublisher = eventPublisher,
            getCurrentUser = getCurrentUser,
        ).get()
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

                    authorId = fixtureInquirerId,
                    authorType = fixtureInquirer.type,

                    createdAt = now,

                    clock = clock,
                ),
                Message.restore(
                    id = fixtureMessageId2,
                    type = Text,
                    text = fixtureMessage,

                    authorId = fixtureImamId,
                    authorType = fixtureImam.type,

                    createdAt = now,

                    clock = clock,
                ),
                Message.restore(
                    id = fixtureMessageId3,
                    type = Audio,
                    text = fixtureAudioText,
                    audio = fixtureAudio,
                    duration = fixtureAudioDuration,

                    authorId = fixtureImamId,
                    authorType = fixtureImam.type,

                    createdAt = now,

                    clock = clock,
                ),
            ),

            clock = clock,
            eventPublisher = eventPublisher,
            getCurrentUser = getCurrentUser,
        )
    }

    fun fixtureSavedTwoChats(type: Chat.Type = Public, message2: NonBlankString = fixtureMessage): List<Chat> {
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

                        authorId = fixtureInquirerId,
                        authorType = fixtureInquirer.type,

                        createdAt = now,

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
                        text = message2,

                        authorId = fixtureInquirerId,
                        authorType = fixtureInquirer.type,

                        createdAt = later,

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
        duration = fixtureAudioDuration,

        authorId = fixtureImamId,
        authorType = fixtureImam.type,

        createdAt = timeAfter(0),

        clock = clock,
    )

    @Suppress("SpellCheckingInspection")
    val fixturePasswordHash =
        NonBlankString.of("\$2y\$12\$CvQSK0JEQkkJN0T2GXDcduyIoeROqxryB3FIbc26GsXN4zy3CqDSG") // password

    val fixtureImam = User(User.Id(1), User.Type.Imam, NonBlankString.of("Imam"), fixturePasswordHash)
    val fixtureImamId = fixtureImam.id
    val fixtureImamFcmToken = FcmToken(NonBlankString.of("123"))

    val fixtureInquirerFcmToken = FcmToken(NonBlankString.of("456"))
    val fixtureInquirer = User(
        id = User.Id(2),
        type = User.Type.Inquirer,
        name = NonBlankString.of("Inquirer"),
        passwordHash = fixturePasswordHash,
        fcmTokens = setOf(fixtureInquirerFcmToken).toMutableSet(),
    )
    val fixtureInquirerId = fixtureInquirer.id

    val fixtureAnotherInquirer =
        User(User.Id(20), User.Type.Inquirer, NonBlankString.of("Some guy"), fixturePasswordHash)
    val fixtureAnotherImam = User(User.Id(30), User.Type.Imam, NonBlankString.of("Some imam"), fixturePasswordHash)

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
    val fixtureAudioDuration = NonBlankString.of("01:23")

    val fixtureNow: LocalDateTime = LocalDateTime.now(fixedClock)

    fun timeAfter(minutes: Long): LocalDateTime = fixtureNow.plusMinutes(minutes)

    fun timeAsString(): String = fixtureNow.format(DateTimeFormatter.ISO_DATE_TIME)

    fun listOfChatProjectionsFixture(): List<ChatProjection> {
        fixtureClock()
        return Either.sequenceRight(
            fixtureSavedTwoChats().map { ChatProjection.from(it, userRepository) }
        ).get().toJavaList()
    }
}
