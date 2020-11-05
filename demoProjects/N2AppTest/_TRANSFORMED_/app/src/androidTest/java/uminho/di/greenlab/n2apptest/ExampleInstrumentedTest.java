package uminho.di.greenlab.n2apptest;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;
import uminho.di.greenlab.trepnlibrary.TrepnLib;
import android.support.test.InstrumentationRegistry;
import org.junit.Before;
import org.junit.After;

@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Before
    public void before() {
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("pm grant " + "com.greenlab.trepnlib" + " android.permission.WRITE_EXTERNAL_STORAGE");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("pm grant " + "com.greenlab.trepnlib" + " android.permission.READ_EXTERNAL_STORAGE");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("pm grant " + InstrumentationRegistry.getTargetContext().getPackageName() + " android.permission.WRITE_EXTERNAL_STORAGE");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand("pm grant " + InstrumentationRegistry.getTargetContext().getPackageName() + " android.permission.READ_EXTERNAL_STORAGE");
        TrepnLib.startProfilingTest(InstrumentationRegistry.getTargetContext());
    }

    @Test
    public void useAppContext() throws Exception {
        TrepnLib.traceTest("uminho.di.greenlab.n2apptest.ExampleInstrumentedTest->useAppContext|");
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("uminho.di.greenlab.n2apptest", appContext.getPackageName());
    }

    @Test
    public void harderTest() throws Exception {
        TrepnLib.traceTest("uminho.di.greenlab.n2apptest.ExampleInstrumentedTest->harderTest|");
        Context appContext = InstrumentationRegistry.getTargetContext();
        for (int i = 0; i < 100000; i++) {
            assertEquals("uminho.di.greenlab.n2apptest", appContext.getPackageName());
        }
    }

    @After
    public void after() {
        TrepnLib.stopProfilingTest(InstrumentationRegistry.getTargetContext());
    }

    @Test
    public void failTest() throws Exception {
        TrepnLib.traceTest("uminho.di.greenlab.n2apptest.ExampleInstrumentedTest->failTest|");
        Context appContext = InstrumentationRegistry.getTargetContext();
        for (int i = 0; i < 100000; i++) {
            assertEquals("uminho.di.greenlab.n2aptest", appContext.getPackageName());
        }
    }
}
