package com.bklimt.candelabra.views;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.parse.ParseAnalytics;

import com.bklimt.candelabra.CandelabraApplication;
import com.bklimt.candelabra.R;
import com.bklimt.candelabra.models.CandelabraUser;

public class StartActivity extends Activity {
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.start);

    ParseAnalytics.trackAppOpened(getIntent());

    new AsyncTask<Void, Void, Boolean>() {
      @Override
      protected Boolean doInBackground(Void... params) {
        CandelabraUser user = CandelabraUser.getCurrentUser();
        user.fetch(StartActivity.this);

        if (user.getIpAddress() == null) {
          return false;
        }
        
        Logger log = Logger.getLogger(getClass().getName());
        try {
          ((CandelabraApplication) StartActivity.this.getApplication()).updateLights();
        } catch (Exception e) {
          log.log(Level.SEVERE, "Unable to update lights.", e);
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
