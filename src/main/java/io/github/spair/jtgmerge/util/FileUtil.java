package io.github.spair.jtgmerge.util;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public final class FileUtil {

    public static void convertLineEndings(final File file, final Separator separator) {
        if (separator == null) {
            return;
        }

        try {
            String fileContent = new String(Files.readAllBytes(file.toPath()));
            fileContent = fileContent.replace(System.lineSeparator(), separator.separatorChar());
            Files.write(file.toPath(), fileContent.getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private FileUtil() {
    }
}
