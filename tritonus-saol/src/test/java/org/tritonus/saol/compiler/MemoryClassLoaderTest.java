package org.tritonus.saol.compiler;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.junit.jupiter.api.Test;


class MemoryClassLoaderTest {

    @Test
    void test1() throws Exception {
        InputStream is = MemoryClassLoader.class.getResourceAsStream("/org/tritonus/saol/compiler/MemoryClassLoader.class");
        assert is != null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        while (true) {
            int read = is.read(buffer);
            if (read == -1) {
                break;
            }
            baos.write(buffer, 0, read);
        }
        MemoryClassLoader mcl = new MemoryClassLoader();
        Class<?> cls = mcl.findClass("org.tritonus.saol.compiler.MemoryClassLoader", baos.toByteArray());
System.out.println("class loaded: " + cls.getName());
        is.close();
    }
}