package kz.azan.askimam.favorite.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.vavr.kotlin.left
import io.vavr.kotlin.none
import io.vavr.kotlin.right
import io.vavr.kotlin.some
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.favorite.app.usecase.AddChatToFavorites
import kz.azan.askimam.favorite.app.usecase.GetMyFavorites
import kz.azan.askimam.favorite.web.dto.AddChatToFavoritesDto
import kz.azan.askimam.meta.ControllerTest
import kz.azan.askimam.meta.WithPrincipal
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post

internal class FavoriteControllerTest : ControllerTest() {

    private val url = "/favorites"

    @MockkBean
    private lateinit var getMyFavorites: GetMyFavorites

    @MockkBean
    private lateinit var addChatToFavorites: AddChatToFavorites

    @Test
    internal fun `should reject with 401s`() {
        mvc.get(url).andExpect { status { isUnauthorized() } }
        mvc.post(url).andExpect { status { isUnauthorized() } }
    }

    @Test
    @WithPrincipal
    internal fun `should get list of favorites`() {
        every { getMyFavorites() } returns right(sequenceOfFavoriteProjectionsFixture)

        mvc.get(url).andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
            jsonPath("\$.data", Matchers.hasSize<Any>(2))
            jsonPath("\$.data[0].id") { value(1) }
            jsonPath("\$.data[0].chatId") { value(1) }
            jsonPath("\$.data[0].subject") { value("Subject 1") }
        }
    }

    @Test
    @WithPrincipal
    internal fun `should not get list of favorites`() {
        every { getMyFavorites() } returns left(Declination.withReason("oops!"))

        mvc.get(url).andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Error") }
            jsonPath("\$.error") { value("oops!") }
            jsonPath("\$.data") { doesNotExist() }
        }
    }

    @Test
    @WithPrincipal
    internal fun `should add a chat to my favorites`() {
        every { addChatToFavorites(fixtureChatId1) } returns none()

        mvc.post(url) {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(AddChatToFavoritesDto(1))
        }.andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
        }
    }

    @Test
    @WithPrincipal
    internal fun `should not add a chat to my favorites`() {
        every { addChatToFavorites(fixtureChatId1) } returns some(Declination.withReason("nope"))

        mvc.post(url) {
            contentType = APPLICATION_JSON
            content = objectMapper.writeValueAsString(AddChatToFavoritesDto(1))
        }.andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Error") }
            jsonPath("\$.error") { value("nope") }
        }
    }
}