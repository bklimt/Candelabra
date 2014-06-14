package com.bklimt.candelabra.models;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import bolts.Continuation;
import bolts.Task;

import com.bklimt.candelabra.backbone.Collection;
import com.bklimt.candelabra.backbone.Visitor;
import com.bklimt.candelabra.networking.Http;

public class LightSet extends Collection<Light> {
  public LightSet() {
  }

  public LightSet(JSONArray json) {
    setJSON(json);
  }

  public void copy(LightSet other) {
    setJSON(other.toJSON());
  }
  
  public void createMockLights() {
    this.clear();
    for (int i = 1; i <= 3; ++i) {
      Light light = new Light();
      light.setId("" + i);
      light.setName("Light " + i);
      light.setOn(true);
      add(light);
    }
  }
  
  private Task<Boolean> fetchLights(final Iterator<String> keys, final JSONObject object,
      final ArrayList<Light> toAdd, final ArrayList<Light> toRemove) {
    if (!keys.hasNext()) {
      return Task.forResult(true);
    }

    String key = keys.next();
    Light light = findById(key);
    if (light == null) {
      light = new Light();
      toAdd.add(light);
    } else {
      toRemove.remove(light);
    }

    JSONObject value;
    String name;
    try {
      value = object.getJSONObject(key);
      name = value.getString("name");
    } catch (JSONException e) {
      return Task.forResult(false);
    }
    light.setId(key);
    light.setName(name);

    final Light finalLight = light;

    RootViewModel root = RootViewModel.get();
    String userName = root.getUserName();
    String ipAddress = root.getIpAddress();
    URL lightURL;
    try {
      lightURL = new URL("http", ipAddress, 80, "/api/" + userName + "/lights/" + light.getId());
    } catch (MalformedURLException e) {
      return Task.forError(e);
    }
    Task<JSONObject> task = Http.getInstance().get(null, lightURL);
    return task.onSuccessTask(new Continuation<JSONObject, Task<Boolean>>() {
      @Override
      public Task<Boolean> then(Task<JSONObject> task) throws Exception {
        JSONObject lightObject = task.getResult();
        JSONObject state = lightObject.getJSONObject("state");
        finalLight.setOn(state.getBoolean("on"));
        finalLight.getColor().setHue(state.getInt("hue"));
        finalLight.getColor().setSat(state.getInt("sat"));
        finalLight.getColor().setBri(state.getInt("bri"));

        return fetchLights(keys, object, toAdd, toRemove);
      }
    });
  }

  public Task<Boolean> fetchCurrentLights() {
    synchronized (lock) {
      RootViewModel root = RootViewModel.get();
      
      if (!root.isEnabled()) {
        return Task.forResult(true);
      }
      
      String ipAddress = root.getIpAddress();
      URL url;
      try {
        url = new URL("http", ipAddress, 80, "/api/" + root.getUserName() + "/lights");
      } catch (MalformedURLException e) {
        return Task.forError(e);
      }
      
      final ArrayList<Light> toRemove = new ArrayList<Light>();
      final ArrayList<Light> toAdd = new ArrayList<Light>();
      
      Http http = Http.getInstance();
      return http.get(null, url).onSuccessTask(new Continuation<JSONObject, Task<Boolean>>() {
        @Override
        public Task<Boolean> then(Task<JSONObject> task) throws Exception {
          JSONObject object = task.getResult();
          @SuppressWarnings("unchecked")
          Iterator<String> keys = (Iterator<String>) object.keys();

          // Record all the original lights.
          each(new Visitor<Light>() {
            @Override
            public void visit(Light light) {
              toRemove.add(light);
            }
          });

          return fetchLights(keys, object, toAdd, toRemove);
        }
      }).onSuccess(new Continuation<Boolean, Boolean>() {
        @Override
        public Boolean then(Task<Boolean> arg0) throws Exception {
          Collections.sort(toAdd, new Comparator<Light>() {
            @Override
            public int compare(Light lhs, Light rhs) {
              return lhs.getId().compareTo(rhs.getId());
            }
          });
          for (Light light : toAdd) {
            add(light);
            light.connect();
          }
          for (Light light : toRemove) {
            remove(light);
            light.disconnect();
          }
          return true;
        }
      });
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
