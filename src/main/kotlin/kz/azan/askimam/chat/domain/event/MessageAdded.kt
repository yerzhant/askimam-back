package kz.azan.askimam.chat.domain.event

import kz.azan.askimam.common.domain.Event
import kz.azan.askimam.common.type.NotBlankString

data class MessageAdded(
    val subject: NotBlankString?,
    val text: NotBlankString,
) : Event()
