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
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ToggleButton;

public abstract class Model {
  protected Object lock = new Object();
  private Logger log = Logger.getLogger(getClass().getName());

  private HashMap<String, Object> attributes = new HashMap<String, Object>();
  private ArrayList<ModelListener> listeners = new ArrayList<ModelListener>();

  private HashMap<String, EditText> boundEditTexts = new HashMap<String, EditText>();
  private HashMap<String, ToggleButton> boundToggleButtons = new HashMap<String, ToggleButton>();

  private HashMap<String, TextWatcher> textWatchers = new HashMap<String, TextWatcher>();

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
      if (!(value instanceof Number || value instanceof Boolean || value instanceof String
          || value instanceof Model || value instanceof Collection)) {
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
          if (value instanceof String || value instanceof Integer || value instanceof Boolean) {
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
        if (newValue instanceof String || newValue instanceof Integer
            || newValue instanceof Boolean) {
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

  protected void notifyChanged(final String key, Object oldValue, final Object newValue) {
    synchronized (lock) {
      log.info("Firing change event for " + key + ": " + oldValue + " -> " + newValue);
      for (ModelListener listener : listeners) {
        listener.onChanged(key, newValue);
      }
      Runnable notifyBoundControls = new Runnable() {
        public void run() {
          EditText editText = boundEditTexts.get(key);
          if (editText != null) {
            editText.setText((String) newValue);
          }
          ToggleButton toggleButton = boundToggleButtons.get(key);
          if (toggleButton != null) {
            toggleButton.setChecked(newValue != null && ((Boolean) newValue).booleanValue());
          }
        }
      };
      if (Looper.myLooper() == Looper.getMainLooper()) {
        notifyBoundControls.run();
      } else {
        new Handler(Looper.getMainLooper()).post(notifyBoundControls);
      }
    }
  }

  public void bindToEditText(final Activity activity, int id, final String key) {
    synchronized (lock) {
      final EditText editText = (EditText) activity.findViewById(id);
      editText.setText((String) get(key));

      TextWatcher textWatcher = textWatchers.get(key);
      if (textWatcher == null) {
        textWatcher = new TextWatcher() {
          @Override
          public void afterTextChanged(Editable s) {
          }

          @Override
          public void beforeTextChanged(CharSequence s, int start, int count, int after) {
          }

          @Override
          public void onTextChanged(CharSequence s, int start, int before, int count) {
            set(key, s.toString());
          }
        };
      }

      editText.addTextChangedListener(textWatcher);
      boundEditTexts.put(key, editText);
    }
  }

  public void unbindEditText(final Activity activity, int id, String key) {
    final EditText editText = (EditText) activity.findViewById(id);
    editText.removeTextChangedListener(textWatchers.get(key));
    boundEditTexts.remove(editText);
  }

  public void bindToToggleButton(final Activity activity, int id, final String key) {
    final ToggleButton toggleButton = (ToggleButton) activity.findViewById(id);
    Boolean on = (Boolean) get(key);
    toggleButton.setChecked(on != null && on.booleanValue());
    toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        set(key, isChecked);
      }
    });
    boundToggleButtons.put(key, toggleButton);
  }

  public void unbindToggleButton(final Activity activity, int id, String key) {
    final ToggleButton toggleButton = (ToggleButton) activity.findViewById(id);
    toggleButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
      }
    });
    boundToggleButtons.remove(key);
  }
}
