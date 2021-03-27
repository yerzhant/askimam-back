package kz.azan.askimam.favorite.web

import kz.azan.askimam.common.web.dto.ResponseDto
import kz.azan.askimam.common.web.meta.RestApi
import kz.azan.askimam.favorite.app.usecase.AddChatToFavorites
import kz.azan.askimam.favorite.app.usecase.GetMyFavorites
import kz.azan.askimam.favorite.web.dto.AddChatToFavoritesDto
import kz.azan.askimam.favorite.web.dto.FavoriteDto
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@RestApi("favorites")
class FavoriteController(
    private val getMyFavorites: GetMyFavorites,
    private val addChatToFavorites: AddChatToFavorites,
) {
    @GetMapping
    fun get(): ResponseDto = getMyFavorites().fold(
        { ResponseDto.error(it) },
        { list -> ResponseDto.ok(list.map { FavoriteDto.from(it) }.asJava()) }
    )

    @PostMapping
    fun add(@RequestBody dto: AddChatToFavoritesDto): ResponseDto = addChatToFavorites(dto.chatId()).fold(
        { ResponseDto.ok() },
        { ResponseDto.error(it) }
    )
}