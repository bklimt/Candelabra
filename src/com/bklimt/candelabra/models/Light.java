package com.bklimt.candelabra.models;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bklimt.candelabra.backbone.Model;
import com.bklimt.candelabra.backbone.ModelListener;
import com.bklimt.candelabra.networking.Http;
import com.bklimt.candelabra.util.Callback;

public class Light extends Model {
  private class Updater implements ModelListener {
    public void onChanged(String key, Object value) {
      RootViewModel root = RootViewModel.get();
      final String ipAddress = root.getIpAddress();
      final String path = "/api/" + root.getUserName() + "/lights/" + getId() + "/state";

      URL url;
      try {
        url = new URL("http", ipAddress, 80, path);
      } catch (MalformedURLException e) {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Unable to set light color.", e);
        return;
      }

      JSONObject command = new JSONObject();
      try {
        command.put("on", getOn());
        command.put("hue", getColor().getHue());
        command.put("sat", getColor().getSat());
        command.put("bri", getColor().getBri());
      } catch (JSONException e) {
        Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Unable to set light color.", e);
        return;
      }

      Http.getInstance().put("SET_LIGHT_" + getId(), url, command, new Callback<JSONArray>() {
        @Override
        public void callback(JSONArray result, final Exception e) {
          if (e != null) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Unable to set light color.",
                e);
          }
        }
      });
    }
  }

  private Updater updater = new Updater();

  public void setDefaults() {
    set("color", new HSVColor());
  }

  public HSVColor getColor() {
    return (HSVColor) get("color");
  }

  public String getId() {
    return (String) get("id");
  }

  public void setId(String newId) {
    set("id", newId);
  }

  public String getName() {
    return (String) get("name");
  }

  public void setName(String newName) {
    set("name", newName);
  }
  
  public boolean getOn() {
    Boolean on = (Boolean) get("on");
    return on != null && on.booleanValue();
  }
  
  public void setOn(boolean newValue) {
    set("on", newValue);
  }

  public void applyPreset(Light preset) {
    setOn(preset.getOn());
    getColor().setJSON(preset.getColor().toJSON());
  }

  public void connect() {
    addListener(updater);
    getColor().addListener(updater);
  }

  public void disconnect() {
    removeListener(updater);
    getColor().removeListener(updater);
  }
}
