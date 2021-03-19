package kz.azan.askimam.chat.domain.model

import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.chat.domain.event.MessageDeleted
import kz.azan.askimam.chat.domain.model.Message.Sender.Imam
import kz.azan.askimam.chat.domain.model.Message.Sender.Inquirer
import kz.azan.askimam.chat.domain.model.Message.Type.Audio
import kz.azan.askimam.chat.domain.model.Message.Type.Text
import kz.azan.askimam.common.domain.EventPublisher
import kz.azan.askimam.common.type.NotBlankString
import kz.azan.askimam.user.domain.model.User
import java.time.Clock
import java.time.ZonedDateTime

class Chat(
    private val clock: Clock,
    private val eventPublisher: EventPublisher,
    val type: Type,
    val askedBy: User.Id,
    messageId: Message.Id,
    messageText: NotBlankString,
    private var subject: NotBlankString? = null,
) {
    private val createdAt = ZonedDateTime.now(clock)!!
    private var updatedAt = ZonedDateTime.now(clock)!!
    private var isViewedByImam = false
    private var isViewedByInquirer = true
    private val messages = mutableListOf<MessageEntity>()

    init {
        messages.add(
            MessageEntity(
                clock,
                messageId,
                Text,
                Inquirer,
                messageText,
            )
        )
        eventPublisher.publish(ChatCreated(subject, messageText))
    }

    fun createdAt() = createdAt
    fun updatedAt() = updatedAt

    fun isAnswered() = messages.any { it.answeredBy() != null }
    fun isViewedByImam() = isViewedByImam
    fun isViewedByInquirer() = isViewedByInquirer

    fun viewedByImam() {
        isViewedByImam = true
    }

    fun viewedByInquirer() {
        isViewedByInquirer = true
    }

    fun subject(): NotBlankString = subject ?: messages.first().text()

    fun messages() = messages.map {
        Message(
            it.id,
            it.createdAt,
            it.updatedAt(),
            it.type,
            it.sender,
            it.text(),
            it.audio,
            it.answeredBy(),
        )
    }.toList()

    fun renameSubject(newSubject: NotBlankString) {
        subject = newSubject
    }

    fun addTextMessageByInquirer(id: Message.Id, text: NotBlankString) {
        addMessage(id, Text, Inquirer, text)
    }

    fun addTextMessageByImam(id: Message.Id, text: NotBlankString, answeredBy: User.Id) {
        addMessage(id, Text, Imam, text, answeredBy = answeredBy)
    }

    fun addAudioMessage(id: Message.Id, sender: Message.Sender, audio: NotBlankString, answeredBy: User.Id) {
        addMessage(id, Audio, sender, NotBlankString.of("Аудио"), audio, answeredBy)
    }

    private fun addMessage(
        id: Message.Id,
        type: Message.Type,
        sender: Message.Sender,
        text: NotBlankString,
        audio: NotBlankString? = null,
        answeredBy: User.Id? = null,
    ) {
        messages.add(
            MessageEntity(
                clock,
                id,
                type,
                sender,
                text,
                audio,
                answeredBy,
            )
        )

        when (sender) {
            Inquirer -> isViewedByImam = false
            Imam -> isViewedByInquirer = false
        }

        eventPublisher.publish(MessageAdded(subject, text))
    }

    fun deleteMessage(id: Message.Id) {
        messages.removeIf { it.id == id }
        eventPublisher.publish(MessageDeleted(id))
    }

    fun updateTextMessageByInquirer(id: Message.Id, text: NotBlankString) {
        messages.find { it.id == id && it.sender == Inquirer }?.run {
            updateText(text)
        }
    }

    fun updateTextMessageByImam(id: Message.Id, text: NotBlankString, imamId: User.Id) {
        messages.find { it.id == id && it.sender == Imam && it.answeredBy() == imamId }?.run {
            updateText(text)
        }
    }

    enum class Type { Public, Private }
}

private class MessageEntity(
    private val clock: Clock,
    val id: Message.Id,
    val type: Message.Type,
    val sender: Message.Sender,
    private var text: NotBlankString,
    val audio: NotBlankString? = null,
    private var answeredBy: User.Id? = null,
) {
    val createdAt = ZonedDateTime.now(clock)!!
    private var updatedAt: ZonedDateTime? = null

    fun updatedAt() = updatedAt

    fun text() = text
    fun answeredBy() = answeredBy

    fun updateText(text: NotBlankString) {
        this.text = text
        updatedAt = ZonedDateTime.now(clock)
    }
}