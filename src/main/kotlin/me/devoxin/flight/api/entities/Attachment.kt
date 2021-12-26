package me.devoxin.flight.api.entities

import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream

public data class Attachment(val filename: String, val stream: InputStream) {
    public companion object {
        public fun from(filename: String, inputStream: InputStream): Attachment =
            Attachment(filename, inputStream)

        public fun from(filename: String, byteArray: ByteArray): Attachment =
            Attachment(filename, ByteArrayInputStream(byteArray))

        public fun from(file: File, filename: String? = null): Attachment =
            Attachment(filename ?: file.name, FileInputStream(file))
    }
}
