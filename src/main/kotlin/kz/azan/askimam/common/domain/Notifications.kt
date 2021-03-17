package kz.azan.askimam.common.domain

interface Notifications {
    fun notify(event: Event)
}

abstract class Event
