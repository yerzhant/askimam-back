package kz.azan.askimam.chat.infra.repo

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.vavr.kotlin.some
import kz.azan.askimam.chat.ChatFixtures
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.chat.domain.model.Message
import kz.azan.askimam.chat.infra.dao.ChatDao
import kz.azan.askimam.chat.infra.model.ChatRow
import kz.azan.askimam.common.domain.Declination
import org.assertj.core.api.Assertions
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
            Assertions.assertThat(type).isEqualTo(Chat.Type.Public)
            Assertions.assertThat(subject()).isEqualTo(fixtureSubject)

            Assertions.assertThat(createdAt).isEqualTo(fixtureNow)
            Assertions.assertThat(updatedAt()).isEqualTo(fixtureNow)

            Assertions.assertThat(isVisibleToPublic()).isTrue
            Assertions.assertThat(isViewedByImam()).isTrue
            Assertions.assertThat(isViewedByInquirer()).isTrue

            Assertions.assertThat(askedBy).isEqualTo(fixtureInquirerId)
            Assertions.assertThat(answeredBy()).isEqualTo(fixtureImamId)

            Assertions.assertThat(inquirerFcmToken()).isEqualTo(fixtureInquirerFcmToken)
            Assertions.assertThat(imamFcmToken()).isEqualTo(fixtureImamFcmToken)


            Assertions.assertThat(messages().size).isEqualTo(3)

            Assertions.assertThat(messages().first().type).isEqualTo(Message.Type.Text)
            Assertions.assertThat(messages().first().authorId).isEqualTo(fixtureInquirerId)
            Assertions.assertThat(messages().first().text()).isEqualTo(fixtureMessage)

            Assertions.assertThat(messages().first().createdAt).isEqualTo(fixtureNow)
            Assertions.assertThat(messages().first().updatedAt()).isNull()
        }
    }

    @Test
    internal fun `should not find by an id`() {
        fixtureClock()
        every { dao.findById(1) } returns Optional.empty()

        val error = repository.findById(fixtureChatId1)

        Assertions.assertThat(error.left).isEqualTo(Declination.withReason("The chat is not found"))
    }

    @Test
    internal fun `should not find by an id - db exception`() {
        fixtureClock()
        every { dao.findById(1) } throws Exception("boom")

        val error = repository.findById(fixtureChatId1)

        Assertions.assertThat(error.left).isEqualTo(Declination.withReason("boom"))
    }

    @Test
    internal fun `should find public chats`() {
        fixtureClock()
        every {
            dao.findByTypeAndIsVisibleToPublicIsTrueOrderByUpdatedAtDesc(Chat.Type.Public, any())
        } returns fixtureSavedTwoChats().map { ChatRow.from(it) }

        Assertions.assertThat(repository.findPublicChats(0, 20).get()).hasSize(2)
    }

    @Test
    internal fun `should find my chats - inquirer`() {
        fixtureClock()
        every { getCurrentUser() } returns some(fixtureInquirer)
        every {
            dao.findByAskedByOrderByUpdatedAtDesc(fixtureInquirerId.value, any())
        } returns fixtureSavedTwoChats().map { ChatRow.from(it) }

        Assertions.assertThat(repository.findMyChats(0, 20).get()).hasSize(2)
    }

    @Test
    internal fun `should find my chats - imam`() {
        fixtureClock()
        every { getCurrentUser() } returns some(fixtureImam)
        every {
            dao.findByAnsweredByOrderByUpdatedAtDesc(fixtureImamId.value, any())
        } returns fixtureSavedTwoChats().map { ChatRow.from(it) }

        Assertions.assertThat(repository.findMyChats(0, 20).get()).hasSize(2)
    }

    @Test
    internal fun `should find unanswered chats`() {
        fixtureClock()
        every {
            dao.findByAnsweredByIsNullOrderByUpdatedAtDesc(any())
        } returns fixtureSavedTwoChats().map { ChatRow.from(it) }

        Assertions.assertThat(repository.findUnansweredChats(0, 20).get()).hasSize(2)
    }

    @Test
    internal fun `should not find unanswered chats - db error`() {
        every {
            dao.findByAnsweredByIsNullOrderByUpdatedAtDesc(any())
        } throws Exception("x")

        Assertions.assertThat(repository.findUnansweredChats(0, 20).isLeft).isTrue
    }

    @Test
    internal fun `should create a chat`() {
        fixtureClock()
        every { getCurrentUser() } returns some(fixtureInquirer)
        val chatRow = ChatRow.from(fixtureChat())
        every { dao.save(chatRow) } returns chatRow

        Assertions.assertThat(repository.create(fixtureChat()).isEmpty).isTrue

        verify { dao.save(chatRow) }
    }

    @Test
    internal fun `should not create a chat`() {
        fixtureClock()
        every { getCurrentUser() } returns some(fixtureInquirer)
        val chatRow = ChatRow.from(fixtureChat())
        every { dao.save(chatRow) } throws Exception("ta da")

        Assertions.assertThat(
            repository.create(fixtureChat()).isDefined
        ).isTrue
    }

    @Test
    internal fun `should delete a chat`() {
        fixtureClock()
        every { getCurrentUser() } returns some(fixtureInquirer)
        val chat = fixtureSavedChat()
        val chatRow = ChatRow.from(chat)
        every { dao.delete(chatRow) } returns Unit

        Assertions.assertThat(repository.delete(chat).isEmpty).isTrue

        verify { dao.delete(chatRow) }
    }

    @Test
    internal fun `should not delete a chat`() {
        fixtureClock()
        every { getCurrentUser() } returns some(fixtureInquirer)
        val chat = fixtureSavedChat()
        val chatRow = ChatRow.from(chat)
        every { dao.delete(chatRow) } throws Exception("x")

        Assertions.assertThat(repository.delete(chat).isDefined).isTrue
    }

    @Test
    internal fun `should update a chat`() {
        fixtureClock()
        every { getCurrentUser() } returns some(fixtureInquirer)
        val chat = fixtureChat()
        val chatRow = ChatRow.from(chat)
        every { dao.save(chatRow) } returns chatRow

        Assertions.assertThat(repository.update(chat).isEmpty).isTrue

        verify { dao.save(chatRow) }
    }

    @Test
    internal fun `should not update a chat`() {
        fixtureClock()
        every { getCurrentUser() } returns some(fixtureInquirer)
        val chat = fixtureChat()
        val chatRow = ChatRow.from(chat)
        every { dao.save(chatRow) } throws Exception()

        Assertions.assertThat(repository.update(chat).get()).isEqualTo(
            Declination.withReason("Unknown")
        )
    }
}