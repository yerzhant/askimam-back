package kz.azan.askimam.common.infra

import kz.azan.askimam.common.domain.Event
import kz.azan.askimam.common.domain.EventPublisher
import org.springframework.stereotype.Component

@Component
class LoggingEventPublisher : EventPublisher {
    override fun publish(event: Event) {
        println(event)
    }
}