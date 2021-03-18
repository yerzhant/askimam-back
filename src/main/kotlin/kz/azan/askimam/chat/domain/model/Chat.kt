package kz.azan.askimam.chat.domain.model

import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.chat.domain.model.Message.Sender.Imam
import kz.azan.askimam.chat.domain.model.Message.Sender.Inquirer
import kz.azan.askimam.chat.domain.model.Message.Type.Audio
import kz.azan.askimam.chat.domain.model.Message.Type.Text
import kz.azan.askimam.common.domain.EventPublisher
import kz.azan.askimam.common.type.NotBlankString
import kz.azan.askimam.imam.domain.model.ImamId
import kz.azan.askimam.inquirer.domain.model.InquirerId
import java.time.Clock
import java.time.ZonedDateTime

class Chat(
    private val clock: Clock,
    private val eventPublisher: EventPublisher,
    val type: Type,
    val askedBy: InquirerId,
    firstMessage: NotBlankString,
    private var subject: NotBlankString? = null,
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
                Text,
                Inquirer,
                firstMessage,
            )
        )
        eventPublisher.publish(ChatCreated(subject, firstMessage))
    }

    fun createdAt() = createdAt
    fun updatedAt() = updatedAt

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

    fun subject(): NotBlankString = subject ?: messages.first().text()

    fun messages() = messages.map {
        Message(
            it.createdAt,
            it.updatedAt(),
            it.type,
            it.sender,
            it.text(),
            it.audio,
        )
    }.toList()

    fun renameSubject(newSubject: NotBlankString) {
        subject = newSubject
    }

    fun addNewTextMessage(sender: Message.Sender, text: NotBlankString) {
        addNewMessage(Text, sender, text)
    }

    fun addNewAudioMessage(sender: Message.Sender, audio: NotBlankString) {
        addNewMessage(Audio, sender, NotBlankString.of("Аудио"), audio)
    }

    private fun addNewMessage(
        type: Message.Type,
        sender: Message.Sender,
        text: NotBlankString,
        audio: NotBlankString? = null,
    ) {
        messages.add(
            MessageEntity(
                clock,
                type,
                sender,
                text,
                audio,
            )
        )

        when (sender) {
            Inquirer -> isViewedByImam = false
            Imam -> isViewedByInquirer = false
        }

        eventPublisher.publish(MessageAdded(subject, text))
    }

    enum class Type { Public, Private }
}

private class MessageEntity(
    clock: Clock,
    val type: Message.Type,
    val sender: Message.Sender,
    private var text: NotBlankString,
    val audio: NotBlankString? = null,
) {
    val createdAt = ZonedDateTime.now(clock)!!
    private var updatedAt: ZonedDateTime? = null

    fun updatedAt() = updatedAt

    fun text() = text
}