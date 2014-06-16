package com.bklimt.candelabra.views;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import bolts.Continuation;
import bolts.Task;

import com.bklimt.candelabra.R;
import com.bklimt.candelabra.models.RootViewModel;
import com.bklimt.candelabra.networking.Upnp;

public class UpnpActivity extends Activity {
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_upnp);

    final RootViewModel root = RootViewModel.get();
    root.setEnabled(true);

    Task.<Void> forResult(null).continueWith(new Continuation<Void, Void>() {
      @Override
      public Void then(Task<Void> isAlreadySetup) throws Exception {
        // Thread.sleep(5000);
        return null;
      }
    }, Task.BACKGROUND_EXECUTOR).continueWithTask(new Continuation<Void, Task<String>>() {
      @Override
      public Task<String> then(Task<Void> task) throws Exception {
        return Upnp.findIpAddress(getApplicationContext());
      }
    }, Task.UI_THREAD_EXECUTOR).onSuccessTask(new Continuation<String, Task<Void>>() {
      @Override
      public Task<Void> then(Task<String> task) throws Exception {
        root.setIpAddress(task.getResult());
        return root.registerUsername();
      }
    }, Task.UI_THREAD_EXECUTOR).continueWith(new Continuation<Void, Void>() {
      @Override
      public Void then(Task<Void> task) throws Exception {
        if (task.isFaulted()) {
          // The auto-setup didn't work, so go to manual setup.
          Intent intent = new Intent(UpnpActivity.this, SetupActivity.class);
          startActivity(intent);
        } else {
          // Everything is already set up.
          root.saveDeviceSettings(UpnpActivity.this);
          Intent intent = new Intent(UpnpActivity.this, LightsActivity.class);
          startActivity(intent);
        }
        return null;
      }
    }, Task.UI_THREAD_EXECUTOR);
  }
}
