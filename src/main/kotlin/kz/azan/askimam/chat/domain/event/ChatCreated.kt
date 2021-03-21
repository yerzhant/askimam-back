package kz.azan.askimam.chat.domain.event

import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.common.domain.Event
import kz.azan.askimam.common.type.NotBlankString

data class ChatCreated(
    val subject: Subject?,
    val message: NotBlankString,
) : Event()
