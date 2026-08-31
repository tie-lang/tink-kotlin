// test_tink.kt —— unit tests for tink.kt. Run: kotlinc tink.kt test_tink.kt -include-runtime -d test.jar && java -jar test.jar
import java.nio.charset.StandardCharsets

fun main() {
    var failures = 0
    fun check(cond: Boolean, name: String) {
        if (cond) println("[PASS] $name") else {
            failures++
            println("[FAIL] $name")
        }
    }

    // crc32 check vector
    check(Tink.crc32("123456789".toByteArray(StandardCharsets.UTF_8)) == 0xCBF43926.toInt(), "crc32 vector")

    // frame roundtrip
    val p = byteArrayOf(1, 2, 3)
    val frame = Tink.frameEncode(p)
    check(frame.size == p.size + 8, "frame length")
    val got = Tink.frameNext(frame, 0)
    check(got != null, "frame present")
    if (got != null) {
        check(got.next == frame.size, "frame next == length")
        check(got.payload.contentEquals(p), "frame payload roundtrip")
    }

    // empty frame roundtrip
    val fe = Tink.frameEncode(ByteArray(0))
    val ge = Tink.frameNext(fe, 0)
    check(ge != null && ge.next == fe.size && ge.payload.isEmpty(), "empty frame roundtrip")

    // CRC tamper rejected
    val ft = Tink.frameEncode(p)
    ft[4] = (ft[4].toInt() + 1).toByte() // tamper payload[0]
    check(Tink.frameNext(ft, 0) == null, "crc tamper rejected")

    // frameSkip matches length
    val fs = Tink.frameEncode(p)
    check(Tink.frameSkip(fs, 0) == fs.size, "frameSkip matches length")

    // out of bounds
    check(Tink.frameNext(frame, frame.size) == null, "frameNext out of bounds")
    check(Tink.frameSkip(frame, frame.size) == null, "frameSkip out of bounds")
    check(Tink.frameNext(ByteArray(0), 0) == null, "frameNext empty input")

    if (failures > 0) {
        println("$failures checks FAILED")
        kotlin.system.exitProcess(1)
    }
    println("all tests passed")
}