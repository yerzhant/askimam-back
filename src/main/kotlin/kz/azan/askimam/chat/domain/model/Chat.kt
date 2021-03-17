package kz.azan.askimam.chat.domain.model

import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.common.domain.Notifications
import kz.azan.askimam.common.type.NotBlankString
import kz.azan.askimam.imam.domain.model.ImamId
import java.time.Clock
import java.time.ZonedDateTime

class Chat(
    private val clock: Clock,
    private val notifications: Notifications,
    val type: Type,
    private var subject: NotBlankString?,
    firstMessage: NotBlankString,
) {
    private val createdAt = ZonedDateTime.now(clock)!!
    private var updatedAt = ZonedDateTime.now(clock)!!
    private var answeredBy: ImamId? = null
    private var isViewedByImam = false
    private var isViewedByInquirer = true
    private val messages = mutableListOf<MessageEntity>()

    init {
        messages.add(
            MessageEntity(
                clock,
                Message.Type.Text,
                Message.Sender.Inquirer,
                firstMessage,
            )
        )
        notifications.notify(ChatCreated(subject, firstMessage))
    }

    constructor(
        clock: Clock,
        notifications: Notifications,
        type: Type,
        firstMessage: NotBlankString
    ) : this(clock, notifications, type, null, firstMessage)

    fun createdAt() = createdAt
    fun updatedAt() = updatedAt

    fun subject(): NotBlankString = subject ?: messages.first().text()

    fun messages() = messages.map {
        Message(
            it.createdAt(),
            it.updatedAt(),
            it.type,
            it.sender,
            it.text()
        )
    }.toList()

    fun renameSubject(newSubject: NotBlankString) {
        subject = newSubject
    }

    fun addNewTextMessage(sender: Message.Sender, newMessage: NotBlankString) {
        messages.add(MessageEntity(clock, Message.Type.Text, sender, newMessage))
        when (sender) {
            Message.Sender.Inquirer -> isViewedByImam = false
            Message.Sender.Imam -> isViewedByInquirer = false
        }
        notifications.notify(MessageAdded(subject, newMessage))
    }

    fun isAnswered() = answeredBy != null
    fun isViewedByImam() = isViewedByImam
    fun isViewedByInquirer() = isViewedByInquirer

    fun viewedByImam() {
        isViewedByImam = true
    }

    fun viewedByInquirer() {
        isViewedByInquirer = true
    }

    enum class Type { Public, Private }
}

private class MessageEntity(
    clock: Clock,
    val type: Message.Type,
    val sender: Message.Sender,
    private var text: NotBlankString,
) {
    private val createdAt = ZonedDateTime.now(clock)!!
    private var updatedAt: ZonedDateTime? = null

    fun createdAt() = createdAt
    fun updatedAt() = updatedAt

    fun text() = text
}