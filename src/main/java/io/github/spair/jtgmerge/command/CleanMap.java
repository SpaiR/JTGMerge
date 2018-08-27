package io.github.spair.jtgmerge.command;

import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;

import static picocli.CommandLine.Command;

@Command(name = "clean", description = "Cleans a map after changes have been made.")
public class CleanMap implements Runnable {

    @Parameters(index = "0", paramLabel = "ORIGINAL", description = "file with original map")
    private File original;

    @Parameters(index = "1", paramLabel = "MODIFIED", description = "file with modified map")
    private File modified;

    @Parameters(
            index = "2", paramLabel = "OUTPUT",
            description = "file to output result (if not provided @|yellow MODIFIED|@ is used)",
            arity = "0..1")
    private File output;

    @Option(
            names = "--tgm",
            description = "Result will be saved in TGM format (default: ${DEFAULT-VALUE}).",
            arity = "1")
    private boolean tgm = true;

    @Override
    public void run() {
        if (output == null) {
            output = modified;
        }
        // TODO
    }
}
