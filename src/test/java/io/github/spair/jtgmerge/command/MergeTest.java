package io.github.spair.jtgmerge.command;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class MergeTest {

    private Merge merge;

    @Before
    public void setUp() {
        merge = new Merge();
    }

    @Test
    public void testRun() {
        File origin = ResourceUtil.readResourceFile("merge_origin.dmm");
        File local = ResourceUtil.readResourceFile("merge_local.dmm");
        File remote = ResourceUtil.readResourceFile("merge_remote.dmm");
        File expected = ResourceUtil.readResourceFile("merge_result.dmm");

        File result = new File("merge.dmm.result");
        result.deleteOnExit();
        ResourceUtil.copy(local, result);

        FieldUtil.setField(merge, "origin", origin);
        FieldUtil.setField(merge, "local", result);
        FieldUtil.setField(merge, "remote", remote);

        merge.run();

        AssertUtil.assertFiles(expected, result);

        result.delete();
    }
}
