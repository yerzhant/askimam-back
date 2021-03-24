package kz.azan.askimam.chat.infra

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kz.azan.askimam.chat.domain.model.Chat.Type.Public
import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.chat.domain.model.Message
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

internal class ChatJdbcRepositoryTest : ChatFixtures() {

    private val dao = mockk<ChatDao>()

    private val repository = ChatJdbcRepository(dao, clock, eventPublisher, getCurrentUser)

    @Test
    internal fun `should find by an id`() {
        fixtureClock()
        every { dao.findById(1) } returns Optional.of(ChatRow.from(fixtureSavedChat()))

        val chat = repository.findById(fixtureChatId1).get()

        with(chat) {
            assertThat(type).isEqualTo(Public)
            assertThat(subject()).isEqualTo(fixtureSubject)

            assertThat(createdAt).isEqualTo(fixtureNow)
            assertThat(updatedAt()).isEqualTo(fixtureNow)

            assertThat(isVisibleToPublic()).isTrue
            assertThat(isViewedByImam()).isTrue
            assertThat(isViewedByInquirer()).isTrue

            assertThat(askedBy).isEqualTo(fixtureInquirerId)
            assertThat(answeredBy()).isEqualTo(fixtureImamId)

            assertThat(inquirerFcmToken()).isEqualTo(fixtureInquirerFcmToken)
            assertThat(imamFcmToken()).isEqualTo(fixtureImamFcmToken)


            assertThat(messages().size).isEqualTo(3)

            assertThat(messages().first().type).isEqualTo(Message.Type.Text)
            assertThat(messages().first().authorId).isEqualTo(fixtureInquirerId)
            assertThat(messages().first().text()).isEqualTo(fixtureMessage)

            assertThat(messages().first().createdAt).isEqualTo(fixtureNow)
            assertThat(messages().first().updatedAt()).isNull()
        }
    }

    @Test
    internal fun `should not find by an id`() {
        fixtureClock()
        every { dao.findById(1) } returns Optional.empty()

        val error = repository.findById(fixtureChatId1)

        assertThat(error.left).isEqualTo(Declination.withReason("Chat not found"))
    }

    @Test
    internal fun `should not find by an id - db exception`() {
        fixtureClock()
        every { dao.findById(1) } throws Exception("boom")

        val error = repository.findById(fixtureChatId1)

        assertThat(error.left).isEqualTo(Declination.withReason("boom"))
    }

    @Test
    internal fun `should find public chats`() {
        fixtureClock()
        every {
            dao.findByTypeAndIsVisibleToPublicIsTrue(Public, any())
        } returns fixtureSavedTwoChats().map { ChatRow.from(it) }

        assertThat(repository.findPublicChats(0, 20).get()).hasSize(2)
    }

    @Test
    internal fun `should create a chat`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureInquirer
        val chatRow = ChatRow.from(fixtureChat())
        every { dao.save(chatRow) } returns chatRow

        assertThat(repository.create(fixtureChat()).isEmpty).isTrue

        verify { dao.save(chatRow) }
    }

    @Test
    internal fun `should not create a chat`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureInquirer
        val chatRow = ChatRow.from(fixtureChat())
        every { dao.save(chatRow) } throws Exception("ta da")

        assertThat(
            repository.create(fixtureChat()).isDefined
        ).isTrue
    }

    @Test
    internal fun `should delete a chat`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureInquirer
        val chat = fixtureSavedChat()
        val chatRow = ChatRow.from(chat)
        every { dao.delete(chatRow) } returns Unit

        assertThat(repository.delete(chat).isEmpty).isTrue

        verify { dao.delete(chatRow) }
    }

    @Test
    internal fun `should not delete a chat`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureInquirer
        val chat = fixtureSavedChat()
        val chatRow = ChatRow.from(chat)
        every { dao.delete(chatRow) } throws Exception("x")

        assertThat(repository.delete(chat).isDefined).isTrue
    }

    @Test
    internal fun `should update a chat`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureInquirer
        val chat = fixtureChat()
        val chatRow = ChatRow.from(chat)
        every { dao.save(chatRow) } returns chatRow

        assertThat(repository.update(chat).isEmpty).isTrue

        verify { dao.save(chatRow) }
    }

    @Test
    internal fun `should not update a chat`() {
        fixtureClock()
        every { getCurrentUser() } returns fixtureInquirer
        val chat = fixtureChat()
        val chatRow = ChatRow.from(chat)
        every { dao.save(chatRow) } throws Exception()

        assertThat(repository.update(chat).get()).isEqualTo(
            Declination.withReason("Unknown")
        )
    }
}