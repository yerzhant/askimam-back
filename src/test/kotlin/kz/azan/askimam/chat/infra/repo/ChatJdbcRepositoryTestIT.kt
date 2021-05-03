package kz.azan.askimam.chat.infra.repo

import io.mockk.every
import io.vavr.kotlin.some
import kz.azan.askimam.chat.ChatFixtures
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.Message
import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.chat.infra.dao.ChatDao
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.meta.DataJdbcIT
import org.assertj.core.api.Assertions
import org.assertj.core.data.TemporalUnitWithinOffset
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.jdbc.Sql
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

@DataJdbcIT
@Sql("/scripts/users.sql", "/scripts/chats.sql")
internal class ChatJdbcRepositoryTestIT(
    private val dao: ChatDao,
) : ChatFixtures() {

    private val repository = ChatJdbcRepository(dao, clock, eventPublisher, getCurrentUser)

    @Test
    internal fun `should get a chat by an id - dao`() {
        dao.findById(1).get()
        val public = dao.findByTypeAndIsVisibleToPublicIsTrueOrderByUpdatedAtDesc(
            Chat.Type.Public,
            PageRequest.of(0, 1)
        )
        Assertions.assertThat(public).hasSize(1)
    }

    @Test
    fun `should find by an id`() {
        val chat = repository.findById(Chat.Id(1)).get()

        with(chat) {
            Assertions.assertThat(type).isEqualTo(Chat.Type.Public)
            Assertions.assertThat(subject()).isEqualTo(fixtureSubject)

            Assertions.assertThat(createdAt).isCloseTo(
                LocalDateTime.now(),
                TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
            )
            Assertions.assertThat(updatedAt()).isCloseTo(
                LocalDateTime.now(),
                TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
            )

            Assertions.assertThat(isVisibleToPublic()).isTrue
            Assertions.assertThat(isViewedByImam()).isTrue
            Assertions.assertThat(isViewedByInquirer()).isTrue

            Assertions.assertThat(askedBy).isEqualTo(fixtureInquirerId)
            Assertions.assertThat(answeredBy()).isEqualTo(fixtureImamId)

            Assertions.assertThat(inquirerFcmToken()).isEqualTo(fixtureInquirerFcmToken)
            Assertions.assertThat(imamFcmToken()).isEqualTo(fixtureImamFcmToken)


            Assertions.assertThat(messages().size).isEqualTo(3)

            Assertions.assertThat(messages()[0].id).isEqualTo(Message.Id(1))
            Assertions.assertThat(messages()[0].type).isEqualTo(Message.Type.Text)
            Assertions.assertThat(messages()[0].authorId).isEqualTo(fixtureInquirerId)
            Assertions.assertThat(messages()[0].text()).isEqualTo(fixtureMessage)
            Assertions.assertThat(messages()[0].updatedAt()).isNull()
            Assertions.assertThat(messages()[0].createdAt).isCloseTo(
                LocalDateTime.now(),
                TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
            )

            Assertions.assertThat(messages()[1].id).isEqualTo(Message.Id(3))
            Assertions.assertThat(messages()[1].type).isEqualTo(Message.Type.Audio)
            Assertions.assertThat(messages()[1].authorId).isEqualTo(fixtureImamId)
            Assertions.assertThat(messages()[1].text()).isEqualTo(NonBlankString.of("Audio"))
            Assertions.assertThat(messages()[1].audio).isEqualTo(fixtureAudio)
            Assertions.assertThat(messages()[1].updatedAt()).isNull()
            Assertions.assertThat(messages()[1].createdAt).isCloseTo(
                LocalDateTime.now().plusDays(1),
                TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
            )

            Assertions.assertThat(messages()[2].id).isEqualTo(Message.Id(2))
            Assertions.assertThat(messages()[2].type).isEqualTo(Message.Type.Text)
            Assertions.assertThat(messages()[2].authorId).isEqualTo(fixtureImamId)
            Assertions.assertThat(messages()[2].text()).isEqualTo(fixtureNewReply)
            Assertions.assertThat(messages()[2].createdAt).isCloseTo(
                LocalDateTime.now().plusDays(2),
                TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
            )
            Assertions.assertThat(messages()[2].updatedAt()).isCloseTo(
                LocalDateTime.now().plusDays(3),
                TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
            )
        }
    }

    @Test
    internal fun `should not find by an id`() {
        val error = repository.findById(Chat.Id(100))

        Assertions.assertThat(error.left).isEqualTo(Declination.withReason("The chat is not found"))
    }

    @Test
    internal fun `should find public chats`() {
        Assertions.assertThat(repository.findPublicChats(0, 20).get()).hasSize(2)
    }

    @Test
    internal fun `should find public chats - limit`() {
        Assertions.assertThat(repository.findPublicChats(0, 1).get()).hasSize(1)
    }

    @Test
    internal fun `should find my chats - inquirer`() {
        every { getCurrentUser() } returns some(fixtureInquirer)

        Assertions.assertThat(repository.findMyChats(0, 20).get()).hasSize(4)
    }

    @Test
    internal fun `should find my chats - imam`() {
        every { getCurrentUser() } returns some(fixtureImam)

        Assertions.assertThat(repository.findMyChats(0, 20).get()).hasSize(1)
    }

    @Test
    internal fun `should find unanswered chats`() {
        Assertions.assertThat(repository.findUnansweredChats(0, 20).get()).hasSize(3)
    }

    @Test
    internal fun `should create a chat`() {
        every { getCurrentUser() } returns some(fixtureInquirer)
        fixtureClock()

        Assertions.assertThat(repository.create(fixtureChat()).isEmpty).isTrue

        val all = dao.findAll()
        Assertions.assertThat(all).hasSize(5)
        Assertions.assertThat(all.find { it.id!! > 4 }?.messages).hasSize(1)
        Assertions.assertThat(all.find { it.id!! > 4 }?.messages?.first()?.id).isGreaterThan(3)
    }

    @Test
    internal fun `should delete a chat`() {
        fixtureClock()

        Assertions.assertThat(repository.delete(fixtureSavedChat(id = Chat.Id(1))).isEmpty).isTrue

        Assertions.assertThat(dao.findAll()).hasSize(3)
    }

    @Test
    internal fun `should update a chat`() {
        fixtureClock()
        every { getCurrentUser() } returns some(fixtureInquirer)
        every { eventPublisher.publish(any()) } returns Unit
        val chat = fixtureSavedChat(id = Chat.Id(1))
        chat.updateSubject(Subject.from("Hello"))
        chat.updateTextMessage(Message.Id(1), NonBlankString.of("Bye"), fixtureInquirerFcmToken)

        Assertions.assertThat(repository.update(chat).isEmpty).isTrue

        Assertions.assertThat(dao.findAll().first().subject).isEqualTo("Hello")
    }
}