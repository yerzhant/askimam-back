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
import kz.azan.askimam.chat.domain.policy.AddMessagePolicy
import kz.azan.askimam.chat.domain.policy.DeleteMessagePolicy
import kz.azan.askimam.chat.domain.policy.UpdateChatPolicy
import kz.azan.askimam.chat.domain.policy.UpdateMessagePolicy
import kz.azan.askimam.chat.domain.service.GetCurrentUser
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
    private val getCurrentUser: GetCurrentUser,
    private val chatRepository: ChatRepository,
    private val messageRepository: MessageRepository,

    val id: Id,
    val type: Type,
    val askedBy: User.Id,

    private val createdAt: ZonedDateTime,
    private var updatedAt: ZonedDateTime,

    private var subject: Subject? = null,

    private val messages: MutableList<Message> = mutableListOf(),

    private var isVisibleToPublic: Boolean = false,
    private var isViewedByImam: Boolean = false,
    private var isViewedByInquirer: Boolean = true,
) {
    private fun init(messageText: NonBlankString) = messageRepository.generateId().fold(
        { some(it) },
        {
            chatRepository.create(this).orElse {
                val message = Message.newText(clock, it, askedBy, messageText)

                messageRepository.add(message).onEmpty {
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

    fun viewedByImam(): Option<Declination> =
        UpdateChatPolicy.forImam.isAllowed(this, getCurrentUser()).orElse {
            isViewedByImam = true
            chatRepository.update(this)
        }

    fun viewedByInquirer(): Option<Declination> =
        UpdateChatPolicy.forInquirer.isAllowed(this, getCurrentUser()).orElse {
            isViewedByInquirer = true
            chatRepository.update(this)
        }

    fun subject() = subject

    fun messages() = messages.toList()

    fun updateSubject(newSubject: Subject): Option<Declination> {
        val user = getCurrentUser()
        return UpdateChatPolicy.getFor(user).isAllowed(this, user).orElse {
            subject = newSubject
            chatRepository.update(this)
        }
    }

    fun addTextMessage(text: NonBlankString): Option<Declination> = messageRepository.generateId().fold(
        { some(it) },
        {
            val author = getCurrentUser()
            val message = Message.newText(clock, it, author.id, text)
            addMessage(message, AddMessagePolicy.getFor(author), author)
        }
    )

    fun addAudioMessage(audio: NonBlankString): Option<Declination> = messageRepository.generateId().fold(
        { some(it) },
        {
            val imam = getCurrentUser()
            val message = Message.newAudio(clock, it, imam.id, audio)
            addMessage(message, AddMessagePolicy.forImam, imam)
        }
    )

    private fun addMessage(
        message: Message,
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
                messageRepository.add(message).onEmpty {
                    messages.add(message)
                    eventPublisher.publish(MessageAdded(subject, message.text()))
                }
            }
        }

    fun deleteMessage(id: Message.Id): Option<Declination> {
        val messageEntity = messages.find { it.id == id } ?: return some(Declination.withReason("Invalid id"))

        return messageEntity.run {
            val user = getCurrentUser()
            DeleteMessagePolicy.forThe(user).isAllowed(authorId, user).orElse {
                messageRepository.delete(this).onEmpty {
                    messages.remove(this)
                    eventPublisher.publish(MessageDeleted(id))
                }
            }
        }
    }

    fun updateTextMessage(id: Message.Id, text: NonBlankString): Option<Declination> {
        val messageEntity = messages.find { it.id == id } ?: return some(Declination.withReason("Invalid id"))

        return messageEntity.run {
            UpdateMessagePolicy.forAll.isAllowed(authorId, getCurrentUser(), type).orElse {
                updateText(text)
                messageRepository.update(this).onEmpty {
                    eventPublisher.publish(MessageUpdated(id, text, this.updatedAt()!!))
                }
            }
        }
    }

    companion object {
        fun newWithSubject(
            clock: Clock,
            eventPublisher: EventPublisher,
            getCurrentUser: GetCurrentUser,
            chatRepository: ChatRepository,
            messageRepository: MessageRepository,
            type: Type,
            subject: Subject,
            messageText: NonBlankString,
        ): Either<Declination, Chat> = chatRepository.generateId().flatMap { id ->
            val now = ZonedDateTime.now(clock)
            val chat = Chat(
                clock,
                eventPublisher,
                getCurrentUser,
                chatRepository,
                messageRepository,
                id,
                type,
                getCurrentUser().id,
                now,
                now,
                subject
            )

            chat.init(messageText).fold(
                { right(chat) },
                { left(it) }
            )
        }

        fun new(
            clock: Clock,
            eventPublisher: EventPublisher,
            getCurrentUser: GetCurrentUser,
            chatRepository: ChatRepository,
            messageRepository: MessageRepository,
            type: Type,
            messageText: NonBlankString,
        ): Either<Declination, Chat> = chatRepository.generateId().flatMap { id ->
            val now = ZonedDateTime.now(clock)
            val chat = Chat(
                clock,
                eventPublisher,
                getCurrentUser,
                chatRepository,
                messageRepository,
                id,
                type,
                getCurrentUser().id,
                now,
                now
            )

            chat.init(messageText).fold(
                { right(chat) },
                { left(it) }
            )
        }

        fun restore(
            clock: Clock,
            eventPublisher: EventPublisher,
            getCurrentUser: GetCurrentUser,
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
            getCurrentUser,
            chatRepository,
            messageRepository,
            id,
            type,
            askedBy,
            createdAt,
            updatedAt,
            subject,
            messages.toMutableList(),
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
        return "Chat(id=$id, type=$type, askedBy=$askedBy, createdAt=$createdAt, updatedAt=$updatedAt, subject=$subject, messages=$messages, isVisibleToPublic=$isVisibleToPublic, isViewedByImam=$isViewedByImam, isViewedByInquirer=$isViewedByInquirer)"
    }

    data class Id(val value: Long)
    enum class Type { Public, Private }
}
