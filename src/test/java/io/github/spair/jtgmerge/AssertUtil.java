package io.github.spair.jtgmerge;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public final class AssertUtil {
    public static void assertFiles(final File f1, final File f2) {
        try {
            assertEquals(new String(Files.readAllBytes(f1.toPath()), StandardCharsets.UTF_8), new String(Files.readAllBytes(f2.toPath()), StandardCharsets.UTF_8));
            assertArrayEquals("files should be identical", Files.readAllBytes(f1.toPath()), Files.readAllBytes(f2.toPath()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
