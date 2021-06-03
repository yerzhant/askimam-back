package kz.azan.askimam.chat.infra.adapter

import io.vavr.control.Either
import io.vavr.kotlin.Try
import kz.azan.askimam.chat.app.port.SearchPort
import kz.azan.askimam.chat.domain.model.Chat
import kz.azan.askimam.common.domain.Declination
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject

@Service
class SearchAdapter(
    private val restTemplate: RestTemplate,
) : SearchPort {

    override fun find(phrase: String): Either<Declination, List<Chat.Id>> =
        Try { restTemplate.getForObject<List<Long>>("/search/$phrase") }
            .toEither()
            .bimap(
                { Declination.from(it) },
                { list -> list.map { Chat.Id(it) } }
            )
}
