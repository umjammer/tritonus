[![Release](https://jitpack.io/v/umjammer/tritonus.svg)](https://jitpack.io/#umjammer/tritonus)
[![Java CI](https://github.com/umjammer/tritonus/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/tritonus/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/tritonus/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/tritonus/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-17-b07219)
[![Parent](https://img.shields.io/badge/Parent-vavi--sound--sandbox-pink)](https://github.com/umjammer/vavi-sound-sandbox)

# tritonus

<img alt="tritone logo" src="https://github.com/umjammer/tritonus/assets/493908/11bc60d6-643f-47ba-a8b1-d451056ed247" width="320" />

♪ This is mavenized Tritonus.

Tritonus is an implementation of the Java Sound API and several Java Sound SPI.<br/>
For original versions of these components, see: http://www.tritonus.org/

All modules are implemented in jna or pure Java.

| module        |      status      | spi <sup>[1]</sup> | comment                 | library                                                 |
|---------------|:----------------:|:------------------:|-------------------------|---------------------------------------------------------|
| share         |        ✅         |                    |                         |                                                         |
| remaining     |        ✅         |     `RWC--IO-`     |                         |                                                         |
| dsp           |        ✅         |                    |                         |                                                         |
| core          |        ✅         |                    |                         |                                                         |
| gsm           |        ✅         |     `RWC-----`     |                         |                                                         |
| javasequencer |        ✅         |     `----D---`     |                         |                                                         |
| jorbis        |        ✅         |     `R-C-----`     | pure                    | [jorbis](http://www.jcraft.com/jorbis/)                 |
| midishare     |        ✅         |     `----D---`     |                         |                                                         |
| mp3           |        ✅         |     `RWC-----`     | jna                     | brew:lame, [jlayer](https://github.com/umjammer/jlayer) |
| esd           |        🚫        |     `---M----`     | linux only              | libesd                                                  |
| alsa          |        🚫        |     `---MD---`     | linux only              | libasound                                               |
| vorbis        | ✅<sup>[2]</sup>  |     `RWC-----`     | jna                     | brew:libvorbis                                          |
| pvorbis       |        ✅         |     `-WC-----`     | pure                    | [jVorbisEnc](https://github.com/umjammer/jVorbisEnc)    |
| cdda          |        🚫        |                    | linux only              | libcdda_interface libcdda_paranoia                      |
| fluidsynth    |        ✅         |     `----D---`     | jna                     | brew:fluid-synth                                        |
| src           |        ✅         |     `--C-----`     | sampling rate converter |                                                         |
| aos           |        ✅         |                    |                         |                                                         |
| saol          |        🚧        |                    |                         |                                                         |
| test          |        🚧        |                    |                         |                                                         |
| timidity      |        🚧        |                    |                         | [libtimidity](https://github.com/sezero/libtimidity)    |

<sub>[1] R: reader, W: writer, C: converter, M: mixer device, D: midi driver, I: midi reader, O: midi writer, B: sound bank SPI</sub><br/>
<sub>[2] unstable, use jorbis, pvprbis</sub>

## Installation

### natives

 * lame
 * fluid-synth
 * libvorbis

e.g.
```shell
$ brew install lame fluid-synth libvorbis
```

### maven

* https://jitpack.io/#umjammer/tritonus

### jvm option

e.g. `-Djna.native.path=/opt/homebrew/lib`

## License

Tritonus is distributed under the terms of the Apache License,
Version 2.0. See the file [LICENSE](LICENSE) for details.

### License Exceptions

- the low level GSM code (package org.tritonus.lowlevel.gsm)
  is licensed under the GNU GPL
- BladeMP3EncDLL.h for Windows is licensed under the GNU LGPL.
- the pvorbis lib is licensed under a BSD style license

## References

### fluidsynth

* https://github.com/atsushieno/fluidsynth-midi-service-j (android)

### lame

* https://github.com/henkelmax/lame4j (jni)
* https://sourceforge.net/projects/lamejb/ (jna)
* https://openinnowhere.sourceforge.net/lameonj/ (jni)
* https://github.com/nwaldispuehl/java-lame (pure)

### vorbis

 * http://fmj-sf.net/theora-java/getting_started.php (jna)
 * https://github.com/stephengold/j-ogg-all (pure)

### pvorbis

 * [jVorbisEnc](https://src.fedoraproject.org/repo/pkgs/jVorbisEnc/) (pure) ... [patched](https://github.com/umjammer/jVorbisEnc)

## TODO

* ~~deploy to bintray via github actions~~
* timidity (lost by disk crash)
* ~~use jna instead of jni~~
  * sub modules for linux only 
* make logging use lazy evaluation 
