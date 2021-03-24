package kz.azan.askimam.chat.domain.model

import io.vavr.control.Option
import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.event.ChatCreated
import kz.azan.askimam.chat.domain.event.MessageAdded
import kz.azan.askimam.chat.domain.event.MessageDeleted
import kz.azan.askimam.chat.domain.event.MessageUpdated
import kz.azan.askimam.chat.domain.model.Chat.Type.Public
import kz.azan.askimam.chat.domain.policy.*
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
    val id: Id? = null,
    val type: Type,
    private var subject: Subject? = null,

    val askedBy: User.Id,
    private var answeredBy: User.Id? = null,

    private var inquirerFcmToken: FcmToken,
    private var imamFcmToken: FcmToken? = null,

    val createdAt: LocalDateTime,
    private var updatedAt: LocalDateTime,

    private var isVisibleToPublic: Boolean = false,
    private var isViewedByImam: Boolean = false,
    private var isViewedByInquirer: Boolean = true,

    private val messages: MutableList<Message> = mutableListOf(),

    private val clock: Clock,
    private val eventPublisher: EventPublisher,
    private val getCurrentUser: GetCurrentUser,
) {
    private fun init(messageText: NonBlankString) {
        val message = Message.newText(clock, askedBy, messageText)
        messages.add(message)
        eventPublisher.publish(ChatCreated(subject, messageText))
    }

    fun subject() = subject
    fun subjectText() = subject ?: messages.first().text()
    fun updatedAt() = updatedAt

    fun answeredBy() = answeredBy

    fun inquirerFcmToken() = inquirerFcmToken
    fun imamFcmToken() = imamFcmToken

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

    fun addTextMessage(text: NonBlankString, fcmToken: FcmToken): Option<Declination> {
        val author = getCurrentUser()
        val message = Message.newText(clock, author.id, text)
        return addMessage(message, AddMessagePolicy.getFor(author), author, fcmToken)
    }

    fun addAudioMessage(audio: NonBlankString, fcmToken: FcmToken): Option<Declination> {
        val imam = getCurrentUser()
        val message = Message.newAudio(clock, imam.id, audio)
        return addMessage(message, AddMessagePolicy.forImam, imam, fcmToken)
    }

    private fun addMessage(
        message: Message,
        policy: AddMessagePolicy,
        user: User,
        fcmToken: FcmToken,
    ): Option<Declination> = policy.isAllowed(this, user).onEmpty {

        updatedAt = LocalDateTime.now(clock)

        when (user.type) {
            Inquirer -> {
                isViewedByImam = false
                inquirerFcmToken = fcmToken
            }
            Imam -> {
                if (type == Public) isVisibleToPublic = true
                isViewedByInquirer = false
                answeredBy = user.id
                imamFcmToken = fcmToken
            }
        }

        messages.add(message)
        eventPublisher.publish(MessageAdded(subject, message.text()))
    }

    fun updateTextMessage(id: Message.Id, text: NonBlankString, fcmToken: FcmToken): Option<Declination> {

        val message = messages.find { it.id == id } ?: return some(Declination.withReason("Invalid id"))

        return message.run {
            val currentUser = getCurrentUser()

            UpdateMessagePolicy.forAll.isAllowed(authorId, currentUser, type).onEmpty {
                when (currentUser.type) {
                    Imam -> imamFcmToken = fcmToken
                    Inquirer -> inquirerFcmToken = fcmToken
                }

                updateText(text)
                eventPublisher.publish(MessageUpdated(id, text, this.updatedAt()!!))
            }
        }
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

    fun returnToUnansweredList(): Option<Declination> =
        ReturnChatToUnansweredListPolicy.forAll.isAllowed(getCurrentUser()).onEmpty {
            answeredBy = null
            imamFcmToken = null
        }

    companion object {
        fun newWithSubject(
            type: Type,
            subject: Subject,
            messageText: NonBlankString,

            inquirerFcmToken: FcmToken,

            clock: Clock,
            eventPublisher: EventPublisher,
            getCurrentUser: GetCurrentUser,
        ): Chat {
            val now = LocalDateTime.now(clock)
            return Chat(
                type = type,
                subject = subject,
                askedBy = getCurrentUser().id,

                inquirerFcmToken = inquirerFcmToken,

                createdAt = now,
                updatedAt = now,

                clock = clock,
                eventPublisher = eventPublisher,
                getCurrentUser = getCurrentUser,
            ).apply { init(messageText) }
        }

        fun new(
            type: Type,
            messageText: NonBlankString,

            inquirerFcmToken: FcmToken,

            clock: Clock,
            eventPublisher: EventPublisher,
            getCurrentUser: GetCurrentUser,
        ): Chat {
            val now = LocalDateTime.now(clock)
            return Chat(
                type = type,
                askedBy = getCurrentUser().id,

                inquirerFcmToken = inquirerFcmToken,

                createdAt = now,
                updatedAt = now,

                clock = clock,
                eventPublisher = eventPublisher,
                getCurrentUser = getCurrentUser,
            ).apply { init(messageText) }
        }

        fun restore(
            id: Id,
            type: Type,
            subject: Subject?,

            askedBy: User.Id,
            answeredBy: User.Id?,

            inquirerFcmToken: FcmToken,
            imamFcmToken: FcmToken?,

            createdAt: LocalDateTime,
            updatedAt: LocalDateTime,

            isVisibleToPublic: Boolean,
            isViewedByImam: Boolean,
            isViewedByInquirer: Boolean,

            messages: List<Message>,

            clock: Clock,
            eventPublisher: EventPublisher,
            getCurrentUser: GetCurrentUser,
        ) = Chat(
            id = id,
            type = type,
            subject = subject,

            askedBy = askedBy,
            answeredBy = answeredBy,

            inquirerFcmToken = inquirerFcmToken,
            imamFcmToken = imamFcmToken,

            createdAt = createdAt,
            updatedAt = updatedAt,

            isVisibleToPublic = isVisibleToPublic,
            isViewedByImam = isViewedByImam,
            isViewedByInquirer = isViewedByInquirer,

            messages = messages.toMutableList(),

            clock = clock,
            eventPublisher = eventPublisher,
            getCurrentUser = getCurrentUser,
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
