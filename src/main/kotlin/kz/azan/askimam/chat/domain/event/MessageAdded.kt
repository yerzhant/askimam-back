package kz.azan.askimam.chat.domain.event

import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.event.domain.model.Event
import kz.azan.askimam.user.domain.model.User

data class MessageAdded(
    val subject: Subject?,
    val text: NonBlankString,
    val userIdToBeNotified: User.Id? = null,
    val answeredImamId: User.Id? = null,
) : Event()
