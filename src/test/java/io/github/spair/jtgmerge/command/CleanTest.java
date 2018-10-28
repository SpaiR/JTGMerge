package io.github.spair.jtgmerge.command;

import io.github.spair.jtgmerge.AssertUtil;
import io.github.spair.jtgmerge.FieldUtil;
import io.github.spair.jtgmerge.ResourceUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class CleanTest {

    private Clean clean;

    @Before
    public void setUp() {
        clean = new Clean();
    }

    @Test
    public void testRun() {
        File beforeClean = ResourceUtil.readResourceFile("clean_before.dmm");
        File beforeCleanModified = ResourceUtil.readResourceFile("clean_before_modified.dmm");
        File result = new File("clean.dmm.result");
        result.deleteOnExit();

        FieldUtil.setField(clean, "original", beforeClean);
        FieldUtil.setField(clean, "modified", beforeCleanModified);
        FieldUtil.setField(clean, "output", result);
        FieldUtil.setField(clean, "sanitizeVars", new String[]{"step_x"});

        clean.run();

        AssertUtil.assertFiles(ResourceUtil.readResourceFile("clean_after_expected.dmm"), result);

        result.delete();
    }
}