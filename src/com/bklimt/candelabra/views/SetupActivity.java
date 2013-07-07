package com.bklimt.candelabra.views;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import com.bklimt.candelabra.CandelabraApplication;
import com.bklimt.candelabra.R;
import com.bklimt.candelabra.models.RootViewModel;

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

    RootViewModel.get().bindToEditText(this, R.id.ip_address_edit, "ipAddress");
    RootViewModel.get().bindToEditText(this, R.id.user_name_edit, "userName");
    RootViewModel.get().bindToEditText(this, R.id.device_type_edit, "deviceType");

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
          protected Exception doInBackground(Void... params) {
            RootViewModel root = RootViewModel.get();
            root.saveDeviceSettings(SetupActivity.this);

            JSONObject command = new JSONObject();
            try {
              command.put("username", root.getUserName());
              command.put("devicetype", root.getDeviceType());
            } catch (JSONException je) {
              return new RuntimeException(je);
            }

            URL url;
            try {
              url = new URL("http", root.getIpAddress(), 80, "/api");
            } catch (MalformedURLException mue) {
              return new RuntimeException(mue);
            }

            try {
              CandelabraApplication.post(url, command);
            } catch (Exception e) {
              return e;
            }
            
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
              Toast toast = Toast.makeText(SetupActivity.this,
                  "Unable to save settings. " + error.getMessage(), Toast.LENGTH_LONG);
              toast.show();
            }
          }
        }.execute();
      }
    });
  }
}
