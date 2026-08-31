// tink.kt —— tink data-flow node frame protocol (universal, language-agnostic).
//
// Frame = [len u32 BE][payload][crc u32 BE]; crc = CRC32-IEEE (0xEDB88320).
// Mirrors std/tink.tie (tie standard library) and the other-language tink
// libraries; pure functions over ByteArray, IO (stdin/stdout) left to the
// caller. Kotlin (JVM / multiplatform), stdlib-only, no dependencies.
//
//   val frame = Tink.frameEncode(byteArrayOf(1, 2, 3))
//   val got = Tink.frameNext(frame, 0)  // Frame?

/** Kotlin implementation of the tink data-flow node frame protocol. */
object Tink {
    /** CRC32-IEEE over a ByteArray (bit-loop, no table; matches java.util.zip). */
    fun crc32(data: ByteArray): Int {
        var crc = -1
        for (b in data) {
            crc = crc xor (b.toInt() and 0xFF)
            repeat(8) {
                crc = if (crc and 1 != 0) (crc ushr 1) xor -0x12477CE0 else (crc ushr 1)
            }
        }
        return crc.inv()
    }

    /** Result of parsing a frame: the payload (a copy) and the position after it. */
    class Frame(val payload: ByteArray, val next: Int)

    /** Encode a payload into a full frame: [len u32 BE][payload][crc u32 BE]. */
    fun frameEncode(payload: ByteArray): ByteArray {
        val n = payload.size
        val out = ByteArray(n + 8)
        out[0] = (n ushr 24).toByte()
        out[1] = (n ushr 16).toByte()
        out[2] = (n ushr 8).toByte()
        out[3] = n.toByte()
        System.arraycopy(payload, 0, out, 4, n)
        val c = crc32(payload)
        out[n + 4] = (c ushr 24).toByte()
        out[n + 5] = (c ushr 16).toByte()
        out[n + 6] = (c ushr 8).toByte()
        out[n + 7] = c.toByte()
        return out
    }

    /** Parse one frame at pos (verifies CRC). Returns Frame or null on error. */
    fun frameNext(bytes: ByteArray, pos: Int): Frame? {
        if (bytes.size < pos + 8) return null
        val n = be32(bytes, pos)
        val end = pos + 8 + n
        if (bytes.size < end) return null
        val payload = bytes.copyOfRange(pos + 4, pos + 4 + n)
        val want = be32(bytes, end - 4)
        if (crc32(payload) != want) return null
        return Frame(payload, end)
    }

    /** Skip one frame at pos without copying or verifying (zero-copy). */
    fun frameSkip(bytes: ByteArray, pos: Int): Int? {
        if (bytes.size < pos + 8) return null
        val n = be32(bytes, pos)
        val end = pos + 8 + n
        if (bytes.size < end) return null
        return end
    }

    private fun be32(b: ByteArray, off: Int): Int =
        ((b[off].toInt() and 0xFF) shl 24) or ((b[off + 1].toInt() and 0xFF) shl 16) or
            ((b[off + 2].toInt() and 0xFF) shl 8) or (b[off + 3].toInt() and 0xFF)
}