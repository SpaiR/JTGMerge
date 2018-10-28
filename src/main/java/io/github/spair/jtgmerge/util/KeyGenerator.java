package io.github.spair.jtgmerge.util;

import io.github.spair.dmm.io.DmmData;
import lombok.val;

public final class KeyGenerator {

    private static final char[] VALID_KEY_ELEMENTS = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    private final DmmData dmmData;
    private int keyGeneratorCurrentId;

    public KeyGenerator(final DmmData dmmData) {
        this.dmmData = dmmData;
        keyGeneratorCurrentId = (int) Math.pow(VALID_KEY_ELEMENTS.length, dmmData.getKeyLength() - 1);
    }

    public String createKey() {
        while (true) {
            int localId = keyGeneratorCurrentId++;
            val generatedKey = new StringBuilder();

            while (localId >= VALID_KEY_ELEMENTS.length) {
                int i = localId % VALID_KEY_ELEMENTS.length;
                generatedKey.insert(0, VALID_KEY_ELEMENTS[i]);
                localId -= i;
                localId /= VALID_KEY_ELEMENTS.length;
            }

            generatedKey.append(VALID_KEY_ELEMENTS[localId]);

            if (dmmData.hasTileContentByKey(generatedKey.toString())) {
                continue;
            }

            if (generatedKey.length() == dmmData.getKeyLength()) {
                return generatedKey.toString();
            } else {
                System.out.println("ERROR: Generated key is outside of bounds");
                System.exit(1);
            }
        }
    }
}
