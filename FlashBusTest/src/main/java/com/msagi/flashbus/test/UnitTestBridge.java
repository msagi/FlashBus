package com.msagi.flashbus.test;

import com.msagi.flashbus.annotation.FlashBusConfiguration;
import com.msagi.flashbus.annotation.Subscribe;
import com.msagi.flashbus.test.events.TestEvent;
import com.msagi.flashbus.test.events.TestEventWithData;
import com.msagi.flashbus.test.events.TestRuntimeExceptionEvent;
import com.msagi.flashbus.FlashBus;

import java.util.ArrayList;
import java.util.List;

/**
 * JUnit test for {@link com.msagi.flashbus.FlashBus}
 *
 * @author msagi (miklos.sagi@gmail.com)
 * @author yanislav.mihaylov (jany81@gmail.com)
 */
@FlashBusConfiguration(packageName = "com.msagi.flashbus.test")
public class UnitTestBridge {

    private final FlashBus mEventBus = new FlashBus();

    private final List<TestEvent> mReceivedTestEventsOnMainThread = new ArrayList<>();

    private final List<TestEventWithData> mReceivedTestEventsWithDataOnMainThread = new ArrayList<>();

    @Subscribe
    public void handleTestEventOnMainThread(final TestEvent event) {
        System.out.println("UnitTestBridge: Event received on handleTestEventOnMainThread: " + event);
        mReceivedTestEventsOnMainThread.add(event);
    }

    @Subscribe
    public void handleTestEventWithDataOnMainThread(final TestEventWithData event) {
        System.out.println("UnitTestBridge: Event received on handleTestEventWithDataOnMainThread: " + event);
        mReceivedTestEventsWithDataOnMainThread.add(event);
    }

    @Subscribe
    public void throwRuntimeExceptionOnMainThread(final TestRuntimeExceptionEvent event) {
        System.out.println("UnitTestBridge: Event received on throwRuntimeExceptionOnMainThread: " + event);
        throw new RuntimeException("This is a test runtime exception when handling event " + event);
    }

    public FlashBus getFlashBus() {
        return mEventBus;
    }

    public void register() {
        mEventBus.register(this);
        System.out.println("UnitTestBridge: registered on FlashBus: " + mEventBus);
    }

    public void unregister() {
        mEventBus.unregister(this);
        System.out.println("UnitTestBridge: unregistered from FlashBus: " + mEventBus);
    }

    public int getReceivedTestEventsOnMainThreadListSize() {
        return mReceivedTestEventsOnMainThread.size();
    }

    public int getReceivedTestEventsWithDataOnMainThreadListSize() {
        return mReceivedTestEventsWithDataOnMainThread.size();
    }
}
