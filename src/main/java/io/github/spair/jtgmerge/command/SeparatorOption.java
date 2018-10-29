package io.github.spair.jtgmerge.command;

import io.github.spair.jtgmerge.util.Separator;
import picocli.CommandLine.Option;

final class SeparatorOption {

    @Option(names = {"--separator"}, description = "Separator to split lines. Accepts: ${COMPLETION-CANDIDATES}")
    Separator separator = null;
}
