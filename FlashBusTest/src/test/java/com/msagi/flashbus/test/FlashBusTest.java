package com.msagi.flashbus.test;

import com.msagi.flashbus.test.events.TestEvent;
import com.msagi.flashbus.test.events.TestEventWithData;
import com.msagi.flashbus.test.events.TestRuntimeExceptionEvent;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * JUnit test for FlashBus.
 *
 * Note: If you are on Linux or on a Mac, you will probably need to configure the default JUnit test runner configuration
 * in order to work around a bug where Android Studio does not set the working directory to the module being tested. This
 * can be accomplished by editing the run configurations, Defaults -> JUnit and changing the working directory value to
 * $MODULE_DIR$.
 *
 * @author msagi (miklos.sagi@gmail.com)
 * @author yanislav.mihaylov (jany81@gmail.com)
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class FlashBusTest {

    private final UnitTestBridge mUnitTestBridge = new UnitTestBridge();

    private final FlashBus mEventBus = mUnitTestBridge.getFlashBus();

    @Test
    public void getDefaultBus() {
        final FlashBus bus = FlashBus.getDefault();
        Assert.assertNotNull(bus);
    }

    @Test
    public void registrationWithNull() {
        mEventBus.register((Object)null);
    }

    @Test
    public void getStickyWithNullValue() {
        assertNull(mEventBus.getStickyEvent(null));
    }

    @Test
    public void unregisterWithNullEventHandler() {
        mEventBus.unregister((Object)null);
    }

    @Test
    public void postTestEvent() {
        mUnitTestBridge.register();

        mEventBus.post(new TestEvent());

        runTasks();
        assertEquals(1, mUnitTestBridge.getReceivedTestEventsOnMainThreadListSize());
        assertEquals(0, mUnitTestBridge.getReceivedTestEventsWithDataOnMainThreadListSize());

        mUnitTestBridge.unregister();
    }

    @Test
    public void duplicateEventHandlerRegistration() {
        mUnitTestBridge.register();
        mUnitTestBridge.register();
        mEventBus.post(new TestEvent());

        runTasks();
        assertEquals(1, mUnitTestBridge.getReceivedTestEventsOnMainThreadListSize());
        assertEquals(0, mUnitTestBridge.getReceivedTestEventsWithDataOnMainThreadListSize());

        mUnitTestBridge.unregister();
    }

    @Test
    public void postTestEventWithData() {
        mUnitTestBridge.register();

        mEventBus.post(new TestEventWithData("test"));

        runTasks();
        assertEquals(0, mUnitTestBridge.getReceivedTestEventsOnMainThreadListSize());
        assertEquals(1, mUnitTestBridge.getReceivedTestEventsWithDataOnMainThreadListSize());

        mUnitTestBridge.unregister();
    }

    @Test
    public void multipleEventsDelivery() {
        mUnitTestBridge.register();

        mEventBus.post(new TestEvent());
        mEventBus.post(new TestEvent());
        mEventBus.post(new TestEventWithData("test"));
        mEventBus.post(new TestEvent());
        mEventBus.post(new TestEventWithData("test"));

        runTasks();
        assertEquals(3, mUnitTestBridge.getReceivedTestEventsOnMainThreadListSize());
        assertEquals(2, mUnitTestBridge.getReceivedTestEventsWithDataOnMainThreadListSize());

        mUnitTestBridge.unregister();
    }

    @Test
    public void missingStickyEvent() {
        final Object event = mEventBus.getStickyEvent(TestEvent.class);
        assertNull(event);
    }

    @Test
    public void postStickyEvent() {
        mEventBus.postSticky(new TestEvent());
        final Object event = mEventBus.getStickyEvent(TestEvent.class);
        assertNotNull(event);
        assertTrue(event instanceof TestEvent);
    }

    @Test
    public void getStickyEvent() {
        mUnitTestBridge.register();
        mEventBus.postSticky(new TestEvent());

        runTasks();
        assertEquals(1, mUnitTestBridge.getReceivedTestEventsOnMainThreadListSize());
        Assert.assertNotNull(mEventBus.getStickyEvent(TestEvent.class));

        mUnitTestBridge.unregister();
    }

    @Test
    public void getStickyEventOnRegistration() {
        mEventBus.postSticky(new TestEvent());
        mUnitTestBridge.register();

        runTasks();
        assertEquals(1, mUnitTestBridge.getReceivedTestEventsOnMainThreadListSize());
        Assert.assertNotNull(mEventBus.getStickyEvent(TestEvent.class));

        mUnitTestBridge.unregister();
    }

    @Test
    public void removeStickyEventWithNull() {
        mEventBus.removeStickyEvent(null);
    }

    @Test
    public void removeStickyEvent() {
        mEventBus.postSticky(new TestEvent());
        Assert.assertNotNull(mEventBus.getStickyEvent(TestEvent.class));
        mEventBus.removeStickyEvent(TestEvent.class);
        Assert.assertNull(mEventBus.getStickyEvent(TestEvent.class));
    }

    @Test
    public void unregisterEventHandler() {
        mUnitTestBridge.register();
        mUnitTestBridge.unregister();

        mEventBus.post(new TestEvent());

        runTasks();
        assertEquals(0, mUnitTestBridge.getReceivedTestEventsOnMainThreadListSize());
    }


    @Test
    public void unexpectedExceptionThrownFromEventHandler() {
        mUnitTestBridge.register();
        mEventBus.post(new TestRuntimeExceptionEvent());

        runTasks();
        assertEquals(0, mUnitTestBridge.getReceivedTestEventsOnMainThreadListSize());
        assertEquals(0, mUnitTestBridge.getReceivedTestEventsWithDataOnMainThreadListSize());

        mUnitTestBridge.unregister();
    }

    @Test
    public void unexpectedExceptionThrownFromEventHandlerDuringRegistration() {
        mEventBus.postSticky(new TestRuntimeExceptionEvent());
        mUnitTestBridge.register();

        runTasks();
        assertEquals(0, mUnitTestBridge.getReceivedTestEventsOnMainThreadListSize());
        assertEquals(0, mUnitTestBridge.getReceivedTestEventsWithDataOnMainThreadListSize());

        mUnitTestBridge.unregister();
    }

    /**
     * Forces Robolectric to force Handler to deliver events to the MAIN thread
     */
    private void runTasks() {
        ShadowLooper.runUiThreadTasks();
    }
}
