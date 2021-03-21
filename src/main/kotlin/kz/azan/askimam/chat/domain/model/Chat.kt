package kz.azan.askimam.chat.domain.model

import io.vavr.control.Option
import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.chat.domain.event.MessageDeleted
import kz.azan.askimam.chat.domain.event.MessageUpdated
import kz.azan.askimam.chat.domain.model.Chat.Type.Public
import kz.azan.askimam.chat.domain.model.Message.Type.Audio
import kz.azan.askimam.chat.domain.model.Message.Type.Text
import kz.azan.askimam.chat.domain.policy.AddMessagePolicy
import kz.azan.askimam.chat.domain.policy.DeleteMessagePolicy
import kz.azan.askimam.chat.domain.policy.UpdateChatPolicy
import kz.azan.askimam.chat.domain.policy.UpdateMessagePolicy
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.common.domain.EventPublisher
import kz.azan.askimam.common.type.NotBlankString
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.User.Type.Imam
import kz.azan.askimam.user.domain.model.User.Type.Inquirer
import java.time.Clock
import java.time.ZonedDateTime

class Chat private constructor(
    private val clock: Clock,
    private val eventPublisher: EventPublisher,

    val type: Type,
    val askedBy: User.Id,

    private val createdAt: ZonedDateTime,
    private var updatedAt: ZonedDateTime,

    private var subject: Subject? = null,

    private val messages: MutableList<MessageEntity> = mutableListOf(),

    private var isVisibleToPublic: Boolean = false,
    private var isViewedByImam: Boolean = false,
    private var isViewedByInquirer: Boolean = true,
) {
    private fun init(messageId: Message.Id, messageText: NotBlankString) {
        messages.add(
            MessageEntity.newText(
                clock,
                messageId,
                askedBy,
                messageText,
            )
        )
        eventPublisher.publish(ChatCreated(subject, messageText))
    }

    fun createdAt() = createdAt
    fun updatedAt() = updatedAt

    fun isVisibleToPublic() = isVisibleToPublic && type == Public
    fun isViewedByImam() = isViewedByImam
    fun isViewedByInquirer() = isViewedByInquirer

    fun viewedByImam() {
        isViewedByImam = true
    }

    fun viewedByInquirer() {
        isViewedByInquirer = true
    }

    fun subjectText(): NotBlankString = subject?.value ?: messages.first().text()

    fun messages() = messages.map {
        Message(
            it.id,
            it.type,
            it.createdAt,
            it.updatedAt(),
            it.authorId,
            it.text(),
            it.audio,
        )
    }.toList()

    fun updateSubject(newSubject: Subject, policy: UpdateChatPolicy, user: User): Option<Declination> =
        policy.isAllowed(this, user).onEmpty { subject = newSubject }

    fun addTextMessage(
        policy: AddMessagePolicy,
        id: Message.Id,
        text: NotBlankString,
        author: User
    ): Option<Declination> {
        val message = MessageEntity.newText(clock, id, author.id, text)
        return addMessage(message, policy, author)
    }

    fun addAudioMessage(
        policy: AddMessagePolicy,
        id: Message.Id,
        audio: NotBlankString,
        imam: User
    ): Option<Declination> {
        val message = MessageEntity.newAudio(clock, id, imam.id, audio)
        return addMessage(message, policy, imam)
    }

    private fun addMessage(
        message: MessageEntity,
        policy: AddMessagePolicy,
        user: User
    ): Option<Declination> =
        policy.isAllowed(this, user).onEmpty {
            updatedAt = ZonedDateTime.now(clock)

            messages.add(message)

            when (user.type) {
                Inquirer -> isViewedByImam = false
                Imam -> {
                    if (type == Public) isVisibleToPublic = true
                    isViewedByInquirer = false
                }
            }

            eventPublisher.publish(MessageAdded(subject, message.text()))
        }

    fun deleteMessage(id: Message.Id, policy: DeleteMessagePolicy, user: User) =
        messages.find { it.id == id }?.run {
            policy.isAllowed(authorId, user).onEmpty {
                messages.remove(this)
                eventPublisher.publish(MessageDeleted(id))
            }
        } as Option<Declination>

    fun updateTextMessage(id: Message.Id, user: User, text: NotBlankString, policy: UpdateMessagePolicy) =
        messages.find { it.id == id }?.run {
            policy.isAllowed(authorId, user).onEmpty {
                updateText(text)
                eventPublisher.publish(MessageUpdated(id, text, updatedAt()!!))
            }
        } as Option<Declination>

    companion object {
        fun newWithSubject(
            clock: Clock,
            eventPublisher: EventPublisher,
            type: Type,
            askedBy: User.Id,
            subject: Subject,
            messageId: Message.Id,
            messageText: NotBlankString,
        ): Chat {
            val now = ZonedDateTime.now(clock)
            return Chat(clock, eventPublisher, type, askedBy, now, now, subject).apply {
                init(messageId, messageText)
            }
        }

        fun new(
            clock: Clock,
            eventPublisher: EventPublisher,
            type: Type,
            askedBy: User.Id,
            messageId: Message.Id,
            messageText: NotBlankString,
        ): Chat {
            val now = ZonedDateTime.now(clock)
            return Chat(clock, eventPublisher, type, askedBy, now, now).apply {
                init(messageId, messageText)
            }
        }

        fun restore(
            clock: Clock,
            eventPublisher: EventPublisher,
            type: Type,
            askedBy: User.Id,
            createdAt: ZonedDateTime,
            updatedAt: ZonedDateTime,
            subject: Subject?,
            messages: List<Message>,
            isVisibleToPublic: Boolean,
            isViewedByImam: Boolean,
            isViewedByInquirer: Boolean,
        ) = Chat(
            clock,
            eventPublisher,
            type,
            askedBy,
            createdAt,
            updatedAt,
            subject,
            messages.map { MessageEntity.restore(clock, it) }.toMutableList(),
            isVisibleToPublic,
            isViewedByImam,
            isViewedByInquirer,
        )
    }

    data class Id(val value: Long)
    enum class Type { Public, Private }
}

private class MessageEntity private constructor(
    private val clock: Clock,

    val id: Message.Id,
    val type: Message.Type,
    val authorId: User.Id,

    private var text: NotBlankString,

    val audio: NotBlankString? = null,

    val createdAt: ZonedDateTime = ZonedDateTime.now(clock),
    private var updatedAt: ZonedDateTime? = null,
) {
    fun updatedAt() = updatedAt

    fun text() = text

    fun updateText(text: NotBlankString) {
        this.text = text
        updatedAt = ZonedDateTime.now(clock)
    }

    companion object {
        fun newText(
            clock: Clock,
            id: Message.Id,
            authorId: User.Id,
            text: NotBlankString,
        ) = MessageEntity(clock, id, Text, authorId, text)

        fun newAudio(
            clock: Clock,
            id: Message.Id,
            authorId: User.Id,
            audio: NotBlankString,
        ): MessageEntity {
            val text = NotBlankString.of("Аудио")
            return MessageEntity(clock, id, Audio, authorId, text, audio)
        }

        fun restore(clock: Clock, message: Message) =
            MessageEntity(
                clock,
                message.id,
                message.type,
                message.authorId,
                message.text,
                message.audio,
                message.createdAt,
                message.updatedAt
            )
    }
}