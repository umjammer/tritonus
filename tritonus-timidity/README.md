# tritonus-timidity

a `javax.sound.midi.Synthesizer` SPI using [TiMidity++ (timidity41)](https://github.com/umjammer/timidity41)
through the Foreign Function & Memory API (JEP 454). no JNI, no JNA.

## native library

build `libtimidity41.dylib` from timidity41 (branch `vavi`, hash `648776c`), dependencies come from homebrew.

```shell
brew install cmake pkgconf flac libogg libvorbis opus speex mpg123 portaudio
cd timidity41
cmake -S . -B build-brew -DCMAKE_BUILD_TYPE=Release -DTIM41_X86_SIMD_LEVEL=NONE
cmake --build build-brew --target timidity41 -j
# -> build-brew/out/lib/libtimidity41.dylib, api: timidity/tim41_api.h
```

## usage

| system property | description |
|---|---|
| `java.library.path` | directory containing `libtimidity41.dylib` (or `tritonus.timidity.library` for a full path) |
| `tritonus.timidity.defaultsoundbank` | soundfont (sf2, sf3, sfz, dls) |
| `tritonus.timidity.cfg` | timidity.cfg (optional) |
| `tritonus.timidity.rate` | output sample rate, default 44100 |
| `tritonus.timidity.verbosity` | native log level, default -1 (silent) |

run java with `--enable-native-access=ALL-UNNAMED`.

```java
Synthesizer synthesizer = MidiSystem.getSynthesizer(); // -Djavax.sound.midi.Synthesizer=vavi.sound.midi.timidity.TiMiditySynthesizerProvider
synthesizer.open();
```

the native engine has global state, only one `TiMiditySynthesizer` can be open at a time.
`TiMiditySynthesizer#setLineEnabled(false)` + `#render(byte[], int)` renders offline without an audio line.

## test

copy `local.properties.sample` to `local.properties` and set paths, then `mvn test`.
