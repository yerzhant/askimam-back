package kz.azan.askimam.event.domain.service

import kz.azan.askimam.event.domain.model.Event

interface EventPublisher {
    fun publish(event: Event)
}
