package kz.azan.askimam.favorite.web

import kz.azan.askimam.meta.ControllerTest
import kz.azan.askimam.meta.WithPrincipal
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.springframework.test.web.servlet.get

internal class FavoriteControllerTest : ControllerTest() {

    private val url = "/favorite"

    @Test
    internal fun `should reject with 401`() {
        mvc.get(url).andExpect { status { isUnauthorized() } }
    }

    @Test
    @WithPrincipal
    internal fun `should get list of favorites`() {
        mvc.get(url).andExpect {
            status { isOk() }
            jsonPath("\$", Matchers.hasSize<Any>(3))
        }
    }
}