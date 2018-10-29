package io.github.spair.jtgmerge.util;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum Separator {

    WIN("\r\n"),
    NIX("\n");

    private final String separatorChars;

    public String separator() {
        return separatorChars;
    }
}
