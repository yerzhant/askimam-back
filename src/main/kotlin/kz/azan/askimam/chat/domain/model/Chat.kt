package kz.azan.askimam.chat.domain.model

import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.chat.domain.event.MessageAdded
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
    firstMessage: NotBlankString,
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
                Text,
                Inquirer,
                firstMessage,
            )
        )
        eventPublisher.publish(ChatCreated(subject, firstMessage))
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

    fun addNewTextMessageByInquirer(text: NotBlankString) {
        addNewMessage(Text, Inquirer, text)
    }

    fun addNewTextMessageByImam(text: NotBlankString, answeredBy: User.Id) {
        addNewMessage(Text, Imam, text, answeredBy = answeredBy)
    }

    fun addNewAudioMessage(sender: Message.Sender, audio: NotBlankString, answeredBy: User.Id) {
        addNewMessage(Audio, sender, NotBlankString.of("Аудио"), audio, answeredBy)
    }

    private fun addNewMessage(
        type: Message.Type,
        sender: Message.Sender,
        text: NotBlankString,
        audio: NotBlankString? = null,
        answeredBy: User.Id? = null,
    ) {
        messages.add(
            MessageEntity(
                clock,
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

    enum class Type { Public, Private }
}

private class MessageEntity(
    clock: Clock,
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
}