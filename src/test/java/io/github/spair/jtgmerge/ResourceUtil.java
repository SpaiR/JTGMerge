package io.github.spair.jtgmerge;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public final class ResourceUtil {

    public static File readResourceFile(final String path) {
        return new File(Objects.requireNonNull(ResourceUtil.class.getClassLoader().getResource(path)).getFile());
    }

    public static void copy(final File src, final File dest) {
        try {
            Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
