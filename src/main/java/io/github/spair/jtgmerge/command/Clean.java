package io.github.spair.jtgmerge.command;

import io.github.spair.dmm.io.DmmData;
import io.github.spair.dmm.io.TileLocation;
import io.github.spair.dmm.io.reader.DmmReader;
import io.github.spair.jtgmerge.util.FileUtil;
import io.github.spair.jtgmerge.util.KeyGenerator;
import io.github.spair.jtgmerge.util.Separator;
import lombok.val;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Command;

import java.io.File;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

@Command(name = "clean", description = "Cleans a map after changes have been made.")
@SuppressWarnings({"FieldCanBeLocal", "unused", "MismatchedReadAndWriteOfArray"})
public class Clean implements Runnable {

    @Parameters(index = "0", paramLabel = "ORIGINAL", description = "file with original map")
    private File original;

    @Parameters(index = "1", paramLabel = "MODIFIED", description = "file with modified map")
    private File modified;

    @Parameters(
            index = "2", paramLabel = "OUTPUT",
            description = "file to output result (if not provided @|yellow MODIFIED|@ will be used)",
            arity = "0..1")
    private File output;

    @Option(
            names = "--tgm",
            description = "result will be saved in TGM format (default: ${DEFAULT-VALUE})",
            arity = "1")
    private boolean tgm = true;

    @Option(
            names = {"-S", "--sanitize"},
            description = "variables to remove from every object on the map",
            arity = "0..*")
    private String[] sanitizeVars = {};

    @Option(names = {"--separator"}, description = "Separator to split lines. Accepts: ${COMPLETION-CANDIDATES}")
    private Separator separator = null;

    private DmmData originalDmmData;
    private DmmData modifiedDmmData;
    private DmmData outputDmmData;

    private Set<String> unusedKeys;
    private KeyGenerator keyGenerator;

    @Override
    public void run() {
        System.out.printf("Cleaning map '%s', tgm format %s\n", modified.getName(), tgm ? "enabled" : "disabled");

        if (output == null) {
            output = modified;
        }

        originalDmmData = DmmReader.readMap(original);
        modifiedDmmData = DmmReader.readMap(modified);

        if (originalDmmData.getKeyLength() != modifiedDmmData.getKeyLength()) {
            System.out.println("ERROR: Key length of original and new map differs");
            System.exit(1);
        }

        initOutputDmmData();

        unusedKeys = new HashSet<>(originalDmmData.getKeys());
        keyGenerator = new KeyGenerator(outputDmmData);

        sanitizeVars();

        fillMapWithReusedKeys();
        fillRemainingTiles();

        if (tgm) {
            outputDmmData.saveAsTGM(output);
        } else {
            outputDmmData.saveAsByond(output);
        }

        FileUtil.convertLineEndings(output, separator);

        System.out.printf("Map '%s' successfully cleaned, output path: '%s'\n", modified.getName(), output.getPath());
    }

    private void initOutputDmmData() {
        outputDmmData = new DmmData();
        outputDmmData.setMaxX(modifiedDmmData.getMaxX());
        outputDmmData.setMaxY(modifiedDmmData.getMaxY());
        outputDmmData.setKeyLength(modifiedDmmData.getKeyLength());
    }

    private void fillMapWithReusedKeys() {
        for (int x = 1; x <= outputDmmData.getMaxX(); x++) {
            for (int y = 1; y <= outputDmmData.getMaxY(); y++) {
                val location = TileLocation.of(x, y);
                val newTileContent = modifiedDmmData.getTileContentByLocation(location);
                val originalKey = originalDmmData.getKeyByTileContent(newTileContent);

                if (!outputDmmData.hasKeyByTileContent(newTileContent) && originalKey != null) {
                    outputDmmData.addKeyAndTileContent(originalKey, newTileContent);
                    unusedKeys.remove(originalKey);
                }

                outputDmmData.addTileContentByLocation(location, newTileContent);
            }
        }
    }

    private void fillRemainingTiles() {
        for (int y = outputDmmData.getMaxY(); y > 0; y--) {
            for (int x = 1; x <= outputDmmData.getMaxX(); x++) {
                val location = TileLocation.of(x, y);
                val tileContent = modifiedDmmData.getTileContentByLocation(location);

                if (!outputDmmData.hasKeyByTileContent(tileContent)) {
                    String key = null;

                    if (unusedKeys.isEmpty()) {
                        key = keyGenerator.createKey();
                    } else {
                        for (val unusedKey : unusedKeys) {
                            if (originalDmmData.getKeyByLocation(location).equals(unusedKey)) {
                                key = unusedKey;
                                unusedKeys.remove(key);
                                break;
                            }
                        }

                        if (key == null) {
                            val it = unusedKeys.iterator();
                            key = it.next();
                            it.remove();
                        }
                    }

                    outputDmmData.addKeyAndTileContent(key, tileContent);
                }
            }
        }
    }

    private void sanitizeVars() {
        for (val sanitizeVar : sanitizeVars) {
            for (val tileContent : new HashSet<>(modifiedDmmData.getKeysByTileContent().keySet())) {
                val key = modifiedDmmData.removeKeyByTileContent(tileContent);
                tileContent.forEach(tileObject -> tileObject.removeVar(sanitizeVar));

                if (modifiedDmmData.hasKeyByTileContent(tileContent)) {
                    modifiedDmmData.removeTileContentByKey(key);
                } else {
                    modifiedDmmData.addKeyByTileContent(tileContent, key);
                }
            }
        }

        if (sanitizeVars.length > 0) {
            System.out.println("Sanitized variables: " + Arrays.toString(sanitizeVars));
        }
    }
}
