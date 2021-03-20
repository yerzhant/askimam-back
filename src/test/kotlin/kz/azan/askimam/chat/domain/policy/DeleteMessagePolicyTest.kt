package kz.azan.askimam.chat.domain.policy

import io.vavr.kotlin.some
import kz.azan.askimam.chat.domain.model.ChatFixtures
import kz.azan.askimam.common.domain.Declination
import kz.azan.askimam.user.domain.model.User
import kz.azan.askimam.user.domain.model.User.Id
import kz.azan.askimam.user.domain.model.User.Type.Imam
import kz.azan.askimam.user.domain.model.User.Type.Inquirer
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DeleteMessagePolicyTest : ChatFixtures() {

    @Test
    internal fun `should allow to delete a message by any imam`() {
        fixtureClock()
        assertThat(DeleteMessagePolicy.forImam.isAllowed(fixtureChat(), User(fixtureImamId, Imam)).isEmpty).isTrue
        assertThat(DeleteMessagePolicy.forImam.isAllowed(fixtureChat(), User(Id(100), Imam)).isEmpty).isTrue
    }

    @Test
    internal fun `should not allow to non imam to delete a message`() {
        fixtureClock()
        assertThat(
            DeleteMessagePolicy.forImam.isAllowed(
                fixtureChat(),
                User(fixtureImamId, Inquirer)
            )
        ).isEqualTo(some(Declination("This operation is only allowed to imams")))
    }

    @Test
    internal fun `should allow an inquirer to delete a message from his own chat`() {
        fixtureClock()
        assertThat(
            DeleteMessagePolicy.forInquirer.isAllowed(
                fixtureChat(),
                User(fixtureInquirerId, Inquirer)
            ).isEmpty
        ).isTrue
    }

    @Test
    internal fun `should not allow an inquirer to delete a message from someone else's chat`() {
        fixtureClock()
        assertThat(
            DeleteMessagePolicy.forInquirer.isAllowed(
                fixtureChat(),
                User(Id(3), Inquirer)
            )
        ).isEqualTo(some(Declination("You're not allowed to delete a message from someone else's chat")))
    }
}