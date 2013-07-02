package com.bklimt.candelabra.models;

import java.net.URL;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

import com.bindroid.trackable.TrackableField;
import com.bklimt.candelabra.CandelabraApplication;

public class Light {
  private HSVColor color = new HSVColor();
  public HSVColor getColor() {
    return color;
  }
  
  private TrackableField<String> id = new TrackableField<String>("");
  public String getId() {
    return id.get();
  }
  public void setId(String newId) {
    id.set(newId);
  }

  private TrackableField<String> name = new TrackableField<String>("");
  public String getName() {
    return name.get();
  }
  public void setName(String newName) {
    name.set(newName);
  }

  public static void getLights(Map<String, Light> lights) throws Exception {
    CandelabraUser user = CandelabraUser.getCurrentUser();
    String ipAddress = user.getIpAddress();
    URL url = new URL("http", ipAddress, 80, "/api/" + user.getUserName() + "/lights");
    JSONObject object = CandelabraApplication.get(url);
    @SuppressWarnings("unchecked")
    Iterator<String> keys = (Iterator<String>) object.keys();
    while (keys.hasNext()) {
      String key = keys.next();
      JSONObject value = object.getJSONObject(key);
      String name = value.getString("name");
      Light light = lights.containsKey(key) ? lights.get(key) : new Light();
      light.setId(key);
      light.setName(name);
      
      URL lightURL = new URL("http", ipAddress, 80, "/api/" + user.getUserName() + "/lights/" + light.id);
      JSONObject lightObject = CandelabraApplication.get(lightURL);
      JSONObject state = lightObject.getJSONObject("state");
      light.getColor().setHue(state.getInt("hue"));
      light.getColor().setSat(state.getInt("sat"));
      light.getColor().setBri(state.getInt("bri"));
      
      lights.put(key, light);
    }
  }
}
