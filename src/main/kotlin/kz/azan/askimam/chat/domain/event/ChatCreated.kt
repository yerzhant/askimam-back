package kz.azan.askimam.chat.domain.event

import kz.azan.askimam.common.domain.Event
import kz.azan.askimam.common.type.NotBlankString

data class ChatCreated(
    val subject: NotBlankString?,
    val message: NotBlankString,
) : Event()
