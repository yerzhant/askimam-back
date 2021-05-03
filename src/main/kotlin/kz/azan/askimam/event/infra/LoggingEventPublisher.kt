package kz.azan.askimam.event.infra

import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.event.domain.model.Event
import kz.azan.askimam.event.domain.service.EventPublisher
import org.springframework.stereotype.Component

@Component
@Suppress("unused")
class LoggingEventPublisher(
    private val fcmService: FcmService,
    private val getImamsFcmTokens: GetImamsFcmTokensService,
) : EventPublisher {

    override fun publish(event: Event) {
        when (event) {
            is ChatCreated -> fcmService.notify(
                getImamsFcmTokens(),
                event.subject,
                event.message,
            )

            is MessageAdded -> TODO()
        }
    }
}
