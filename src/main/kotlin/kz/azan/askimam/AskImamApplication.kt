package kz.azan.askimam

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.web.client.RestTemplate
import java.time.Clock

@EnableAsync
@SpringBootApplication
@EnableConfigurationProperties(AppProperties::class)
class AskImamApplication {

    @Bean
    fun restTemplate(appProperties: AppProperties, restTemplateBuilder: RestTemplateBuilder): RestTemplate =
        restTemplateBuilder.rootUri(appProperties.searchUrl).build()

    @Bean
    fun clock(): Clock = Clock.systemDefaultZone()
}

fun main(args: Array<String>) {
    runApplication<AskImamApplication>(*args)
}

@ConfigurationProperties("app")
data class AppProperties(
    val jwt: Jwt,
    val searchUrl: String,
) {
    data class Jwt(val key: String)
}
