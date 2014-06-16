package com.bklimt.candelabra.models;

import java.io.InputStream;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;

import com.bklimt.candelabra.R;
import com.bklimt.candelabra.backbone.Model;
import com.bklimt.candelabra.networking.Http;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class RootViewModel extends Model {
  private static RootViewModel model = new RootViewModel();

  public static RootViewModel get() {
    return model;
  }

  private Logger log = Logger.getLogger(getClass().getName());

  public void setDefaults() {
    set("enabled", true);
    set("ipAddress", "192.168.1.1");
    set("userName", "CandelabraUserName");
    set("deviceType", "CandelabraDeviceType");
    set("presets", new PresetSet());
    set("lights", new LightSet());
  }

  public boolean isEnabled() {
    Boolean enabled = (Boolean) get("enabled");
    return enabled != null && enabled.booleanValue();
  }

  public void setEnabled(boolean newEnabled) {
    set("enabled", newEnabled);
  }

  public String getIpAddress() {
    return (String) get("ipAddress");
  }

  public void setIpAddress(String newIpAddress) {
    set("ipAddress", newIpAddress);
  }

  public String getUserName() {
    return (String) get("userName");
  }

  public void setUserName(String newUserName) {
    set("userName", newUserName);
  }

  public String getDeviceType() {
    return (String) get("deviceType");
  }

  public void setDeviceType(String newDeviceType) {
    set("deviceType", newDeviceType);
  }

  public LightSet getLights() {
    return (LightSet) get("lights");
  }

  public PresetSet getPresets() {
    return (PresetSet) get("presets");
  }

  public void createDefaultPresets(Context context) {
    getPresets().clear();

    try {
      InputStream jsonStream = context.getResources().openRawResource(R.raw.default_presets);
      String json = Http.readFully(jsonStream);
      JSONArray presetJSON = new JSONArray(json);
      getPresets().setJSON(presetJSON);
    } catch (Exception e) {
      log.log(Level.SEVERE, "Unable to read default presets.", e);
    }
  }

  public void fetchDeviceSettings(Context context) throws Exception {
    SharedPreferences preferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
    String preferencesJSON = preferences.getString("json", "{}");
    log.info("Loading preferences: " + preferencesJSON);
    JSONObject preferencesObject = new JSONObject(preferencesJSON);
    preferencesObject.remove("lights");
    setJSON(preferencesObject);
    if (getPresets().size() == 0) {
      createDefaultPresets(context);
    }
  }

  public void createMockLights() {
    getLights().createMockLights();
  }

  public Task<Void> fetchCurrentLights() {
    return getLights().fetchCurrentLights();
  }
  
  public Task<Void> registerUsername() {
    return Task.<Void> forResult(null).continueWithTask(new Continuation<Void, Task<JSONObject>>() {
      @Override
      public Task<JSONObject> then(Task<Void> task) throws Exception {
        // Try to GET the data about the user from the device.
        URL url = new URL("http", getIpAddress(), 80, "/api/" + getUserName());
        return Http.getInstance().get(null, url);
      }
    }, Task.UI_THREAD_EXECUTOR).continueWithTask(new Continuation<JSONObject, Task<Void>>() {
      @Override
      public Task<Void> then(Task<JSONObject> task) throws Exception {
        if (task.isFaulted()) {
          // Maybe the user isn't registered. Do a POST to set up the new user.
          JSONObject command = new JSONObject();
          command.put("username", getUserName());
          command.put("devicetype", getDeviceType());
          URL url = new URL("http", getIpAddress(), 80, "/api");
          return Http.getInstance().post(null, url, command).makeVoid();
        } else {
          Logger logger = Logger.getLogger(getClass().getName());
          logger.info("Initial information: " + task.getResult());
        }
        return Task.forResult(null);
      }
    }, Task.UI_THREAD_EXECUTOR).onSuccessTask(new Continuation<Void, Task<Void>>() {
      @Override
      public Task<Void> then(Task<Void> task) throws Exception {
        return fetchCurrentLights();
      }
    }, Task.UI_THREAD_EXECUTOR);
  }

  public void saveDeviceSettings(Context context) {
    SharedPreferences preferences = context.getSharedPreferences("prefs", Context.MODE_PRIVATE);
    JSONObject preferencesObject = toJSON();
    preferencesObject.remove("lights");
    String preferencesJSON = preferencesObject.toString();
    log.info("Saving preferences: " + preferencesJSON);
    Editor editor = preferences.edit();
    editor.clear();
    editor.putString("json", preferencesJSON);
    editor.commit();
  }

  public void applyPreset(Preset preset) {
    getLights().applyPreset(preset.getLights());
  }

  public void savePreset(Context context, String name) {
    Preset preset = getPresets().findById(name);
    if (preset == null) {
      preset = new Preset();
      preset.setName(name);
      getPresets().add(preset);
    }
    preset.getLights().setJSON(getLights().toJSON());
    saveDeviceSettings(context);
  }

  public void deletePreset(Context context, Preset preset) {
    getPresets().remove(preset);
    saveDeviceSettings(context);
  }
}
