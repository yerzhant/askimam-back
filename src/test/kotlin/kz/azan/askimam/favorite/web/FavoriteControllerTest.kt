package kz.azan.askimam.favorite.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.vavr.kotlin.left
import io.vavr.kotlin.right
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.favorite.app.usecase.GetMyFavorites
import kz.azan.askimam.meta.ControllerTest
import kz.azan.askimam.meta.WithPrincipal
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get

internal class FavoriteControllerTest : ControllerTest() {

    private val url = "/favorites"

    @MockkBean
    private lateinit var getMyFavorites: GetMyFavorites

    @Test
    internal fun `should reject with 401`() {
        mvc.get(url).andExpect { status { isUnauthorized() } }
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
}