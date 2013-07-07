package com.bklimt.candelabra.backbone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

public abstract class Model {
  protected Object lock = new Object();
  private Logger log = Logger.getLogger(getClass().getName());
  private HashMap<String, Object> attributes = new HashMap<String, Object>();
  private ArrayList<ModelListener> listeners = new ArrayList<ModelListener>();

  public Model() {
    setDefaults();
  }

  public Model(JSONObject json) {
    setDefaults();
    setJSON(json);
  }

  public void setDefaults() {
  }

  public Object get(String key) {
    return attributes.get(key);
  }

  public void set(String key, Object value) {
    synchronized (lock) {
      if (!(value instanceof Number || value instanceof String || value instanceof Model ||
          value instanceof Collection)) {
        throw new RuntimeException("Tried to set invalid type on model.");
      }
      Object oldValue = attributes.get(key);
      if (oldValue == value) {
        return;
      }
      if (oldValue != null && oldValue.equals(value)) {
        return;
      }
      attributes.put(key, value);
      notifyChanged(key, oldValue, value);
    }
  }

  public JSONObject toJSON() {
    JSONObject object = new JSONObject();
    synchronized (lock) {
      for (String key : attributes.keySet()) {
        Object value = attributes.get(key);
        try {
          if (value instanceof String || value instanceof Integer) {
            object.put(key, value);
          } else if (value instanceof Model) {
            object.put(key, ((Model) value).toJSON());
          } else if (value instanceof Collection) {
            object.put(key, ((Collection<?>) value).toJSON());
          } else {
            throw new RuntimeException("Invalid attribute value in model: " + value);
          }
        } catch (JSONException jse) {
          // This is dumb.
          Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Unable to create JSON.", jse);
        }
      }
    }
    return object;
  }

  public void setJSON(JSONObject json) {
    synchronized (lock) {
      @SuppressWarnings("unchecked")
      Iterator<String> keys = json.keys();
      while (keys.hasNext()) {
        String key = keys.next();
        Object oldValue = attributes.get(key);
        Object newValue = json.opt(key);
        if (newValue instanceof String || newValue instanceof Integer) {
          set(key, newValue);

        } else if (newValue instanceof JSONObject) {
          if (!(oldValue instanceof Model)) {
            throw new RuntimeException("Tried to parse unknown model type.");
          }
          ((Model) oldValue).setJSON((JSONObject) newValue);

        } else if (newValue instanceof JSONArray) {
          if (!(oldValue instanceof Collection)) {
            throw new RuntimeException("Tried to parse unknown collection type.");
          }
          ((Collection<?>) oldValue).setJSON((JSONArray) newValue);
        }
      }
    }
  }

  public void addListener(ModelListener listener) {
    synchronized (lock) {
      listeners.add(listener);
    }
  }

  public void removeListener(ModelListener listener) {
    synchronized (lock) {
      listeners.remove(listener);
    }
  }

  protected void notifyChanged(String key, Object oldValue, Object newValue) {
    synchronized (lock) {
      log.info("Firing change event for " + key + ": " + oldValue + " -> " + newValue);
      for (ModelListener listener : listeners) {
        listener.onChanged(key, newValue);
      }
    }
  }

  public void bindToEditText(final Activity activity, int id, final String key) {
    final EditText editText = (EditText) activity.findViewById(id);

    final Capture<Boolean> handling = new Capture<Boolean>(false);
    editText.setText((String) get("key"));

    editText.addTextChangedListener(new TextWatcher() {
      @Override
      public void afterTextChanged(Editable s) {
      }

      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!handling.get()) {
          handling.set(true);
          set(key, s.toString());
          handling.set(false);
        }
      }
    });

    addListener(new ModelListener() {
      @Override
      public void onChanged(String key, final Object value) {
        activity.runOnUiThread(new Runnable() {
          public void run() {
            editText.setText((String) value);
          }
        });
      }
    });
  }
}
