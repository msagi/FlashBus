package com.msagi.flashbus.test;

import android.app.Application;
import android.content.Context;

/**
 * Created by msagi on 18/09/15.
 */
public class FlashBusTestApplication extends Application {

    @Override
    protected void attachBaseContext(final Context base) {
        super.attachBaseContext(base);
        FlashBus.setApplicationContext(base);
    }
}
