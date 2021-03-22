package kz.azan.askimam.chat.domain.event

import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.common.domain.Event
import kz.azan.askimam.common.type.NonBlankString

data class MessageAdded(
    val subject: Subject?,
    val text: NonBlankString,
) : Event()
