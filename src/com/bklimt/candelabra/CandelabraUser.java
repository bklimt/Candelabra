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

  private TrackableField<String> userName = new TrackableField<String>();
  public String getUserName() {
    return userName.get();
  }  
  public void setUsername(String newUsername) {
    userName.set(newUsername);
  }
  
  private TrackableField<String> deviceType = new TrackableField<String>();
  public String getDeviceType() {
    return deviceType.get();
  }  
  public void setDeviceType(String newDeviceType) {
    deviceType.set(newDeviceType);
  }
  
  public void fetch(Activity activity) {
    SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
    setIpAddress(preferences.getString("ipAddress", "192.168.1.3"));
    setUsername(preferences.getString("userName", "CandelabraUserName"));
    setDeviceType(preferences.getString("deviceType", "CandelabraDeviceType"));
  }

  public void save(Activity activity) {
    SharedPreferences preferences = activity.getPreferences(Context.MODE_PRIVATE);
    Editor editor = preferences.edit();
    editor.putString("ipAddress", getIpAddress());
    editor.putString("userName", getUserName());
    editor.putString("deviceType", getDeviceType());
    editor.commit();
  }
  
  private static CandelabraUser currentUser = new CandelabraUser();
}
