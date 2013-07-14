package com.bklimt.candelabra.views;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bklimt.candelabra.R;
import com.bklimt.candelabra.models.RootViewModel;
import com.bklimt.candelabra.networking.Http;
import com.bklimt.candelabra.util.Callback;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class SetupActivity extends Activity {
  private Button saveButton;
  private Button skipButton;
  
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_setup);

    RootViewModel.get().bindToEditText(this, R.id.ip_address_edit, "ipAddress");
    RootViewModel.get().bindToEditText(this, R.id.user_name_edit, "userName");
    RootViewModel.get().bindToEditText(this, R.id.device_type_edit, "deviceType");

    saveButton = (Button) findViewById(R.id.save_button);
    skipButton = (Button) findViewById(R.id.skip_button);
    
    saveButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        onSaveClicked();
      }
    });
    
    skipButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        onSkipClicked();
      }
    });
  }

  private void onSaveClicked() {
    saveButton.setEnabled(false);
    skipButton.setEnabled(false);

    RootViewModel root = RootViewModel.get();
    root.setEnabled(true);
    root.saveDeviceSettings(SetupActivity.this);

    JSONObject command = new JSONObject();
    try {
      command.put("username", root.getUserName());
      command.put("devicetype", root.getDeviceType());
    } catch (JSONException je) {
      saveFinished(saveButton, je);
      return;
    }

    URL url;
    try {
      url = new URL("http", root.getIpAddress(), 80, "/api");
    } catch (MalformedURLException mue) {
      saveFinished(saveButton, mue);
      return;
    }

    Http.getInstance().post(null, url, command, new Callback<JSONArray>() {
      @Override
      public void callback(JSONArray result, Exception error) {
        saveFinished(saveButton, error);
      }
    });
  }
  
  private void onSkipClicked() {
    startActivity(new Intent(SetupActivity.this, LightsActivity.class));
  }

  private void saveFinished(final Button saveButton, final Exception error) {
    runOnUiThread(new Runnable() {
      public void run() {
        saveButton.setEnabled(true);
        skipButton.setEnabled(true);
        if (error == null) {
          startActivity(new Intent(SetupActivity.this, LightsActivity.class));
        } else {
          Logger.getLogger("com.bklimt.candelabra.SetupActivity").log(Level.SEVERE,
              error.getMessage());
          Toast toast = Toast.makeText(SetupActivity.this,
              "Unable to save settings. " + error.getMessage(), Toast.LENGTH_LONG);
          toast.show();
        }
      }
    });
  }
}
