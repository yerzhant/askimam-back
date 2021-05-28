package kz.azan.askimam.chat.app.usecase

import io.vavr.control.Option
import io.vavr.kotlin.Try
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.domain.Declination
import org.springframework.web.multipart.MultipartFile
import java.io.File

@UseCase
class UploadAudioFile {

    operator fun invoke(file: MultipartFile): Option<Declination> =
        Try {
            file.transferTo(File("./audio/${file.originalFilename}"))
        }.fold(
            { some(Declination.from(it)) },
            { none() }
        )
}
