package com.bklimt.candelabra.views;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.bklimt.candelabra.CandelabraApplication;
import com.bklimt.candelabra.R;
import com.bklimt.candelabra.models.CandelabraUser;
import com.bklimt.candelabra.models.HSVColor;
import com.bklimt.candelabra.models.Light;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class LightFragment extends Fragment {

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.light, container);

    final ColorView colorView = (ColorView) view.findViewById(R.id.color);
    colorEdit = (EditColor) view.findViewById(R.id.color_edit);

    if (light != null) {
      colorEdit.setColor(light.getColor());
    }
    
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
        final String path = "/api/" + user.getUserName() + "/lights/" + light.getId() + "/state";
        final int b = (int) (value * 255);
        final int s = (int) (saturation * 255);
        final int h = (int) ((hue / 360.0f) * 65535);

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
              Toast toast = Toast.makeText(LightFragment.this.getActivity(),
                  "Unable to change light color. " + error, Toast.LENGTH_LONG);
              toast.show();
            }
          }
        }.execute();
      }
    });

    return view;
  }
  
  public void setLight(Light newLight) {
    light = newLight;
    if (colorEdit != null) {
      colorEdit.setColor(newLight.getColor());
    }
  }
  
  EditColor colorEdit = null;
  private Light light = null;
}
