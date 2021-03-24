package kz.azan.askimam.chat.infra

import io.mockk.every
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.Chat.Type.Public
import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.chat.domain.model.Message
import kz.azan.askimam.chat.domain.model.Subject
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.common.infra.DataJdbcIT
import kz.azan.askimam.common.type.NonBlankString
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.TemporalUnitWithinOffset
import org.junit.jupiter.api.Test
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
    }

    @Test
    internal fun `should find by an id`() {
        val chat = repository.findById(fixtureChatId).get()

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
            assertThat(messages()[1].text()).isEqualTo(fixtureAudioText)
            assertThat(messages()[1].audio).isEqualTo(fixtureAudio)
            assertThat(messages()[1].updatedAt()).isNull()
            assertThat(messages()[1].createdAt).isCloseTo(
                LocalDateTime.now().plusHours(12),
                TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
            )

            assertThat(messages()[2].id).isEqualTo(Message.Id(2))
            assertThat(messages()[2].type).isEqualTo(Message.Type.Text)
            assertThat(messages()[2].authorId).isEqualTo(fixtureImamId)
            assertThat(messages()[2].text()).isEqualTo(fixtureNewReply)
            assertThat(messages()[2].createdAt).isCloseTo(
                LocalDateTime.now().plusDays(1),
                TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
            )
            assertThat(messages()[2].updatedAt()).isCloseTo(
                LocalDateTime.now().plusDays(1),
                TemporalUnitWithinOffset(1, ChronoUnit.SECONDS)
            )
        }
    }

    @Test
    internal fun `should not find by an id`() {
        val error = repository.findById(Chat.Id(100))

        assertThat(error.left).isEqualTo(Declination.withReason("Chat not found"))
    }

    @Test
    internal fun `should create a chat`() {
        every { getCurrentUser() } returns fixtureInquirer
        fixtureClock()

        assertThat(repository.create(fixtureChat()).isEmpty).isTrue

        val all = dao.findAll()
        assertThat(all).hasSize(2)
        assertThat(all.find { it.id!! != 1L }?.messages).hasSize(1)
        assertThat(all.find { it.id!! != 1L }?.messages?.first()?.id).isGreaterThan(3)
    }

    @Test
    internal fun `should delete a chat`() {
        fixtureClock()

        assertThat(repository.delete(fixtureSavedChat()).isEmpty).isTrue

        assertThat(dao.findAll()).hasSize(0)
    }

    @Test
    internal fun `should update a chat`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureInquirer
        every { eventPublisher.publish(any()) } returns Unit
        val chat = fixtureSavedChat()
        chat.updateSubject(Subject.from("Hello"))
        chat.updateTextMessage(Message.Id(1), NonBlankString.of("Bye"), fixtureInquirerFcmToken)

        assertThat(repository.update(chat).isEmpty).isTrue

        assertThat(dao.findAll().first().subject).isEqualTo("Hello")
    }
}