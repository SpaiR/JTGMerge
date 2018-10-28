package io.github.spair.jtgmerge.command;

import io.github.spair.jtgmerge.AssertUtil;
import io.github.spair.jtgmerge.FieldUtil;
import io.github.spair.jtgmerge.ResourceUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class ConvertTest {

    private File tgmMap = ResourceUtil.readResourceFile("convert_tgm.dmm");
    private File byondMap = ResourceUtil.readResourceFile("convert_byond.dmm");
    private File result = new File("convert.dmm.result");

    private Convert convert;

    @Before
    public void setUp() {
        result.deleteOnExit();
        convert = new Convert();
        FieldUtil.setField(convert, "mapFile", result);
    }

    @After
    public void tearDown() {
        result.delete();
    }

    @Test
    public void testRunToTgm() {
        ResourceUtil.copy(tgmMap, result);
        convert.run();
        AssertUtil.assertFiles(byondMap, result);
    }

    @Test
    public void testRunToByond() {
        ResourceUtil.copy(byondMap, result);
        convert.run();
        AssertUtil.assertFiles(tgmMap, result);
    }
}