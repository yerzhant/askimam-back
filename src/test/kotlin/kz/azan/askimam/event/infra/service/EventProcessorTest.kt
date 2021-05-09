package kz.azan.askimam.event.infra.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.vavr.kotlin.none
import io.vavr.kotlin.right
import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.chat.domain.model.FcmToken
import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.imamrating.app.usecase.IncreaseImamsRating
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.repo.UserRepository
import org.junit.jupiter.api.Test

internal class EventProcessorTest {

    private val fcmService = mockk<FcmService>()

    private val userRepository = mockk<UserRepository>()

    private val getImamsFcmTokensService = mockk<GetImamsFcmTokensService>()

    private val increaseImamsRating = mockk<IncreaseImamsRating>()

    private val underTest = EventProcessor(
        fcmService,
        userRepository,
        increaseImamsRating,
        getImamsFcmTokensService,
    )

    private val subject = Subject.from("subject")

    private val message = NonBlankString.of("text")

    private val imamId = User.Id(1)

    @Test
    internal fun `should send notification on a new question`() {
        every { getImamsFcmTokensService() } returns listOf("123", "456")
        every { fcmService.notify(any(), any(), any()) } returns Unit

        underTest.publish(ChatCreated(subject, message))

        verify { fcmService.notify(listOf("123", "456"), subject, message) }
    }

    @Test
    internal fun `should send notification on a new message was added`() {
        newMessageFixture()

        underTest.publish(MessageAdded(subject, message, imamId))

        verify { fcmService.notify(listOf("123"), subject, message) }
    }

    private fun newMessageFixture() {
        every { userRepository.findById(imamId) } returns right(
            User(
                imamId,
                User.Type.Imam,
                NonBlankString.of("Name"),
                NonBlankString.of("x"),
                mutableSetOf(FcmToken.from("123")),
            )
        )
        every { fcmService.notify(any(), any(), any()) } returns Unit
    }

    @Test
    internal fun `should not send notification on a new message was added`() {
        underTest.publish(MessageAdded(subject, message, null))

        verify(exactly = 0) { fcmService.notify(any(), any(), any()) }
    }

    @Test
    internal fun `should increase imam's rating`() {
        every { increaseImamsRating(imamId) } returns none()

        underTest.publish(MessageAdded(subject, message, null, imamId))

        verify { increaseImamsRating(imamId) }
    }

    @Test
    internal fun `should not increase imam's rating`() {
        newMessageFixture()

        underTest.publish(MessageAdded(subject, message, imamId))

        verify(exactly = 0) { increaseImamsRating(any()) }
    }
}
