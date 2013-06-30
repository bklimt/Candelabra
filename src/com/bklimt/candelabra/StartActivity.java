package com.bklimt.candelabra;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.parse.ParseAnalytics;

import com.bklimt.candelabra.R;

public class StartActivity extends Activity {
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.start);

    ParseAnalytics.trackAppOpened(getIntent());

    new AsyncTask<Void, Void, CandelabraUser>() {
      @Override
      protected CandelabraUser doInBackground(Void... params) {
        CandelabraUser.getCurrentUser().fetch(StartActivity.this);
        return CandelabraUser.getCurrentUser();
      }

      @Override
      protected void onPostExecute(CandelabraUser user) {
        if (user.getIpAddress() == null || user.getIpAddress().equals("")) {
          startActivity(new Intent("candelabra.intent.action.SETUP"));
        } else {
          startActivity(new Intent("candelabra.intent.action.LIGHT"));
        }
      }
    }.execute();
  }
}
