package com.bklimt.candelabra.models;

import org.json.JSONObject;

import com.bklimt.candelabra.backbone.Model;
import com.bklimt.candelabra.util.Callback;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class RootViewModel extends Model {
  private static RootViewModel model = new RootViewModel();

  public static RootViewModel get() {
    return model;
  }

  public void setDefaults() {
    set("enabled", true);
    set("ipAddress", "192.168.1.3");
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
  
  public void createDefaultPresets() {
    getPresets().clear();

    Preset allWhite = new Preset();
    allWhite.setName("All White");

    Light light1 = new Light();
    light1.setId("1");
    light1.setName("Light 0");
    light1.setOn(true);
    light1.getColor().setHue(0);
    light1.getColor().setBri(255);
    light1.getColor().setSat(0);
    allWhite.getLights().add(light1);

    Light light2 = new Light();
    light2.setId("2");
    light2.setName("Light 1");
    light2.setOn(true);
    light2.getColor().setHue(0);
    light2.getColor().setBri(255);
    light2.getColor().setSat(0);
    allWhite.getLights().add(light2);

    Light light3 = new Light();
    light3.setId("3");
    light3.setName("Light 2");
    light3.setOn(true);
    light3.getColor().setHue(0);
    light3.getColor().setBri(255);
    light3.getColor().setSat(0);
    allWhite.getLights().add(light3);

    getPresets().add(allWhite);
  }

  public void fetchDeviceSettings(Activity activity) throws Exception {
    SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
    setJSON(new JSONObject(preferences.getString("json", "{}")));
    if (getPresets().size() == 0) {
      createDefaultPresets();
    }
  }

  public void createMockLights() {
    getLights().createMockLights();
  }
  
  public void fetchCurrentLights(Callback<Boolean> callback) {
    getLights().fetchCurrentLights(callback);
  }

  public void saveDeviceSettings(Activity activity) {
    SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
    Editor editor = preferences.edit();
    editor.putString("json", toJSON().toString());
    editor.commit();
  }
  
  public void applyPreset(Preset preset) {
    getLights().applyPreset(preset.getLights());
  }
  
  public void savePreset(Activity activity, String name) {
    Preset preset = getPresets().findById(name);
    if (preset == null) {
      preset = new Preset();
      preset.setName(name);
      preset.getLights().setJSON(getLights().toJSON());
    }
    getPresets().add(preset);
    saveDeviceSettings(activity);
  }
}
