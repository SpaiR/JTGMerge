package io.github.spair.jtgmerge.command;

import io.github.spair.dmm.io.DmmData;
import io.github.spair.dmm.io.TileContent;
import io.github.spair.dmm.io.TileLocation;
import io.github.spair.dmm.io.reader.DmmReader;
import io.github.spair.jtgmerge.util.FileUtil;
import io.github.spair.jtgmerge.util.KeyGenerator;
import io.github.spair.jtgmerge.util.Separator;
import lombok.AllArgsConstructor;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Option;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Scanner;
import java.util.InputMismatchException;
import java.util.Objects;
import java.util.function.Consumer;

@Command(
        name = "merge",
        description = "Attempts to merge locally modified map with map from remote branch. Provides interactive conflict resolving.")
@SuppressWarnings("unused")
public class Merge implements Runnable {

    @Parameters(index = "0", paramLabel = "ORIGIN", description = "file with origin map (map before local changes)")
    private File origin;

    @Parameters(index = "1", paramLabel = "LOCAL", description = "file with local map (map after local changes)")
    private File local;

    @Parameters(index = "2", paramLabel = "REMOTE", description = "file with remote map (map from remote brunch)")
    private File remote;

    @Option(names = {"--separator"}, description = "Separator to split lines. Accepts: ${COMPLETION-CANDIDATES}")
    private Separator separator = null;

    private DmmData originDmmData;
    private DmmData localDmmData;
    private DmmData remoteDmmData;
    private DmmData resultDmmData;

    private Scanner in = new Scanner(System.in);
    private Set<TileContent> contentWithoutKeys = new HashSet<>();

    @Override
    public void run() {
        System.out.println("Map merging in process...");

        originDmmData = DmmReader.readMap(origin);
        localDmmData = DmmReader.readMap(local);
        remoteDmmData = DmmReader.readMap(remote);

        if (differMapSizes()) {
            System.out.println("ERROR: Map sizes differ. Unable to merge. Aborting!");
            System.exit(1);
        }

        if (differKeyLength()) {
            System.out.println("ERROR: Map key lengths differ. Unable to merge. Aborting!");
            System.exit(1);
        }

        initResultDmmData();
        fillMapWithContent();
        spreadContentWithoutKeys();

        if (resultDmmData.isTgm()) {
            resultDmmData.saveAsTGM(local);
        } else {
            resultDmmData.saveAsByond(local);
        }

        FileUtil.convertLineEndings(local, separator);

        System.out.println("Map merging successfully finished");
    }

    private boolean differMapSizes() {
        return originDmmData.getMaxX() != localDmmData.getMaxX()
                || originDmmData.getMaxX() != remoteDmmData.getMaxX()
                || localDmmData.getMaxX() != remoteDmmData.getMaxX()
                || originDmmData.getMaxY() != localDmmData.getMaxY()
                || originDmmData.getMaxY() != remoteDmmData.getMaxY()
                || localDmmData.getMaxY() != remoteDmmData.getMaxY();
    }

    private boolean differKeyLength() {
        return originDmmData.getKeyLength() != localDmmData.getKeyLength()
                || originDmmData.getKeyLength() != remoteDmmData.getKeyLength()
                || localDmmData.getKeyLength() != remoteDmmData.getKeyLength();
    }

    private void initResultDmmData() {
        resultDmmData = new DmmData();
        resultDmmData.setKeyLength(localDmmData.getKeyLength());
        resultDmmData.setMaxX(localDmmData.getMaxX());
        resultDmmData.setMaxY(localDmmData.getMaxY());
        resultDmmData.setTgm(localDmmData.isTgm());
    }

    private void fillMapWithContent() {
        List<Change> remoteChanges = new ArrayList<>();
        List<Change> localChanges = new ArrayList<>();

        for (int x = 1; x <= originDmmData.getMaxX(); x++) {
            for (int y = 1; y <= originDmmData.getMaxY(); y++) {
                val location = TileLocation.of(x, y);

                val localKey = localDmmData.getKeyByLocation(location);
                val remoteKey = remoteDmmData.getKeyByLocation(location);

                val originTileContent = originDmmData.getTileContentByLocation(location);
                val localTileContent = localDmmData.getTileContentByLocation(location);
                val remoteTileContent = remoteDmmData.getTileContentByLocation(location);

                val originMatchesLocal = compareTileContents(originTileContent, localTileContent);
                val originMatchesRemote = compareTileContents(originTileContent, remoteTileContent);
                val remoteMatchesLocal = compareTileContents(remoteTileContent, localTileContent);

                if (!originMatchesLocal && !originMatchesRemote && !remoteMatchesLocal) {
                    System.out.println("==== CONFLICT ====\n"
                            + "X: " + x + " Y: " + y + "\n"
                            + "--- Local\n"
                            + "Key: " + localKey + '\n'
                            + "Content: " + localTileContent + '\n'
                            + "--- Remote\n"
                            + "Key: " + remoteKey + '\n'
                            + "Content: " + remoteTileContent
                    );

                    switch (readResolveMode()) {
                        case 1:
                            localChanges.add(new Change(location, localKey, localTileContent));
                            System.out.println("Local version is used");
                            break;
                        case 2:
                            remoteChanges.add(new Change(location, remoteKey, remoteTileContent));
                            System.out.println("Remote version is used");
                            break;
                        case 0:
                            System.out.println("Aborted by user");
                            System.exit(2);
                        default:
                            System.out.println("ERROR: Incorrect conflict resolution mode. Aborting!");
                            System.exit(1);
                    }
                } else {
                    if (!originMatchesLocal) {
                        localChanges.add(new Change(location, localKey, localTileContent));
                    } else {
                        remoteChanges.add(new Change(location, remoteKey, remoteTileContent));
                    }
                }
            }
        }

        Consumer<Change> addToResultDmm = change -> {
            resultDmmData.addTileContentByLocation(change.location, change.content);
            if (resultDmmData.hasTileContentByKey(change.key) && !resultDmmData.hasKeyByTileContent(change.content)) {
                contentWithoutKeys.add(change.content);
            } else {
                resultDmmData.addKeyAndTileContent(change.key, change.content);
            }
        };

        remoteChanges.forEach(addToResultDmm);
        localChanges.forEach(addToResultDmm);
    }

    private void spreadContentWithoutKeys() {
        if (contentWithoutKeys.isEmpty()) {
            return;
        }
        val keyGenerator = new KeyGenerator(resultDmmData);
        contentWithoutKeys.forEach(tileContent -> resultDmmData.addKeyAndTileContent(keyGenerator.createKey(), tileContent));
    }

    private boolean compareTileContents(final TileContent tc1, final TileContent tc2) {
        return Objects.equals(tc1, tc2);
    }

    private int readResolveMode() {
        System.out.println("Please select number of version which should be used:\n 1 - local\n 2 - remote\n 0 - abort");
        while (true) {
            try {
                System.out.print(">> ");
                return in.nextInt();
            } catch (InputMismatchException ignored) {
            }
        }
    }

    @AllArgsConstructor
    private static final class Change {
        private final TileLocation location;
        private final String key;
        private final TileContent content;
    }
}
