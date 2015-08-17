package com.msagi.flashbus.benchmark;

import com.msagi.flashbus.EventBus;
import com.msagi.flashbus.annotation.Subscribe;

import android.os.Bundle;

/**
 * Created by msagi on 19/08/15.
 */
public class Experiment {

    @Subscribe
    public void onEvent(final Bundle event) {

    }

    public void doSomething() {
        EventBus.getDefault().register(this);

    }
}
