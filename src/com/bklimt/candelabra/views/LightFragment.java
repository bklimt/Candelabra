package com.bklimt.candelabra.views;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONObject;

import com.bklimt.candelabra.CandelabraApplication;
import com.bklimt.candelabra.R;
import com.bklimt.candelabra.models.RootViewModel;
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

    colorView = (ColorView) view.findViewById(R.id.color);
    colorEdit = (EditColor) view.findViewById(R.id.color_edit);

    if (light != null) {
      colorView.setColor(light.getColor());
      colorEdit.setColor(light.getColor());
    }
    
    colorEdit.addListener(new ColorListener() {
      @Override
      public void onColorChanged(float hue, float saturation, float value, boolean finished) {
        if (!finished) {
          return;
        }

        RootViewModel root = RootViewModel.get();
        final String ipAddress = root.getIpAddress();
        final String path = "/api/" + root.getUserName() + "/lights/" + light.getId() + "/state";
        
        float hsv[] = { hue, saturation, value };
        light.getColor().setHSV(hsv);

        new AsyncTask<Void, Void, Exception>() {
          @Override
          protected Exception doInBackground(Void... params) {
            try {
              URL url = new URL("http", ipAddress, 80, path);
              JSONObject command = new JSONObject();
              command.put("hue", light.getColor().getHue());
              command.put("sat", light.getColor().getSat());
              command.put("bri", light.getColor().getBri());
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
    if (colorView != null) {
      colorView.setColor(newLight.getColor());
    }
  }
  
  ColorView colorView = null;
  EditColor colorEdit = null;
  private Light light = null;
}
