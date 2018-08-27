package io.github.spair.jtgmerge.command;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;

@Command(
        name = "convert",
        description = "Converts map to TGM or to initial BYOND format or swaps them.")
public class ConvertMap implements Runnable {

    private static final String TGM = "tgm";
    private static final String BYOND = "byond";

    @Parameters(index = "0", paramLabel = "MAP_FILE", description = "map file to convert")
    private File mapFile;

    @Option(names = {"-f", "--format"}, description = "Format to convert map ('tgm' or 'byond').")
    private String format;

    @Override
    public void run() {
        if (TGM.equalsIgnoreCase(format)) {
            // TODO
        } else if (BYOND.equalsIgnoreCase(format)) {
            // TODO
        } else {
            // TODO
        }
    }
}
