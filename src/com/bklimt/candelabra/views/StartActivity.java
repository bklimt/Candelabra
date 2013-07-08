package com.bklimt.candelabra.views;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.parse.ParseAnalytics;

import com.bklimt.candelabra.R;
import com.bklimt.candelabra.models.RootViewModel;
import com.bklimt.candelabra.util.Callback;

public class StartActivity extends Activity {
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.start);

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

    root.fetchCurrentLights(new Callback<Boolean>() {
      public void callback(Boolean result, Exception error) {
        if (error != null) {
          log.log(Level.SEVERE, "Unable to fetch current lights.", error);
          Toast toast = Toast.makeText(StartActivity.this, "Unable to fetch current lights.",
              Toast.LENGTH_LONG);
          toast.show();
        }
        startNextActivity(error == null);
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
