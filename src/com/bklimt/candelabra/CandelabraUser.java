package com.bklimt.candelabra;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.bindroid.trackable.TrackableField;

public class CandelabraUser {
  public static CandelabraUser getCurrentUser() {
    return currentUser;
  }
  
  public CandelabraUser() {
  }
  
  private TrackableField<String> ipAddress = new TrackableField<String>("");
  public String getIpAddress() {
    return ipAddress.get();
  }
  public void setIpAddress(String newAddress) {
    ipAddress.set(newAddress);
  }

  private TrackableField<String> apiKey = new TrackableField<String>();
  public String getApiKey() {
    return apiKey.get();
  }  
  public void setApiKey(String newKey) {
    apiKey.set(newKey);
  }
  
  public void fetch(Activity activity) {
    SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
    setIpAddress(preferences.getString("ipAddress", ""));
    setApiKey(preferences.getString("apiKey", ""));
  }

  public void save(Activity activity) {
    SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
    Editor editor = preferences.edit();
    editor.putString("ipAddress", getIpAddress());
    editor.putString("apiKey", getApiKey());
    editor.commit();
  }
  
  private static CandelabraUser currentUser = new CandelabraUser();
}
