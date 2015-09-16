package com.msagi.flashbus.benchmark;

import com.msagi.flashbus.FlashBus;
import com.msagi.flashbus.annotation.Subscribe;
import com.msagi.flashbus.test.events.TestEvent;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    FlashBus mFlashBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.msagi.flashbus.test.R.layout.activity_main);
        mFlashBus = FlashBus.getDefault();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(com.msagi.flashbus.test.R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mFlashBus.register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mFlashBus.unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFlashBus = null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == com.msagi.flashbus.test.R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Subscribe
    public void onBenchmarkStarted(final TestEvent event) {
        Log.e(TAG, "onBenchmarkStarted: event: " + event);
    }
}
