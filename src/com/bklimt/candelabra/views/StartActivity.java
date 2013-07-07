package com.bklimt.candelabra.views;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.parse.ParseAnalytics;

import com.bklimt.candelabra.R;
import com.bklimt.candelabra.models.RootViewModel;

public class StartActivity extends Activity {
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.start);

    ParseAnalytics.trackAppOpened(getIntent());

    new AsyncTask<Void, Void, Boolean>() {
      @Override
      protected Boolean doInBackground(Void... params) {
        RootViewModel root = RootViewModel.get();

        Logger log = Logger.getLogger(getClass().getName());
        try {
          root.fetchDeviceSettings(StartActivity.this);
        } catch (Exception e) {
          log.log(Level.SEVERE, "Unable to fetch device settings.", e);
          Toast toast = Toast.makeText(StartActivity.this, "Unable to fetch device settings.",
              Toast.LENGTH_LONG);
          toast.show();
          return false;
        }

        if (root.getIpAddress() == null) {
          return false;
        }

        try {
          root.fetchCurrentLights();
        } catch (Exception e) {
          log.log(Level.SEVERE, "Unable to fetch current lights.", e);
          Toast toast = Toast.makeText(StartActivity.this, "Unable to fetch current lights.",
              Toast.LENGTH_LONG);
          toast.show();
          return false;
        }

        return true;
      }

      @Override
      protected void onPostExecute(Boolean setup) {
        if (!setup) {
          startActivity(new Intent("candelabra.intent.action.SETUP"));
        } else {
          startActivity(new Intent("candelabra.intent.action.LIGHT"));
        }
      }
    }.execute();
  }
}
