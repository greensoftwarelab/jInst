package demo_kotlin;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class Example2 {

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("uminho.di.greenlab.n2apptest", appContext.getPackageName());
    }

    @Test
    public void harderTest() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        for (int i = 0; i < 100000 ; i++) {
            assertEquals("uminho.di.greenlab.n2apptest", appContext.getPackageName());
        }
    }

    @Test
    public void failTest() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        for (int i = 0; i < 100000 ; i++) {
            assertEquals("uminho.di.greenlab.n2aptest", appContext.getPackageName());
        }
    }
}
