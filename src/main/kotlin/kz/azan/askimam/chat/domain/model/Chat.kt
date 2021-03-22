package kz.azan.askimam.chat.domain.model

import io.vavr.control.Either
import io.vavr.control.Option
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import io.vavr.kotlin.some
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
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.User.Type.Imam
import kz.azan.askimam.user.domain.model.User.Type.Inquirer
import java.time.Clock
import java.time.ZonedDateTime

class Chat private constructor(
    private val clock: Clock,
    private val eventPublisher: EventPublisher,
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,

    val id: Id,
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
    private fun init(messageText: NonBlankString) = messageRepository.generateId().fold(
        { some(it) },
        {
            chatRepository.create(this).orElse {
                val message = MessageEntity.newText(clock, it, askedBy, messageText)

                messageRepository.add(message.toMessage()).onEmpty {
                    messages.add(message)
                    eventPublisher.publish(ChatCreated(subject, messageText))
                }
            }
        }
    )

    fun createdAt() = createdAt
    fun updatedAt() = updatedAt

    fun isVisibleToPublic() = isVisibleToPublic && type == Public
    fun isViewedByImam() = isViewedByImam
    fun isViewedByInquirer() = isViewedByInquirer

    fun viewedByImam(user: User): Option<Declination> =
        UpdateChatPolicy.forImam.isAllowed(this, user).orElse {
            isViewedByImam = true
            chatRepository.update(this)
        }

    fun viewedByInquirer(user: User): Option<Declination> =
        UpdateChatPolicy.forInquirer.isAllowed(this, user).orElse {
            isViewedByInquirer = true
            chatRepository.update(this)
        }

    fun subject() = subject

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

    fun updateSubject(newSubject: Subject, user: User): Option<Declination> =
        UpdateChatPolicy.getFor(user).isAllowed(this, user).orElse {
            subject = newSubject
            chatRepository.update(this)
        }

    fun addTextMessage(
        text: NonBlankString,
        author: User
    ): Option<Declination> = messageRepository.generateId().fold(
        { some(it) },
        {
            val message = MessageEntity.newText(clock, it, author.id, text)
            addMessage(message, AddMessagePolicy.getFor(author), author)
        }
    )

    fun addAudioMessage(
        audio: NonBlankString,
        imam: User
    ): Option<Declination> = messageRepository.generateId().fold(
        { some(it) },
        {
            val message = MessageEntity.newAudio(clock, it, imam.id, audio)
            addMessage(message, AddMessagePolicy.forImam, imam)
        }
    )

    private fun addMessage(
        messageEntity: MessageEntity,
        policy: AddMessagePolicy,
        user: User
    ): Option<Declination> =
        policy.isAllowed(this, user).orElse {
            updatedAt = ZonedDateTime.now(clock)

            when (user.type) {
                Inquirer -> isViewedByImam = false
                Imam -> {
                    if (type == Public) isVisibleToPublic = true
                    isViewedByInquirer = false
                }
            }

            chatRepository.update(this).orElse {
                messageRepository.add(messageEntity.toMessage()).onEmpty {
                    messages.add(messageEntity)
                    eventPublisher.publish(MessageAdded(subject, messageEntity.text()))
                }
            }
        }

    fun deleteMessage(id: Message.Id, user: User): Option<Declination> {
        val messageEntity = messages.find { it.id == id } ?: return some(Declination.withReason("Invalid id"))

        return messageEntity.run {
            DeleteMessagePolicy.forThe(user).isAllowed(authorId, user).orElse {
                messageRepository.delete(this.toMessage()).onEmpty {
                    messages.remove(this)
                    eventPublisher.publish(MessageDeleted(id))
                }
            }
        }
    }

    fun updateTextMessage(id: Message.Id, user: User, text: NonBlankString): Option<Declination> {
        val messageEntity = messages.find { it.id == id } ?: return some(Declination.withReason("Invalid id"))

        return messageEntity.run {
            UpdateMessagePolicy.forAll.isAllowed(authorId, user, type).orElse {
                updateText(text)
                messageRepository.update(this.toMessage()).onEmpty {
                    eventPublisher.publish(MessageUpdated(id, text, this.updatedAt()!!))
                }
            }
        }
    }

    companion object {
        fun newWithSubject(
            clock: Clock,
            eventPublisher: EventPublisher,
            chatRepository: ChatRepository,
            messageRepository: MessageRepository,
            id: Id,
            type: Type,
            askedBy: User.Id,
            subject: Subject,
            messageText: NonBlankString,
        ): Either<Declination, Chat> {
            val now = ZonedDateTime.now(clock)
            val chat = Chat(
                clock,
                eventPublisher,
                chatRepository,
                messageRepository,
                id,
                type,
                askedBy,
                now,
                now,
                subject
            )

            return chat.init(messageText).fold(
                { right(chat) },
                { left(it) }
            )
        }

        fun new(
            clock: Clock,
            eventPublisher: EventPublisher,
            chatRepository: ChatRepository,
            messageRepository: MessageRepository,
            id: Id,
            type: Type,
            askedBy: User.Id,
            messageText: NonBlankString,
        ): Either<Declination, Chat> {
            val now = ZonedDateTime.now(clock)
            val chat = Chat(
                clock,
                eventPublisher,
                chatRepository,
                messageRepository,
                id,
                type,
                askedBy,
                now,
                now
            )

            return chat.init(messageText).fold(
                { right(chat) },
                { left(it) }
            )
        }

        fun restore(
            clock: Clock,
            eventPublisher: EventPublisher,
            chatRepository: ChatRepository,
            messageRepository: MessageRepository,
            id: Id,
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
            chatRepository,
            messageRepository,
            id,
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Chat

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String {
        return "Chat(clock=$clock, eventPublisher=$eventPublisher, chatRepository=$chatRepository, messageRepository=$messageRepository, id=$id, type=$type, askedBy=$askedBy, createdAt=$createdAt, updatedAt=$updatedAt, subject=$subject, messages=$messages, isVisibleToPublic=$isVisibleToPublic, isViewedByImam=$isViewedByImam, isViewedByInquirer=$isViewedByInquirer)"
    }

    data class Id(val value: Long)
    enum class Type { Public, Private }
}

private class MessageEntity private constructor(
    private val clock: Clock,

    val id: Message.Id,
    val type: Message.Type,
    val authorId: User.Id,

    private var text: NonBlankString,

    val audio: NonBlankString? = null,

    val createdAt: ZonedDateTime = ZonedDateTime.now(clock),
    private var updatedAt: ZonedDateTime? = null,
) {
    fun updatedAt() = updatedAt

    fun text() = text

    fun updateText(text: NonBlankString) {
        this.text = text
        updatedAt = ZonedDateTime.now(clock)
    }

    fun toMessage() = Message(id, type, createdAt, updatedAt, authorId, text, audio)

    companion object {
        fun newText(
            clock: Clock,
            id: Message.Id,
            authorId: User.Id,
            text: NonBlankString,
        ) = MessageEntity(clock, id, Text, authorId, text)

        fun newAudio(
            clock: Clock,
            id: Message.Id,
            authorId: User.Id,
            audio: NonBlankString,
        ): MessageEntity {
            val text = NonBlankString.of("Аудио")
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