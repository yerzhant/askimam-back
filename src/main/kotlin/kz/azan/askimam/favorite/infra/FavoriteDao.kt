package kz.azan.askimam.favorite.infra

import kz.azan.askimam.favorite.domain.model.Favorite
import org.springframework.data.repository.CrudRepository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
interface FavoriteDao : CrudRepository<FavoriteRow, Long> {
    fun findByUserId(id: Long): Set<FavoriteRow>
    fun findByUserIdAndChatId(userId: Long, chatId: Long): Favorite
}
