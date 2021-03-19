package kz.azan.askimam.chat.domain.event

import kz.azan.askimam.chat.domain.model.Message
import kz.azan.askimam.common.domain.Event

data class MessageDeleted(val id: Message.Id) : Event()
