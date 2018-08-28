package io.github.spair.jtgmerge.command;

import io.github.spair.dmm.io.reader.DmmReader;
import io.github.spair.dmm.io.writer.DmmWriter;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;

@Command(
        name = "convert",
        description = "Converts map to TGM or to initial BYOND format, or changes the format to the opposite.")
public class Convert implements Runnable {

    private static final String TGM = "tgm";
    private static final String BYOND = "byond";

    @Parameters(index = "0", paramLabel = "MAP_FILE", description = "map file to convert")
    private File mapFile;

    @Option(names = {"-f", "--format"}, description = "format to convert to (accepts: tgm / byond)")
    private String format;

    @Override
    public void run() {
        val dmmData = DmmReader.readMap(mapFile);

        if (format == null) {
            format = dmmData.isTgm() ? BYOND : TGM;
        }

        System.out.printf("Converting '%s' to '%s'\n", mapFile.getName(), format);

        if (TGM.equalsIgnoreCase(format)) {
            DmmWriter.saveAsTGM(mapFile, dmmData);
        } else if (BYOND.equalsIgnoreCase(format)) {
            DmmWriter.saveAsByond(mapFile, dmmData);
        }

        System.out.printf("Map '%s' successfully converted to '%s'\n", mapFile.getName(), format);
    }
}
