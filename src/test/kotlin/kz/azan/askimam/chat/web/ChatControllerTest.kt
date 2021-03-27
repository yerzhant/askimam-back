package kz.azan.askimam.chat.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import kz.azan.askimam.chat.app.usecase.GetPublicChats
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.meta.ControllerTest
import kz.azan.askimam.meta.WithPrincipal
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.security.test.context.support.WithAnonymousUser
import org.springframework.test.web.servlet.get

@WebMvcTest(ChatController::class)
internal class ChatControllerTest : ControllerTest() {

    private val url = "/chats"

    @MockkBean
    private lateinit var getPublicChats: GetPublicChats

    @Test
    internal fun `should be publicly accessible`() {
        every { getPublicChats(0, 20) } returns right(listOfChatProjectionsFixture())

        mvc.get("$url/public/0/20").andExpect { status { isOk() } }
    }

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
}