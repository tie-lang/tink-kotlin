# tink-kotlin

tink data-flow node frame protocol — Kotlin (JVM / multiplatform) object (no
dependencies, stdlib-only). Universal and language-agnostic: any component
that obeys the frame protocol can join a tink pipeline.

```
帧 = [ len: u32 BE ][ payload: len 字节 ][ crc: u32 BE ]
len = payload 字节数
crc = CRC32-IEEE(payload)（多项式 0xEDB88320）
```

Mirrors `std/tink.tie` (tie standard library) and the other-language tink
libraries; pure functions over `ByteArray`, IO (stdin/stdout) left to the
caller. Names follow the Kotlin convention (object `Tink`, camelCase methods).

## API (`object Tink`)

| function | description |
| --- | --- |
| `crc32(data: ByteArray): Int` | CRC32-IEEE over a byte array. Check vector: `crc32("123456789") = 0xCBF43926` |
| `frameEncode(payload: ByteArray): ByteArray` | encode a payload into a full frame `[len][payload][crc]` |
| `frameNext(bytes, pos): Frame?` | parse one frame at `pos`, verify CRC; `Frame(payload, next)` on success, `null` on out-of-bounds / mismatch |
| `frameSkip(bytes, pos): Int?` | skip one frame at `pos` without copying or verifying; `null` on out-of-bounds |

## Usage

```kotlin
val frame = Tink.frameEncode(byteArrayOf(1, 2, 3))
val got = Tink.frameNext(frame, 0) // Tink.Frame?
```

## Build & test

```bash
kotlinc tink.kt test_tink.kt -include-runtime -d test.jar
java -jar test.jar
```

## Cross-language

tink 帧协议各语言实现（API 语义与校验向量一致）：

| language | library |
| --- | --- |
| tie | `std/tink.tie` |
| Rust | `tink-rust`（tink crate） |
| C | `tink-c`（`tink.h` + `tink.c`） |
| Python | `tink-python`（`tink.py`） |
| JavaScript | `tink-js`（`tink.js` + `tink.d.ts`） |
| C++ | `tink-cpp`（`tink.hpp`） |
| Java | `tink-java`（`org.tielang.tink`） |
| C# | `tink-csharp`（namespace `Tink`） |
| Go | `tink-go`（package `tink`） |
| Zig | `tink-zig`（`tink.zig`） |
| Lua | `tink-lua`（`tink.lua`） |
| GDScript | `tink-godot`（`tink.gd`） |
| F# | `tink-fsharp`（`Tink.fs`） |
| PowerShell | `tink-powershell`（`tink.ps1`） |
| Kotlin | this library（`tink-kotlin`） |

## License

本仓库使用 **Tie Public License v1.2 (TPL 1.2)**，完整文本见 [LICENSE](LICENSE)。
This repository is distributed under the **Tie Public License v1.2 (TPL 1.2)** — see [LICENSE](LICENSE) for the full text.