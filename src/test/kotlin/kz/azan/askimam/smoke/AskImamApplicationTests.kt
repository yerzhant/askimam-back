package kz.azan.askimam.smoke

import com.ninjasquad.springmockk.MockkBean
import kz.azan.askimam.event.infra.service.FcmService
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class AskImamApplicationTests {

    @MockkBean
    lateinit var fcmService: FcmService

    @Test
    fun contextLoads() {
    }

}
