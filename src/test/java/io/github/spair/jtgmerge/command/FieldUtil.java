package io.github.spair.jtgmerge.command;

import java.lang.reflect.Field;

public final class FieldUtil {
    public static void setField(final Object object, final String name, final Object value) {
        try {
            Field f = object.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(object, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
