package kz.azan.askimam.event.infra.service

import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.event.domain.model.Event
import kz.azan.askimam.event.domain.service.EventPublisher
import kz.azan.askimam.user.domain.repo.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
@Suppress("unused")
class NotificationEventPublisher(
    private val fcmService: FcmService,
    private val userRepository: UserRepository,
    private val getImamsFcmTokens: GetImamsFcmTokensService,
) : EventPublisher {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun publish(event: Event) {
        when (event) {
            is ChatCreated -> onNewQuestion(event)
            is MessageAdded -> onNewMessage(event)
        }
    }

    private fun onNewMessage(event: MessageAdded) {
        if (event.addressee != null) {
            userRepository.findById(event.addressee).fold(
                { logger.error("Notification not sent: ${it.reason.value}") },
                { user ->
                    user.fcmTokens.map { it.value.value }.run {
                        if (isNotEmpty()) {
                            fcmService.notify(
                                this,
                                event.subject,
                                event.text,
                            )
                        }
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
}