package kz.azan.askimam.chat.domain.model

import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.chat.domain.event.MessageDeleted
import kz.azan.askimam.chat.domain.event.MessageUpdated
import kz.azan.askimam.chat.domain.model.Message.Type.Audio
import kz.azan.askimam.chat.domain.model.Message.Type.Text
import kz.azan.askimam.common.domain.EventPublisher
import kz.azan.askimam.common.type.NotBlankString
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.User.Type.Imam
import kz.azan.askimam.user.domain.model.User.Type.Inquirer
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
    private var isVisibleToPublic = false
    private var isViewedByImam = false
    private var isViewedByInquirer = true
    private val messages = mutableListOf<MessageEntity>()

    init {
        messages.add(
            MessageEntity(
                clock,
                messageId,
                Text,
                askedBy,
                messageText,
            )
        )
        eventPublisher.publish(ChatCreated(subject, messageText))
    }

    fun createdAt() = createdAt
    fun updatedAt() = updatedAt

    fun isVisibleToPublic() = isVisibleToPublic
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
            it.authorId,
            it.text(),
            it.audio,
        )
    }.toList()

    fun renameSubject(newSubject: NotBlankString) {
        subject = newSubject
    }

    fun addTextMessage(id: Message.Id, text: NotBlankString, author: User) {
        addMessage(id, Text, author, text)
    }

    fun addAudioMessage(id: Message.Id, audio: NotBlankString, author: User) {
        addMessage(id, Audio, author, NotBlankString.of("Аудио"), audio)
    }

    private fun addMessage(
        id: Message.Id,
        messageType: Message.Type,
        author: User,
        text: NotBlankString,
        audio: NotBlankString? = null,
    ) {
        messages.add(
            MessageEntity(
                clock,
                id,
                messageType,
                author.id,
                text,
                audio,
            )
        )

        when (author.type) {
            Inquirer -> isViewedByImam = false
            Imam -> {
                if (type == Type.Public) isVisibleToPublic = true
                isViewedByInquirer = false
            }
        }

        eventPublisher.publish(MessageAdded(subject, text))
    }

    fun deleteMessage(id: Message.Id) {
        messages.removeIf { it.id == id }
        eventPublisher.publish(MessageDeleted(id))
    }

    fun updateTextMessageByInquirer(id: Message.Id, text: NotBlankString) {
        messages.find { it.id == id }?.run {
            updateText(text)
            eventPublisher.publish(MessageUpdated(id, text, updatedAt()!!))
        }
    }

    fun updateTextMessageByImam(id: Message.Id, text: NotBlankString, imamId: User.Id) {
        messages.find { it.id == id && it.authorId == imamId }?.run {
            updateText(text)
            eventPublisher.publish(MessageUpdated(id, text, updatedAt()!!))
        }
    }

    enum class Type { Public, Private }
}

private class MessageEntity(
    private val clock: Clock,
    val id: Message.Id,
    val type: Message.Type,
    val authorId: User.Id,
    private var text: NotBlankString,
    val audio: NotBlankString? = null,
) {
    val createdAt = ZonedDateTime.now(clock)!!
    private var updatedAt: ZonedDateTime? = null

    fun updatedAt() = updatedAt

    fun text() = text

    fun updateText(text: NotBlankString) {
        this.text = text
        updatedAt = ZonedDateTime.now(clock)
    }
}