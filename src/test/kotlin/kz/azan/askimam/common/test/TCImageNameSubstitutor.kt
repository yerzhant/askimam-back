package kz.azan.askimam.common.test

import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.ImageNameSubstitutor

class TCImageNameSubstitutor : ImageNameSubstitutor() {
    override fun apply(original: DockerImageName?): DockerImageName =
        when (original?.unversionedPart) {
            "mysql" -> DockerImageName.parse("reg.azan.kz/test-mysql:1.0.0")
            else -> original!!
        }

    override fun getDescription() = "TC substitutor"
}