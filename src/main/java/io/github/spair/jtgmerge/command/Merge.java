package io.github.spair.jtgmerge.command;

import io.github.spair.dmm.io.DmmData;
import io.github.spair.dmm.io.TileContent;
import io.github.spair.dmm.io.TileLocation;
import io.github.spair.dmm.io.reader.DmmReader;
import io.github.spair.dmm.io.writer.DmmWriter;
import lombok.val;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.Callable;

@Command(
        name = "merge",
        description = "Attempts to merge locally modified map with map from remote branch."
                    + "Provides interactive conflict resolving.")
public class Merge implements Callable<Integer> {

    private static final Integer SUCCESS = 0;
    private static final Integer FAIL = 1;

    @Parameters(index = "0", paramLabel = "ORIGIN", description = "file with origin map (map before local changes)")
    private File origin;

    @Parameters(index = "1", paramLabel = "LOCAL", description = "file with local map (map after local changes)")
    private File local;

    @Parameters(index = "2", paramLabel = "REMOTE", description = "file with remote map (map from remote brunch)")
    private File remote;

    private DmmData originDmmData;
    private DmmData localDmmData;
    private DmmData remoteDmmData;
    private DmmData resultDmmData;

    private Scanner in = new Scanner(System.in);

    @Override
    public Integer call() {
        originDmmData = DmmReader.readMap(origin);
        localDmmData = DmmReader.readMap(local);
        remoteDmmData = DmmReader.readMap(remote);

        if (differMapSizes()) {
            System.out.println("ERROR: Map sizes differ. Unable to merge. Aborting.");
            return FAIL;
        }

        if (differKeyLength()) {
            System.out.println("ERROR: Map key lengths differ. Unable to merge. Aborting.");
            return FAIL;
        }

        initResultDmmData();

        for (int x = 1; x <= originDmmData.getMaxX(); x++) {
            for (int y = 1; y <= originDmmData.getMaxY(); y++) {
                val location = TileLocation.of(x, y);

                val originTileContent = originDmmData.getTileContentByLocation(location);
                val localTileContent = localDmmData.getTileContentByLocation(location);
                val remoteTileContent = remoteDmmData.getTileContentByLocation(location);

                val originMatchesLocal = compareTileContents(originTileContent, localTileContent);
                val originMatchesRemote = compareTileContents(originTileContent, remoteTileContent);
                val remoteMatchesLocal = compareTileContents(remoteTileContent, localTileContent);

                String key = null;
                TileContent tileContent = null;

                if (!originMatchesLocal && !originMatchesRemote && !remoteMatchesLocal) {
                    System.out.printf("CONFLICT: X=%d, Y=%d\n", x, y);

                    switch (readResolveMode()) {
                        case 1:
                            key = localDmmData.getKeyByTileContent(localTileContent);
                            tileContent = localTileContent;
                            break;
                        case 2:
                            key = remoteDmmData.getKeyByTileContent(remoteTileContent);
                            tileContent = remoteTileContent;
                            break;
                        case 0:
                            System.exit(FAIL);
                        default:
                            System.out.println("Incorrect conflict resolution mode! Aborting.");
                            System.exit(FAIL);
                    }
                } else if (!originMatchesLocal) {
                    key = localDmmData.getKeyByTileContent(localTileContent);
                    tileContent = localTileContent;
                } else if (!originMatchesRemote) {
                    key = remoteDmmData.getKeyByTileContent(remoteTileContent);
                    tileContent = remoteTileContent;
                } else {
                    key = originDmmData.getKeyByTileContent(originTileContent);
                    tileContent = originTileContent;
                }

                resultDmmData.addTileContentByLocation(location, tileContent);
                resultDmmData.addTileContentByKey(key, tileContent);
                resultDmmData.addKeyByTileContent(tileContent, key);
            }
        }

        if (resultDmmData.isTgm()) {
            DmmWriter.saveAsTGM(local, resultDmmData);
        } else {
            DmmWriter.saveAsByond(local, resultDmmData);
        }

        return SUCCESS;
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

    private boolean compareTileContents(final TileContent tc1, final TileContent tc2) {
        return tc1 == null ? tc2 == null : tc1.equals(tc2);
    }

    private int readResolveMode() {
        System.out.println(
                "A conflict has been detected. Please specify which version should be used (example: 1)\n"
              + " 1 - local\n 2 - remote\n 0 - abort"
        );

        while (true) {
            try {
                System.out.print(">> ");
                return in.nextInt();
            } catch (InputMismatchException ignored) {
            }
        }
    }
}
