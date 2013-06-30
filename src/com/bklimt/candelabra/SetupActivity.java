package com.bklimt.candelabra;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.bindroid.BindingMode;
import com.bindroid.ui.UiBinder;
import com.bklimt.candelabra.R;
import com.parse.ParseException;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class SetupActivity extends Activity {
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.setup);

    UiBinder.bind(this, R.id.ip_address_edit, "Text", CandelabraUser.getCurrentUser(), "ipAddress",
        BindingMode.TWO_WAY);

    UiBinder.bind(this, R.id.api_key_edit, "Text", CandelabraUser.getCurrentUser(), "apiKey",
        BindingMode.TWO_WAY);

    final Button saveButton = (Button) findViewById(R.id.save_button);
    saveButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        new AsyncTask<Void, Void, Exception>() {
          @Override
          protected void onPreExecute() {
            saveButton.setEnabled(false);
          }

          @Override
          protected ParseException doInBackground(Void... params) {
            CandelabraUser.getCurrentUser().save(SetupActivity.this);
            return null;
          }

          @Override
          protected void onPostExecute(Exception error) {
            saveButton.setEnabled(true);
            if (error == null) {
              startActivity(new Intent("candelabra.intent.action.LIGHT"));
            } else {
              Logger.getLogger("com.bklimt.candelabra.SetupActivity").log(Level.SEVERE,
                  error.getMessage());
              Toast toast = Toast.makeText(SetupActivity.this, "Unable to save settings.",
                  Toast.LENGTH_SHORT);
              toast.show();
            }
          }
        }.execute();
      }
    });
  }
}
