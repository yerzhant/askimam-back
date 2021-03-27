package kz.azan.askimam.meta

import com.fasterxml.jackson.databind.ObjectMapper
import kz.azan.askimam.favorite.FavoriteFixtures
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.TestConstructor
import org.springframework.test.context.TestConstructor.AutowireMode.ALL
import org.springframework.test.web.servlet.MockMvc

@WebMvcTest
@TestConstructor(autowireMode = ALL)
open class ControllerTest : FavoriteFixtures() {

    @Autowired
    protected lateinit var mvc: MockMvc

    @Autowired
    protected lateinit var objectMapper: ObjectMapper
}