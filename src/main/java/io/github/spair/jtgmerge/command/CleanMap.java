package io.github.spair.jtgmerge.command;

import io.github.spair.dmm.io.DmmData;
import io.github.spair.dmm.io.TileLocation;
import io.github.spair.dmm.io.reader.DmmReader;
import io.github.spair.dmm.io.writer.DmmWriter;
import lombok.val;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.Set;

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
            description = "result will be saved in TGM format (default: ${DEFAULT-VALUE})",
            arity = "1")
    private boolean tgm = true;

    private final String[] validKeyElements = {
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t",
            "u", "v", "w", "x", "y", "z", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N",
            "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
    };

    private DmmData originalDmmData;
    private DmmData modifiedDmmData;
    private DmmData outputDmmData;

    private int keyGeneratorCurrentId;

    @Override
    public void run() {
        System.out.println(
                String.format("Cleaning map '%s', tgm format is %s", modified.getName(), tgm ? "enabled" : "disabled")
        );

        if (output == null) {
            output = modified;
        }

        initDmmData();

        val unusedKeys = fillMapWithReusedKeys();
        fillRemainingTiles(unusedKeys);

        if (tgm) {
            DmmWriter.saveAsTGM(output, outputDmmData);
        } else {
            DmmWriter.saveAsByond(output, outputDmmData);
        }

        System.out.println(
                String.format("Map '%s' successfully cleaned, output path: %s", modified.getName(), output.getPath())
        );
    }

    private void initDmmData() {
        originalDmmData = DmmReader.readMap(original);
        modifiedDmmData = DmmReader.readMap(modified);

        if (originalDmmData.getKeyLength() != modifiedDmmData.getKeyLength()) {
            throw new RuntimeException("Key length of original and new map differs.");
        }

        outputDmmData = new DmmData();
        outputDmmData.setMaxX(modifiedDmmData.getMaxX());
        outputDmmData.setMaxY(modifiedDmmData.getMaxY());
        outputDmmData.setKeyLength(modifiedDmmData.getKeyLength());

        keyGeneratorCurrentId = (int) Math.pow(validKeyElements.length, outputDmmData.getKeyLength() - 1);
    }

    private Set<String> fillMapWithReusedKeys() {
        val unusedKeys = originalDmmData.getTileContentsByKey().keySet();

        for (int x = 1; x <= outputDmmData.getMaxX(); x++) {
            for (int y = 1; y <= outputDmmData.getMaxY(); y++) {
                val location = TileLocation.of(x, y);
                val newTileContent = modifiedDmmData.getTileContentByLocation(location);
                val originalKey = originalDmmData.getKeyByTileContent(newTileContent);

                if (outputDmmData.getKeyByTileContent(newTileContent) == null && originalKey != null) {
                    outputDmmData.addKeyByTileContent(newTileContent, originalKey);
                    outputDmmData.addTileContentByKey(originalKey, newTileContent);
                    unusedKeys.remove(originalKey);
                }

                outputDmmData.addTileContentByLocation(location, newTileContent);
            }
        }

        return unusedKeys;
    }

    private void fillRemainingTiles(final Set<String> unusedKeys) {
        for (int x = 1; x <= outputDmmData.getMaxX(); x++) {
            for (int y = 1; y <= outputDmmData.getMaxY(); y++) {
                val tileContent = modifiedDmmData.getTileContentByLocation(TileLocation.of(x, y));

                if (outputDmmData.getKeyByTileContent(tileContent) == null) {
                    String key;

                    if (unusedKeys.isEmpty()) {
                        key = generateNewKey();
                    } else {
                        val it = unusedKeys.iterator();
                        key = it.next();
                        it.remove();
                    }

                    outputDmmData.addKeyByTileContent(tileContent, key);
                    outputDmmData.addTileContentByKey(key, tileContent);
                }
            }
        }
    }

    private String generateNewKey() {
        while (true) {
            int localId = keyGeneratorCurrentId++;
            val generatedKey = new StringBuilder();

            while (localId >= validKeyElements.length) {
                int i = localId % validKeyElements.length;
                generatedKey.insert(0, validKeyElements[i]);
                localId -= i;
                localId /= validKeyElements.length;
            }

            generatedKey.append(validKeyElements[localId]);

            if (outputDmmData.getTileContentByKey(generatedKey.toString()) != null) {
                continue;
            }

            if (generatedKey.length() == outputDmmData.getKeyLength()) {
                return generatedKey.toString();
            } else {
                throw new RuntimeException("Generated key is outside of bounds.");
            }
        }
    }
}
