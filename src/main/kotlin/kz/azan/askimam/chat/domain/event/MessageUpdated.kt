package kz.azan.askimam.chat.domain.event

import kz.azan.askimam.chat.domain.model.Message
import kz.azan.askimam.common.domain.Event
import kz.azan.askimam.common.type.NonBlankString
import java.time.LocalDateTime

data class MessageUpdated(
    val id: Message.Id,
    val text: NonBlankString,
    val updatedAt: LocalDateTime,
) : Event()
