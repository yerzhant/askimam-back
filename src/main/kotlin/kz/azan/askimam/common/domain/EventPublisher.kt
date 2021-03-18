package kz.azan.askimam.common.domain

interface EventPublisher {
    fun publish(event: Event)
}

abstract class Event
