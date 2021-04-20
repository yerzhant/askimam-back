package kz.azan.askimam.common.domain

// TODO: send notification not to imam's topic, but to all (or some) of them by their token ids
interface EventPublisher {
    fun publish(event: Event)
}

abstract class Event
