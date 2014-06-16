package com.bklimt.candelabra.views;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import bolts.Continuation;
import bolts.Task;

import com.parse.ParseAnalytics;

import com.bklimt.candelabra.R;
import com.bklimt.candelabra.models.RootViewModel;

public class StartActivity extends Activity {
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_start);

    ParseAnalytics.trackAppOpened(getIntent());
    
    Task.<Void> forResult(null).continueWith(new Continuation<Void, Void>() {
      @Override
      public Void then(Task<Void> isAlreadySetup) throws Exception {
        // Thread.sleep(5000);
        return null;
      }
    }, Task.BACKGROUND_EXECUTOR).continueWithTask(new Continuation<Void, Task<Boolean>>() {
      @Override
      public Task<Boolean> then(Task<Void> isAlreadySetup) throws Exception {
        return isAlreadySetup();
      }
    }, Task.UI_THREAD_EXECUTOR).continueWith(new Continuation<Boolean, Void>() {
      @Override
      public Void then(Task<Boolean> isAlreadySetup) throws Exception {
        Intent intent = null;
        if (!isAlreadySetup.getResult()) {
          intent = new Intent(StartActivity.this, UpnpActivity.class);
        } else {
          intent = new Intent(StartActivity.this, LightsActivity.class);
        }
        startActivity(intent);
        return null;
      }
    }, Task.UI_THREAD_EXECUTOR);
  }
  
  private Task<Boolean> isAlreadySetup() {
    RootViewModel root = RootViewModel.get();
    final Logger log = Logger.getLogger(getClass().getName());
    
    try {
      root.fetchDeviceSettings(StartActivity.this);
    } catch (Exception e) {
      log.log(Level.SEVERE, "Unable to fetch device settings.", e);
      Toast toast = Toast.makeText(StartActivity.this, "Unable to fetch device settings.",
          Toast.LENGTH_LONG);
      toast.show();
      return Task.forResult(false);
    }

    if (root.getIpAddress() == null) {
      return Task.forResult(false);
    }

    root.setEnabled(true);
    return root.fetchCurrentLights().continueWith(new Continuation<Void, Boolean>() {
      @Override
      public Boolean then(Task<Void> task) throws Exception {
        Exception error = task.getError();
        if (error != null) {
          // Disable the lights by default if we couldn't connect.
          RootViewModel root = RootViewModel.get();
          root.setEnabled(false);
          root.createMockLights();

          log.log(Level.SEVERE, "Unable to fetch current lights.", error);
          StartActivity.this.runOnUiThread(new Runnable() {
            public void run() {
              Toast toast = Toast.makeText(StartActivity.this, "Unable to fetch current lights.",
                  Toast.LENGTH_LONG);
              toast.show();
            }
          });
        }
        return (error == null);
      }
    }, Task.UI_THREAD_EXECUTOR);
  }
}
