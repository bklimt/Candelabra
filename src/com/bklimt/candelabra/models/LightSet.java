package com.bklimt.candelabra.models;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONObject;

import com.bklimt.candelabra.CandelabraApplication;
import com.bklimt.candelabra.backbone.Collection;
import com.bklimt.candelabra.backbone.Visitor;

public class LightSet extends Collection<Light> {
  public LightSet() {
  }

  public LightSet(JSONArray json) {
    setJSON(json);
  }
  
  public void copy(LightSet other) {
    setJSON(other.toJSON());
  }

  public void fetchCurrentLights() throws Exception {
    synchronized (lock) {
      RootViewModel root = RootViewModel.get();
      String ipAddress = root.getIpAddress();
      URL url = new URL("http", ipAddress, 80, "/api/" + root.getUserName() + "/lights");
      JSONObject object = CandelabraApplication.get(url);
      @SuppressWarnings("unchecked")
      Iterator<String> keys = (Iterator<String>) object.keys();
  
      // Record all the original lights.
      final ArrayList<Light> toRemove = new ArrayList<Light>();
      each(new Visitor<Light>() {
        @Override
        public void visit(Light light) {
          toRemove.add(light);
        }
      });
      
      final ArrayList<Light> toAdd = new ArrayList<Light>();
      while (keys.hasNext()) {
        String key = keys.next();
        Light light = findById(key);
        if (light == null) {
          light = new Light();
          toAdd.add(light);
        } else {
          toRemove.remove(light);
        }
  
        JSONObject value = object.getJSONObject(key);
        String name = value.getString("name");
        light.setId(key);
        light.setName(name);
  
        URL lightURL = new URL("http", ipAddress, 80, "/api/" + root.getUserName() + "/lights/"
            + light.getId());
        JSONObject lightObject = CandelabraApplication.get(lightURL);
        JSONObject state = lightObject.getJSONObject("state");
        light.getColor().setHue(state.getInt("hue"));
        light.getColor().setSat(state.getInt("sat"));
        light.getColor().setBri(state.getInt("bri"));
      }
  
      Collections.sort(toAdd, new Comparator<Light>() {
        @Override
        public int compare(Light lhs, Light rhs) {
          return lhs.getId().compareTo(rhs.getId());
        }
      });
      for (Light light : toAdd) {
        add(light);
      }
      for (Light light : toRemove) {
        remove(light);
      }
    }
  }
  
  public void applyPreset(LightSet preset) {
    preset.each(new Visitor<Light>() {
      @Override
      public void visit(Light preset) {
        Light light = findById(preset.getId());
        if (light != null) {
          light.applyPreset(preset);
        }
      }
    });
  }

  @Override
  protected Light create() {
    return new Light();
  }

  @Override
  protected String getId(Light item) {
    return item.getId();
  }
}
