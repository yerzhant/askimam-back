package kz.azan.askimam.chat.domain.model

import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.common.domain.EventPublisher
import kz.azan.askimam.common.type.NotBlankString
import kz.azan.askimam.imam.domain.model.ImamId
import kz.azan.askimam.inquirer.domain.model.InquirerId
import java.time.Clock
import java.time.ZonedDateTime

class Chat(
    val type: Type,
    private var subject: NotBlankString?,
    firstMessage: NotBlankString,
    val askedBy: InquirerId,
    private val clock: Clock,
    private val eventPublisher: EventPublisher,
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
                Message.Type.Text,
                Message.Sender.Inquirer,
                firstMessage,
                clock,
            )
        )
        eventPublisher.publish(ChatCreated(subject, firstMessage))
    }

    constructor(
        type: Type,
        firstMessage: NotBlankString,
        askedBy: InquirerId,
        clock: Clock,
        eventPublisher: EventPublisher,
    ) : this(type, null, firstMessage, askedBy, clock, eventPublisher)

    fun createdAt() = createdAt
    fun updatedAt() = updatedAt

    fun subject(): NotBlankString = subject ?: messages.first().text()

    fun messages() = messages.map {
        Message(
            it.createdAt,
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
        messages.add(
            MessageEntity(
                Message.Type.Text,
                sender,
                newMessage,
                clock,
            )
        )
        when (sender) {
            Message.Sender.Inquirer -> isViewedByImam = false
            Message.Sender.Imam -> isViewedByInquirer = false
        }
        eventPublisher.publish(MessageAdded(subject, newMessage))
    }

    fun isAnswered() = answeredBy != null
    fun isViewedByImam() = isViewedByImam
    fun isViewedByInquirer() = isViewedByInquirer
    fun answeredBy() = answeredBy

    fun viewedByImam() {
        isViewedByImam = true
    }

    fun viewedByInquirer() {
        isViewedByInquirer = true
    }

    enum class Type { Public, Private }
}

private class MessageEntity(
    val type: Message.Type,
    val sender: Message.Sender,
    private var text: NotBlankString,
    clock: Clock,
) {
    val createdAt = ZonedDateTime.now(clock)!!
    private var updatedAt: ZonedDateTime? = null

    fun updatedAt() = updatedAt

    fun text() = text
}