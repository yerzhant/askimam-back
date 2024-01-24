package kz.azan.askimam.event.infra.service

import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.chat.domain.event.MessageDeleted
import kz.azan.askimam.event.domain.model.Event
import kz.azan.askimam.event.domain.service.EventPublisher
import kz.azan.askimam.imamrating.app.usecase.IncreaseImamsRating
import kz.azan.askimam.user.domain.repo.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import java.io.File

@Component
class EventProcessor(
    private val fcmService: FcmService,
    private val userRepository: UserRepository,
    private val increaseImamsRating: IncreaseImamsRating,
    private val getImamsFcmTokens: GetImamsFcmTokensService,
) : EventPublisher {

    private val logger = LoggerFactory.getLogger(javaClass)

    @Async
    override fun publish(event: Event) {
        when (event) {
            is ChatCreated -> onNewQuestion(event)
            is MessageAdded -> onNewMessage(event)
            is MessageDeleted -> onDeleteMessage(event)
        }
    }

    private fun onNewMessage(event: MessageAdded) {
        sendNotification(event)

        event.answeredImamId?.let { increaseImamsRating(it) }
    }

    private fun sendNotification(event: MessageAdded) {
        if (event.userIdToBeNotified != null) {
            userRepository.findById(event.userIdToBeNotified).fold(
                { logger.error("Notification not sent: ${it.reason.value}") },
                { user ->
                    user.fcmTokens.map { it.value.value }.run {
                        fcmService.notify(
                            this,
                            event.subject,
                            event.text,
                        )
                    }
                }
            )
        }
    }

    private fun onNewQuestion(event: ChatCreated) {
        fcmService.notify(
            getImamsFcmTokens(),
            event.subject,
            event.message,
        )
    }

    private fun onDeleteMessage(event: MessageDeleted) {
        if (event.audio != null) {
            try {
                File("./audio/${event.audio.value}").delete()
            } catch (e: Exception) {
                logger.error("The audio file ${event.audio.value} was not deleted: ${e.message}.")
            }
        }
    }
}
