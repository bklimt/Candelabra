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
    RootViewModel root = RootViewModel.get();
    final Logger log = Logger.getLogger(getClass().getName());
    try {
      root.fetchDeviceSettings(StartActivity.this);
    } catch (Exception e) {
      log.log(Level.SEVERE, "Unable to fetch device settings.", e);
      Toast toast = Toast.makeText(StartActivity.this, "Unable to fetch device settings.",
          Toast.LENGTH_LONG);
      toast.show();
      startNextActivity(false);
      return;
    }

    if (root.getIpAddress() == null) {
      startNextActivity(false);
      return;
    }

    root.setEnabled(true);
    root.fetchCurrentLights().continueWith(new Continuation<Boolean, Void>() {
      @Override
      public Void then(Task<Boolean> task) throws Exception {
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
        startNextActivity(error == null);
        return null;
      }
    });    
  }

  private void startNextActivity(boolean setup) {
    Intent intent = null;
    if (!setup) {
      intent = new Intent(this, SetupActivity.class);
    } else {
      intent = new Intent(this, LightsActivity.class);
    }
    startActivity(intent);
  }
}
