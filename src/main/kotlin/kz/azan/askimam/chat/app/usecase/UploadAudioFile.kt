package kz.azan.askimam.chat.app.usecase

import io.vavr.control.Option
import io.vavr.kotlin.Try
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import kz.azan.askimam.common.app.meta.UseCase
import kz.azan.askimam.common.domain.Declination
import java.io.File

@UseCase
class UploadAudioFile {

    operator fun invoke(filename: String?, bytes: ByteArray): Option<Declination> =
        Try {
            if (filename == null) throw IllegalArgumentException("File name must not be null.")
            File("./audio/$filename").writeBytes(bytes)
        }.fold(
            { some(Declination.from(it)) },
            { none() }
        )
}
