/*
 * Copyright (c) 2026 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.sound.midi.timidity;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.invoke.MethodHandle;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static java.lang.foreign.ValueLayout.ADDRESS;
import static java.lang.foreign.ValueLayout.JAVA_INT;


/**
 * TiMidity++ (libtimidity41) binding using the Foreign Function &amp; Memory API (JEP 454).
 * <p>
 * the native library keeps global state, so there is only one synthesizer per process.
 * <p>
 * system properties
 * <ul>
 *  <li>{@code tritonus.timidity.library} ... full path of the native library</li>
 *  <li>{@code java.library.path} ... directories searched for {@code libtimidity41.dylib (.so, .dll)}</li>
 * </ul>
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (nsano)
 * @version 0.00 2026-11-26 nsano initial version <br>
 * @see "timidity41/timidity/tim41_api.h"
 */
public final class TiMidity {

    /** the native library base name */
    public static final String LIBRARY_NAME = "timidity41";

    private static final Linker linker = Linker.nativeLinker();

    private static final MethodHandle tim41_version;
    private static final MethodHandle tim41_init;
    private static final MethodHandle tim41_close;
    private static final MethodHandle tim41_get_rate;
    private static final MethodHandle tim41_send_short;
    private static final MethodHandle tim41_send_sysex;
    private static final MethodHandle tim41_render;
    private static final MethodHandle tim41_reset;
    private static final MethodHandle tim41_set_verbosity;

    static {
        SymbolLookup lookup = SymbolLookup.libraryLookup(findLibrary(), Arena.global());

        tim41_version = downcall(lookup, "tim41_version", FunctionDescriptor.of(ADDRESS));
        tim41_init = downcall(lookup, "tim41_init", FunctionDescriptor.of(JAVA_INT, ADDRESS, ADDRESS, JAVA_INT));
        tim41_close = downcall(lookup, "tim41_close", FunctionDescriptor.ofVoid());
        tim41_get_rate = downcall(lookup, "tim41_get_rate", FunctionDescriptor.of(JAVA_INT));
        tim41_send_short = downcall(lookup, "tim41_send_short", FunctionDescriptor.ofVoid(JAVA_INT));
        tim41_send_sysex = downcall(lookup, "tim41_send_sysex", FunctionDescriptor.ofVoid(ADDRESS, JAVA_INT));
        tim41_render = downcall(lookup, "tim41_render", FunctionDescriptor.of(JAVA_INT, ADDRESS, JAVA_INT));
        tim41_reset = downcall(lookup, "tim41_reset", FunctionDescriptor.ofVoid());
        tim41_set_verbosity = downcall(lookup, "tim41_set_verbosity", FunctionDescriptor.ofVoid(JAVA_INT));
    }

    private static MethodHandle downcall(SymbolLookup lookup, String name, FunctionDescriptor descriptor) {
        MemorySegment symbol = lookup.find(name).orElseThrow(() -> new UnsatisfiedLinkError("symbol not found: " + name));
        return linker.downcallHandle(symbol, descriptor);
    }

    /** finds the native library path, returns a library name for the system search path if not found */
    private static Path findLibrary() {
        String explicit = System.getProperty("tritonus.timidity.library");
        if (explicit != null && !explicit.isEmpty()) {
            return Path.of(explicit);
        }
        String fileName = System.mapLibraryName(LIBRARY_NAME);
        String paths = System.getProperty("java.library.path", "");
        for (String dir : paths.split(java.io.File.pathSeparator)) {
            if (dir.isEmpty()) continue;
            Path candidate = Path.of(dir, fileName);
            if (Files.isRegularFile(candidate)) {
                return candidate;
            }
        }
        // let dlopen search (DYLD_LIBRARY_PATH, LD_LIBRARY_PATH, ...)
        return Path.of(fileName);
    }

    private TiMidity() {
    }

    private static RuntimeException rethrow(Throwable t) {
        if (t instanceof RuntimeException e) return e;
        if (t instanceof Error e) throw e;
        return new IllegalStateException(t);
    }

    /** @return native library version */
    public static String version() {
        try {
            MemorySegment p = (MemorySegment) tim41_version.invokeExact();
            return p.reinterpret(Long.MAX_VALUE).getString(0);
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    /**
     * @param cfgFile timidity.cfg path, nullable
     * @param cfgString configuration text, nullable
     * @param rate sample rate
     * @return 0: success
     */
    public static int init(String cfgFile, String cfgString, int rate) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment cfg = Optional.ofNullable(cfgFile).map(arena::allocateFrom).orElse(MemorySegment.NULL);
            MemorySegment str = Optional.ofNullable(cfgString).map(arena::allocateFrom).orElse(MemorySegment.NULL);
            return (int) tim41_init.invokeExact(cfg, str, rate);
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    public static void close() {
        try {
            tim41_close.invokeExact();
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    public static int getRate() {
        try {
            return (int) tim41_get_rate.invokeExact();
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    /** @param message status | data1 &lt;&lt; 8 | data2 &lt;&lt; 16 */
    public static void sendShort(int message) {
        try {
            tim41_send_short.invokeExact(message);
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    public static void sendShort(int status, int data1, int data2) {
        sendShort((status & 0xff) | ((data1 & 0x7f) << 8) | ((data2 & 0x7f) << 16));
    }

    /** @param data sysex message including leading 0xf0 */
    public static void sendSysex(byte[] data) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment buf = arena.allocate(data.length);
            MemorySegment.copy(data, 0, buf, java.lang.foreign.ValueLayout.JAVA_BYTE, 0, data.length);
            tim41_send_sysex.invokeExact(buf, data.length);
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    /**
     * @param buffer native buffer of at least {@code frames * 4} bytes,
     *               filled with signed 16bit native endian interleaved stereo
     * @return frames rendered, negative on error
     */
    public static int render(MemorySegment buffer, int frames) {
        try {
            return (int) tim41_render.invokeExact(buffer, frames);
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    public static void reset() {
        try {
            tim41_reset.invokeExact();
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }

    /** @param verbosity -1: silent, 0: normal, 1: verbose ... */
    public static void setVerbosity(int verbosity) {
        try {
            tim41_set_verbosity.invokeExact(verbosity);
        } catch (Throwable t) {
            throw rethrow(t);
        }
    }
}
