package io.github.spair.jtgmerge.util;

import io.github.spair.dmm.io.DmmData;
import lombok.val;
import lombok.var;

public final class KeyGenerator {

    private static final int LIMIT = 65530;
    private static final int BASE = 52;

    private static final char[] BASE_52_KEYS = {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
            'u', 'v', 'w', 'x', 'y', 'z', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
            'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z'
    };

    private final DmmData dmmData;

    public KeyGenerator(final DmmData dmmData) {
        this.dmmData = dmmData;
    }

    public String createKey() {
        var freeKeys = Math.min(LIMIT, (Math.pow(BASE, dmmData.getKeyLength()))) - dmmData.getKeys().size();
        var num = 1;

        while (freeKeys > 0) {
            if (!dmmData.hasTileContentByKey(numToKey(num)))
                break;
            num++;
            freeKeys--;
        }

        if (num > LIMIT)
            throw new IllegalStateException("Maximum available num values is 65530. Current num: " + num);

        return numToKey(num);
    }

    private String numToKey(final int num) {
        StringBuilder result = new StringBuilder();
        int currentNum = num;

        while (currentNum > 0) {
            result.insert(0, BASE_52_KEYS[currentNum % BASE]);
            currentNum = (int) Math.floor(((double) currentNum) / BASE);
        }

        val keyLength = dmmData.getKeyLength();
        if (result.length() > keyLength)
            throw new IllegalStateException("Key length cannot less than result length");

        val lengthDiff = keyLength - result.length();
        if (lengthDiff != 0) {
            for (int i = 0; i < lengthDiff; i++) {
                result.insert(0, BASE_52_KEYS[0]);
            }
        }

        return result.toString();
    }
}
