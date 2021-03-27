package kz.azan.askimam.chat.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import kz.azan.askimam.chat.app.projection.ChatProjection
import kz.azan.askimam.chat.app.usecase.GetChat
import kz.azan.askimam.chat.app.usecase.GetPublicChats
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.meta.ControllerTest
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.get

@WebMvcTest(ChatController::class)
internal class ChatControllerTest : ControllerTest() {

    private val url = "/chats"

    @MockkBean
    private lateinit var getPublicChats: GetPublicChats

    @MockkBean
    private lateinit var getChatUseCase: GetChat

    @Test
    internal fun `should be rejected with 401`() {
//        mvc.get("$url/public").andExpect { status { isUnauthorized() } }
    }

    @Test
    internal fun `should get public chats`() {
        every { getPublicChats(0, 20) } returns right(listOfChatProjectionsFixture())

        mvc.get("$url/public/0/20").andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
            jsonPath("\$.data", hasSize<Any>(2))
            jsonPath("\$.data[0].id") { value(1) }
            jsonPath("\$.data[0].subject") { value("Subject") }
            jsonPath("\$.data[0].isFavorite") { value(false) }
        }
    }

    @Test
    internal fun `should not get public chats`() {
        every { getPublicChats(0, 20) } returns left(Declination.withReason("x"))

        mvc.get("$url/public/0/20").andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Error") }
            jsonPath("\$.error") { value("x") }
        }
    }

    @Test
    internal fun `should get a chat`() {
        fixtureClock()
        every { userRepository.findById(fixtureImamId) } returns fixtureImam
        every { getChatUseCase(fixtureChatId1) } returns right(ChatProjection.from(fixtureSavedChat(), userRepository))

        mvc.get("$url/messages/1").andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
            jsonPath("\$.data.id") { value(1) }
            jsonPath("\$.data.subject") { value("Subject") }
            jsonPath("\$.data.messages", hasSize<Any>(3))
            jsonPath("\$.data.messages[0].id") { value(1) }
            jsonPath("\$.data.messages[0].type") { value("Text") }
            jsonPath("\$.data.messages[0].text") { value("A message") }
            jsonPath("\$.data.messages[0].author") { doesNotExist() }
            jsonPath("\$.data.messages[0].createdAt") { value(timeAfter(0).toString()) }
            jsonPath("\$.data.messages[0].updatedAt") { doesNotExist() }
            jsonPath("\$.data.messages[1].id") { value(2) }
            jsonPath("\$.data.messages[1].type") { value("Text") }
            jsonPath("\$.data.messages[1].text") { value("A message") }
            jsonPath("\$.data.messages[1].author") { value("Imam") }
            jsonPath("\$.data.messages[1].createdAt") { value(timeAfter(0).toString()) }
            jsonPath("\$.data.messages[1].updatedAt") { doesNotExist() }
        }
    }
}