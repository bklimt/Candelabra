package com.bklimt.candelabra;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.bklimt.candelabra.R;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class LightActivity extends Activity {
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.light);

    final ColorView colorView = (ColorView) findViewById(R.id.color);

    EditColor colorEdit = (EditColor) findViewById(R.id.color_edit);
    colorEdit.addListener(new ColorListener() {
      @Override
      public void onColorChanged(float hue, float saturation, float value, boolean finished) {
        float[] hsv = { hue, saturation, value };
        colorView.setColor(HSVColor.getColor(hsv));

        if (!finished) {
          return;
        }
        
        CandelabraUser user = CandelabraUser.getCurrentUser();
        final String ipAddress = user.getIpAddress();
        final String path = "/api/" + user.getUserName() + "/lights/3/state";
        final int b = (int)(value * 255);
        final int s = (int)(saturation * 255);
        final int h = (int)((hue / 360.0f) * 65535);

        new AsyncTask<Void, Void, Exception>() {
          @Override
          protected Exception doInBackground(Void... params) {
            try {
              URL url = new URL("http", ipAddress, 80, path);
              JSONObject command = new JSONObject();
              command.put("bri", b);
              command.put("sat", s);
              command.put("hue", h);
              CandelabraApplication.put(url, command);

            } catch (Exception e) {
              return e;
            }
            return null;
          }
          
          @Override
          protected void onPostExecute(Exception error) {
            if (error != null) {
              Logger.getLogger(getClass().getName()).log(Level.SEVERE, error.getMessage());
              Toast toast = Toast.makeText(LightActivity.this,
                  "Unable to change light color. " + error, Toast.LENGTH_LONG);
              toast.show();
            }
          }
        }.execute();
      }
    });

    Button setupButton = (Button) findViewById(R.id.setup_button);
    setupButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        startActivity(new Intent("candelabra.intent.action.SETUP"));
      }
    });
  }
}
