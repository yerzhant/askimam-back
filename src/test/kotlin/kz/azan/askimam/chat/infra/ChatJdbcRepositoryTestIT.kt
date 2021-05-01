package kz.azan.askimam.chat.infra

import io.mockk.every
import io.vavr.kotlin.some
import kz.azan.askimam.chat.ChatFixtures
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.Chat.Type.Public
import kz.azan.askimam.chat.domain.model.Message
import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.meta.DataJdbcIT
import org.assertj.core.api.Assertions.assertThat
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
        val public = dao.findByTypeAndIsVisibleToPublicIsTrueOrderByUpdatedAtDesc(Public, PageRequest.of(0, 1))
        assertThat(public).hasSize(1)
    }

    @Test
    fun `should find by an id`() {
        val chat = repository.findById(Chat.Id(1)).get()

        with(chat) {
            assertThat(type).isEqualTo(Public)
            assertThat(subject()).isEqualTo(fixtureSubject)

            assertThat(createdAt).isCloseTo(
                LocalDateTime.now(),
                TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
            )
            assertThat(updatedAt()).isCloseTo(
                LocalDateTime.now(),
                TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
            )

            assertThat(isVisibleToPublic()).isTrue
            assertThat(isViewedByImam()).isTrue
            assertThat(isViewedByInquirer()).isTrue

            assertThat(askedBy).isEqualTo(fixtureInquirerId)
            assertThat(answeredBy()).isEqualTo(fixtureImamId)

            assertThat(inquirerFcmToken()).isEqualTo(fixtureInquirerFcmToken)
            assertThat(imamFcmToken()).isEqualTo(fixtureImamFcmToken)


            assertThat(messages().size).isEqualTo(3)

            assertThat(messages()[0].id).isEqualTo(Message.Id(1))
            assertThat(messages()[0].type).isEqualTo(Message.Type.Text)
            assertThat(messages()[0].authorId).isEqualTo(fixtureInquirerId)
            assertThat(messages()[0].text()).isEqualTo(fixtureMessage)
            assertThat(messages()[0].updatedAt()).isNull()
            assertThat(messages()[0].createdAt).isCloseTo(
                LocalDateTime.now(),
                TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
            )

            assertThat(messages()[1].id).isEqualTo(Message.Id(3))
            assertThat(messages()[1].type).isEqualTo(Message.Type.Audio)
            assertThat(messages()[1].authorId).isEqualTo(fixtureImamId)
            assertThat(messages()[1].text()).isEqualTo(NonBlankString.of("Audio"))
            assertThat(messages()[1].audio).isEqualTo(fixtureAudio)
            assertThat(messages()[1].updatedAt()).isNull()
            assertThat(messages()[1].createdAt).isCloseTo(
                LocalDateTime.now().plusDays(1),
                TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
            )

            assertThat(messages()[2].id).isEqualTo(Message.Id(2))
            assertThat(messages()[2].type).isEqualTo(Message.Type.Text)
            assertThat(messages()[2].authorId).isEqualTo(fixtureImamId)
            assertThat(messages()[2].text()).isEqualTo(fixtureNewReply)
            assertThat(messages()[2].createdAt).isCloseTo(
                LocalDateTime.now().plusDays(2),
                TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
            )
            assertThat(messages()[2].updatedAt()).isCloseTo(
                LocalDateTime.now().plusDays(3),
                TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
            )
        }
    }

    @Test
    internal fun `should not find by an id`() {
        val error = repository.findById(Chat.Id(100))

        assertThat(error.left).isEqualTo(Declination.withReason("The chat is not found"))
    }

    @Test
    internal fun `should find public chats`() {
        assertThat(repository.findPublicChats(0, 20).get()).hasSize(2)
    }

    @Test
    internal fun `should find public chats - limit`() {
        assertThat(repository.findPublicChats(0, 1).get()).hasSize(1)
    }

    @Test
    internal fun `should find my chats - inquirer`() {
        every { getCurrentUser() } returns some(fixtureInquirer)

        assertThat(repository.findMyChats(0, 20).get()).hasSize(4)
    }

    @Test
    internal fun `should find my chats - imam`() {
        every { getCurrentUser() } returns some(fixtureImam)

        assertThat(repository.findMyChats(0, 20).get()).hasSize(1)
    }

    @Test
    internal fun `should find unanswered chats`() {
        assertThat(repository.findUnansweredChats(0, 20).get()).hasSize(3)
    }

    @Test
    internal fun `should create a chat`() {
        every { getCurrentUser() } returns some(fixtureInquirer)
        fixtureClock()

        assertThat(repository.create(fixtureChat()).isEmpty).isTrue

        val all = dao.findAll()
        assertThat(all).hasSize(5)
        assertThat(all.find { it.id!! > 4 }?.messages).hasSize(1)
        assertThat(all.find { it.id!! > 4 }?.messages?.first()?.id).isGreaterThan(3)
    }

    @Test
    internal fun `should delete a chat`() {
        fixtureClock()

        assertThat(repository.delete(fixtureSavedChat(id = Chat.Id(1))).isEmpty).isTrue

        assertThat(dao.findAll()).hasSize(3)
    }

    @Test
    internal fun `should update a chat`() {
        fixtureClock()
        every { getCurrentUser() } returns some(fixtureInquirer)
        every { eventPublisher.publish(any()) } returns Unit
        val chat = fixtureSavedChat(id = Chat.Id(1))
        chat.updateSubject(Subject.from("Hello"))
        chat.updateTextMessage(Message.Id(1), NonBlankString.of("Bye"), fixtureInquirerFcmToken)

        assertThat(repository.update(chat).isEmpty).isTrue

        assertThat(dao.findAll().first().subject).isEqualTo("Hello")
    }
}