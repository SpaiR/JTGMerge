package io.github.spair.jtgmerge.command;

import io.github.spair.dmm.io.reader.DmmReader;
import io.github.spair.jtgmerge.util.FileUtil;
import io.github.spair.jtgmerge.util.Separator;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;

@Command(
        name = "convert",
        description = "Converts map to TGM or to initial BYOND format, or changes the format to the opposite.")
@SuppressWarnings("unused")
public class Convert implements Runnable {

    private static final String TGM = "tgm";
    private static final String BYOND = "byond";

    @Parameters(index = "0", paramLabel = "MAP_FILE", description = "map file to convert")
    private File mapFile;

    @Option(names = {"-f", "--format"}, description = "format to convert to (accepts 'tgm' or 'byond' as value)")
    private String format;

    @Option(names = {"--separator"}, description = "Separator to split lines. Accepts: ${COMPLETION-CANDIDATES}")
    private Separator separator = null;

    @Override
    public void run() {
        val dmmData = DmmReader.readMap(mapFile);

        if (format == null) {
            format = dmmData.isTgm() ? BYOND : TGM;
        }

        System.out.printf("Converting '%s' to '%s'\n", mapFile.getName(), format);

        if (TGM.equalsIgnoreCase(format)) {
            dmmData.saveAsTGM(mapFile);
        } else if (BYOND.equalsIgnoreCase(format)) {
            dmmData.saveAsByond(mapFile);
        } else {
            System.out.println("ERROR: Unknown format");
            System.exit(1);
        }

        FileUtil.convertLineEndings(mapFile, separator);

        System.out.printf("Map '%s' successfully converted to '%s'\n", mapFile.getName(), format);
    }
}
