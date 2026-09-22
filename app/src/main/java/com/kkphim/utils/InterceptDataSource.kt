package com.kkphim.utils

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.datasource.*
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class InterceptDataSource(
    private val upstream: DataSource = DefaultHttpDataSource.Factory()
        .setUserAgent("Mozilla/5.0")
        .setAllowCrossProtocolRedirects(true)
        .createDataSource()
) : DataSource {

    private var stream: ByteArrayInputStream? = null
    private var intercepted = false

    override fun open(dataSpec: DataSpec): Long {
        val uri = dataSpec.uri.toString()

        val length = upstream.open(dataSpec)

        if (!uri.contains(".m3u8") || uri.contains(".ts")) {
            intercepted = false
            return length
        }

        intercepted = true

        val buffer = ByteArrayOutputStream()
        val temp = ByteArray(8192)

        while (true) {
            val read = upstream.read(temp, 0, temp.size)
            if (read == -1) break
            buffer.write(temp, 0, read)
        }

        val original = buffer.toString("UTF-8")

        val cleaned = M3u8Filter.processM3u8(original, uri)

        val bytes = cleaned.toByteArray(Charsets.UTF_8)
        stream = ByteArrayInputStream(bytes)

        return bytes.size.toLong()
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        return if (intercepted) {
            stream?.read(buffer, offset, length) ?: C.RESULT_END_OF_INPUT
        } else {
            upstream.read(buffer, offset, length)
        }
    }

    override fun getUri(): Uri? = upstream.uri

    override fun close() {
        upstream.close()
        stream = null
    }

    override fun addTransferListener(listener: TransferListener) {
        upstream.addTransferListener(listener)
    }
}
