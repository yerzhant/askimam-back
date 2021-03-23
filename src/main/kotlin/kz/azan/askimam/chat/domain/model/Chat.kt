package kz.azan.askimam.chat.domain.model

import io.vavr.control.Option
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
import java.time.LocalDateTime

class Chat private constructor(
    private val clock: Clock,
    private val eventPublisher: EventPublisher,
    private val getCurrentUser: GetCurrentUser,

    val id: Id?,
    val type: Type,
    val askedBy: User.Id,

    val createdAt: LocalDateTime,
    private var updatedAt: LocalDateTime,

    private var subject: Subject? = null,

    private val messages: MutableList<Message> = mutableListOf(),

    private var isVisibleToPublic: Boolean = false,
    private var isViewedByImam: Boolean = false,
    private var isViewedByInquirer: Boolean = true,
) {
    private fun init(messageText: NonBlankString) {
        val message = Message.newText(clock, askedBy, messageText)
        messages.add(message)
        eventPublisher.publish(ChatCreated(subject, messageText))
    }

    fun updatedAt() = updatedAt
    fun subject() = subject
    fun messages() = messages.toList()

    fun isVisibleToPublic() = isVisibleToPublic && type == Public
    fun isViewedByImam() = isViewedByImam
    fun isViewedByInquirer() = isViewedByInquirer

    fun viewedByImam(): Option<Declination> =
        UpdateChatPolicy.forImam.isAllowed(this, getCurrentUser()).onEmpty {
            isViewedByImam = true
        }

    fun viewedByInquirer(): Option<Declination> =
        UpdateChatPolicy.forInquirer.isAllowed(this, getCurrentUser()).onEmpty {
            isViewedByInquirer = true
        }

    fun updateSubject(newSubject: Subject): Option<Declination> {
        val user = getCurrentUser()
        return UpdateChatPolicy.getFor(user).isAllowed(this, user).onEmpty {
            subject = newSubject
        }
    }

    fun addTextMessage(text: NonBlankString): Option<Declination> {
        val author = getCurrentUser()
        val message = Message.newText(clock, author.id, text)
        return addMessage(message, AddMessagePolicy.getFor(author), author)
    }

    fun addAudioMessage(audio: NonBlankString): Option<Declination> {
        val imam = getCurrentUser()
        val message = Message.newAudio(clock, imam.id, audio)
        return addMessage(message, AddMessagePolicy.forImam, imam)
    }

    private fun addMessage(
        message: Message,
        policy: AddMessagePolicy,
        user: User
    ): Option<Declination> = policy.isAllowed(this, user).onEmpty {

        updatedAt = LocalDateTime.now(clock)

        when (user.type) {
            Inquirer -> isViewedByImam = false
            Imam -> {
                if (type == Public) isVisibleToPublic = true
                isViewedByInquirer = false
            }
        }

        messages.add(message)
        eventPublisher.publish(MessageAdded(subject, message.text()))
    }

    fun deleteMessage(id: Message.Id): Option<Declination> {
        val message = messages.find { it.id == id } ?: return some(Declination.withReason("Invalid id"))

        return message.run {
            val user = getCurrentUser()

            DeleteMessagePolicy.forThe(user).isAllowed(authorId, user).onEmpty {
                messages.remove(this)
                eventPublisher.publish(MessageDeleted(id))
            }
        }
    }

    fun updateTextMessage(id: Message.Id, text: NonBlankString): Option<Declination> {
        val message = messages.find { it.id == id } ?: return some(Declination.withReason("Invalid id"))

        return message.run {
            UpdateMessagePolicy.forAll.isAllowed(authorId, getCurrentUser(), type).onEmpty {
                updateText(text)
                eventPublisher.publish(MessageUpdated(id, text, this.updatedAt()!!))
            }
        }
    }

    companion object {
        fun newWithSubject(
            clock: Clock,
            eventPublisher: EventPublisher,
            getCurrentUser: GetCurrentUser,
            type: Type,
            subject: Subject,
            messageText: NonBlankString,
        ): Chat {
            val now = LocalDateTime.now(clock)
            return Chat(
                clock,
                eventPublisher,
                getCurrentUser,
                null,
                type,
                getCurrentUser().id,
                now,
                now,
                subject
            ).apply { init(messageText) }
        }

        fun new(
            clock: Clock,
            eventPublisher: EventPublisher,
            getCurrentUser: GetCurrentUser,
            type: Type,
            messageText: NonBlankString,
        ): Chat {
            val now = LocalDateTime.now(clock)
            return Chat(
                clock,
                eventPublisher,
                getCurrentUser,
                null,
                type,
                getCurrentUser().id,
                now,
                now
            ).apply { init(messageText) }
        }

        fun restore(
            clock: Clock,
            eventPublisher: EventPublisher,
            getCurrentUser: GetCurrentUser,
            id: Id,
            type: Type,
            askedBy: User.Id,
            createdAt: LocalDateTime,
            updatedAt: LocalDateTime,
            subject: Subject?,
            messages: List<Message>,
            isVisibleToPublic: Boolean,
            isViewedByImam: Boolean,
            isViewedByInquirer: Boolean,
        ) = Chat(
            clock,
            eventPublisher,
            getCurrentUser,
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
