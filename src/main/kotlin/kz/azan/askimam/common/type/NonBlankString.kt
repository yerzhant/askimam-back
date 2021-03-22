package kz.azan.askimam.common.type

class NonBlankString private constructor(val value: String) {

    companion object {
        fun of(value: String): NonBlankString {
            require(value.isNotBlank())
            return NonBlankString(value.trim())
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as NonBlankString

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return "NotBlankString(value='$value')"
    }

}