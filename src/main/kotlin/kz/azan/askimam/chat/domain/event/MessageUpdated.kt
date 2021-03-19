package kz.azan.askimam.chat.domain.event

import kz.azan.askimam.chat.domain.model.Message
import kz.azan.askimam.common.domain.Event
import kz.azan.askimam.common.type.NotBlankString
import java.time.ZonedDateTime

data class MessageUpdated(
    val id: Message.Id,
    val text: NotBlankString,
    val updatedAt: ZonedDateTime,
) : Event()
