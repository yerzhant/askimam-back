package kz.azan.askimam.chat.domain.event

import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.event.domain.model.Event

data class ChatCreated(
    val subject: Subject?,
    val message: NonBlankString,
) : Event()
