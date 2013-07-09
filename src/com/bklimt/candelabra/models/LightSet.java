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

import com.bklimt.candelabra.backbone.Collection;
import com.bklimt.candelabra.backbone.Visitor;
import com.bklimt.candelabra.networking.Http;
import com.bklimt.candelabra.util.Callback;

public class LightSet extends Collection<Light> {
  public LightSet() {
  }

  public LightSet(JSONArray json) {
    setJSON(json);
  }

  public void copy(LightSet other) {
    setJSON(other.toJSON());
  }

  private void fetchLights(final Iterator<String> keys, final JSONObject object,
      final ArrayList<Light> toAdd, final ArrayList<Light> toRemove,
      final Callback<Boolean> callback) {
    if (!keys.hasNext()) {
      callback.callback(Boolean.TRUE, null);
      return;
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
      callback.callback(false, e);
      return;
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
      callback.callback(null, e);
      return;
    }
    Http.getInstance().get(null, lightURL, new Callback<JSONObject>() {
      public void callback(JSONObject lightObject, Exception error) {
        if (lightObject == null) {
          callback.callback(null, error);
          return;
        }
        try {
          JSONObject state = lightObject.getJSONObject("state");
          finalLight.setOn(state.getBoolean("on"));
          finalLight.getColor().setHue(state.getInt("hue"));
          finalLight.getColor().setSat(state.getInt("sat"));
          finalLight.getColor().setBri(state.getInt("bri"));
        } catch (JSONException e) {
          callback.callback(null, e);
          return;
        }

        fetchLights(keys, object, toAdd, toRemove, callback);
      }
    });
  }

  public void fetchCurrentLights(final Callback<Boolean> callback) {
    synchronized (lock) {
      RootViewModel root = RootViewModel.get();
      
      if (!root.isEnabled()) {
        callback.callback(true, null);
      }
      
      String ipAddress = root.getIpAddress();
      URL url;
      try {
        url = new URL("http", ipAddress, 80, "/api/" + root.getUserName() + "/lights");
      } catch (MalformedURLException e) {
        callback.callback(null, e);
        return;
      }
      Http.getInstance().get(null, url, new Callback<JSONObject>() {
        public void callback(JSONObject object, Exception error) {
          if (error != null) {
            callback.callback(null, error);
            return;
          }

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
          fetchLights(keys, object, toAdd, toRemove, new Callback<Boolean>() {
            public void callback(Boolean result, Exception error) {
              if (error != null) {
                callback.callback(null, error);
                return;
              }

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
              callback.callback(Boolean.TRUE, null);
            }
          });
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
