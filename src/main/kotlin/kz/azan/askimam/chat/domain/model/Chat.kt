package kz.azan.askimam.chat.domain.model

import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.common.domain.Notifications
import kz.azan.askimam.common.type.NotBlankString

class Chat(
    private val notifications: Notifications,
    subject: NotBlankString?,
    firstMessage: NotBlankString,
) {
    private var subject: NotBlankString? = subject

    private val messages = mutableListOf<Message>()

//    private var updatedAt = Clock.systemDefaultZone()

    init {
        messages.add(Message(firstMessage))
        notifications.notify(ChatCreated(subject, firstMessage))
    }

    fun subject(): NotBlankString? = subject

    fun messages() = messages.toList()
}