package kz.azan.askimam.chat.web

import com.ninjasquad.springmockk.MockkBean
import kz.azan.askimam.chat.app.usecase.*
import kz.azan.askimam.meta.ControllerTest
import kz.azan.askimam.security.web.config.WebSecurityConfig
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import

@WebMvcTest(ChatController::class)
@Import(WebSecurityConfig::class)
internal open class ChatControllerTest : ControllerTest() {

    protected val url = "/chats"

    @MockkBean
    protected lateinit var getPublicChats: GetPublicChats

    @MockkBean
    protected lateinit var getChatUseCase: GetChat

    @MockkBean
    protected lateinit var getMyChats: GetMyChats

    @MockkBean
    protected lateinit var getUnansweredChats: GetUnansweredChats

    @MockkBean
    protected lateinit var findChats: FindChats

    @MockkBean
    protected lateinit var createChat: CreateChat

    @MockkBean
    protected lateinit var deleteChat: DeleteChat

    @MockkBean
    protected lateinit var updateChatSubject: UpdateChatSubject

    @MockkBean
    protected lateinit var setViewedBy: SetViewedBy

    @MockkBean
    protected lateinit var returnChatToUnansweredList: ReturnChatToUnansweredList
}