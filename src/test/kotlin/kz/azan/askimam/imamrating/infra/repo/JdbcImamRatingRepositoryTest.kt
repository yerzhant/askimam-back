package kz.azan.askimam.imamrating.infra.repo

import io.mockk.every
import io.mockk.verify
import kz.azan.askimam.imamrating.ImamRatingFixtures
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.util.*

internal class JdbcImamRatingRepositoryTest : ImamRatingFixtures() {

    private val underTest = JdbcImamRatingRepository(dao, userDao)

    @Test
    internal fun `should find by user id`() {
        every { dao.findById(imamId.value) } returns Optional.of(ratingRow)

        val result = underTest.findById(imamId)

        assertThat(result.get()).isEqualTo(rating)
    }

    @Test
    internal fun `should not find by user id`() {
        every { dao.findById(imamId.value) } throws Exception("x")

        val result = underTest.findById(imamId)

        assertThat(result.left.reason.value).isEqualTo("x")
    }

    @Test
    internal fun `should create new rating and return it`() {
        every { dao.findById(imamId.value) } returns Optional.empty()
        every { dao.save(ratingRow.copy(rating = 0)) } returns ratingRow.copy(rating = 0)

        val result = underTest.findById(imamId)

        assertThat(result.get()).isEqualTo(rating.copy(rating = 0))
    }

    @Test
    internal fun `should not create new rating and return it`() {
        every { dao.findById(imamId.value) } returns Optional.empty()
        every { dao.save(ratingRow.copy(rating = 0)) } throws Exception("x")

        val result = underTest.findById(imamId)

        assertThat(result.left.reason.value).isEqualTo("x")
    }

    @Test
    internal fun `should save rating`() {
        every { dao.save(ratingRow) } returns ratingRow

        val result = underTest.save(rating)

        assertThat(result.isEmpty).isTrue
        verify { dao.save(ratingRow) }
    }

    @Test
    internal fun `should not save rating`() {
        every { dao.save(ratingRow) } throws Exception("x")

        val result = underTest.save(rating)

        assertThat(result.get().reason.value).isEqualTo("x")
    }

    @Test
    internal fun `should get ratings`() {
        every { dao.findAllByOrderByRatingDesc() } returns listOf(ratingRow)
        every { userDao.findById(imamId.value.toInt()) } returns Optional.of(imamRow)

        val result = underTest.findAllOrderedByRating()

        assertThat(result.get()).containsAll(listOf(rating))
    }

    @Test
    internal fun `should get empty ratings`() {
        every { dao.findAllByOrderByRatingDesc() } returns listOf(ratingRow)
        every { userDao.findById(imamId.value.toInt()) } returns Optional.of(userRow)

        val result = underTest.findAllOrderedByRating()

        assertThat(result.get()).containsAll(listOf())
    }

    @Test
    internal fun `should get empty ratings - user not found`() {
        every { dao.findAllByOrderByRatingDesc() } returns listOf(ratingRow)
        every { userDao.findById(imamId.value.toInt()) } returns Optional.empty()

        val result = underTest.findAllOrderedByRating()

        assertThat(result.get()).containsAll(listOf())
    }

    @Test
    internal fun `should get empty ratings as well`() {
        every { dao.findAllByOrderByRatingDesc() } returns listOf()

        val result = underTest.findAllOrderedByRating()

        assertThat(result.get()).containsAll(listOf())
    }

    @Test
    internal fun `should not get ratings`() {
        every { dao.findAllByOrderByRatingDesc() } returns listOf(ratingRow)
        every { userDao.findById(imamId.value.toInt()) } throws Exception("x")

        val result = underTest.findAllOrderedByRating()

        assertThat(result.left.reason.value).isEqualTo("x")
    }

    @Test
    internal fun `should not get ratings either`() {
        every { dao.findAllByOrderByRatingDesc() } throws Exception("x")

        val result = underTest.findAllOrderedByRating()

        assertThat(result.left.reason.value).isEqualTo("x")
    }
}
