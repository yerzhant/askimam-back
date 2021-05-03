package kz.azan.askimam.chat.domain.event

import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.event.domain.model.Event

data class MessageAdded(
    val subject: Subject?,
    val text: NonBlankString,
) : Event()
