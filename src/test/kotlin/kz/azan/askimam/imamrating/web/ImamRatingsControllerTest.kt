package kz.azan.askimam.imamrating.web

import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import io.vavr.kotlin.left
import io.vavr.kotlin.list
import io.vavr.kotlin.right
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.common.type.NonBlankString
import kz.azan.askimam.imamrating.app.projection.ImamRatingProjection
import kz.azan.askimam.imamrating.app.usecase.GetImamRatings
import kz.azan.askimam.meta.ControllerTest
import kz.azan.askimam.user.domain.model.User
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.web.servlet.get

@WebMvcTest(ImamRatingsController::class)
internal class ImamRatingsControllerTest : ControllerTest() {

    private val url = "/imam-ratings"

    @MockkBean
    private lateinit var getImamRatings: GetImamRatings

    private val imam = User(User.Id(1), User.Type.Imam, NonBlankString.of("Imam"), NonBlankString.of("p"))

    private val projection = ImamRatingProjection(imam, 123)

    @Test
    internal fun `should get ratings`() {
        every { getImamRatings() } returns right(list(projection))

        mvc.get(url).andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Ok") }
            jsonPath("\$.data", hasSize<Any>(1))
            jsonPath("\$.data[0].name") { value("Imam") }
            jsonPath("\$.data[0].rating") { value(123) }
        }
    }

    @Test
    internal fun `should not get ratings`() {
        every { getImamRatings() } returns left(Declination.withReason("x"))

        mvc.get(url).andExpect {
            status { isOk() }
            jsonPath("\$.status") { value("Error") }
            jsonPath("\$.error") { value("x") }
        }
    }
}
