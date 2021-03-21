package kz.azan.askimam.chat.domain.model

import io.vavr.control.Option
import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.chat.domain.event.MessageDeleted
import kz.azan.askimam.chat.domain.event.MessageUpdated
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

    fun renameSubject(newSubject: NotBlankString, policy: UpdateChatPolicy, user: User): Option<Declination> =
        policy.isAllowed(this, user).onEmpty { subject = newSubject }

    fun addTextMessage(policy: AddMessagePolicy, id: Message.Id, text: NotBlankString, author: User) =
        addMessage(policy, id, Text, author, text)

    fun addAudioMessage(policy: AddMessagePolicy, id: Message.Id, audio: NotBlankString, imam: User) =
        addMessage(policy, id, Audio, imam, NotBlankString.of("Аудио"), audio)

    private fun addMessage(
        policy: AddMessagePolicy,
        id: Message.Id,
        messageType: Message.Type,
        author: User,
        text: NotBlankString,
        audio: NotBlankString? = null,
    ): Option<Declination> =
        policy.isAllowed(this, author).onEmpty {
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