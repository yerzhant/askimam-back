package kz.azan.askimam.chat.domain.event

import kz.azan.askimam.chat.domain.model.Message
import kz.azan.askimam.event.domain.model.Event

data class MessageDeleted(val id: Message.Id) : Event()
