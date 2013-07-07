package com.bklimt.candelabra.backbone;

import java.util.ArrayList;

import org.json.JSONArray;

public abstract class Collection<T extends Model> {
  private ArrayList<T> items = new ArrayList<T>();
  private ArrayList<CollectionListener<T>> listeners = new ArrayList<CollectionListener<T>>();
  protected Object lock = new Object();
  
  protected abstract T create();
  protected abstract String getId(T item);

  public Collection() {
  }

  public Collection(JSONArray json) {
    setJSON(json);
  }
  
  public JSONArray toJSON() {
    synchronized (lock) {
      JSONArray json = new JSONArray();
      for (T item : items) {
        json.put(item.toJSON());
      }
      return json;
    }
  }

  public void setJSON(JSONArray json) {
    synchronized (lock) {
      boolean[] found = new boolean[items.size()];
      for (int i = 0; i < json.length(); ++i) {
        if (json.isNull(i)) {
          continue;
        }
  
        T newItem = create();
        newItem.setJSON(json.optJSONObject(i));
        
        T item = findById(getId(newItem));
        if (item != null) {
          found[items.indexOf(item)] = true;
          newItem = item;
          newItem.setJSON(json.optJSONObject(i));
        } else {
          add(newItem);
        }
      }
      for (int i = found.length - 1; i >= 0; --i) {
        if (!found[i]) {
          remove(items.get(i));
        }
      }
    }
  }

  public int size() {
    synchronized (lock) {
      return items.size();
    }
  }
  
  public void add(T item) {
    synchronized (lock) {
      items.add(item);
      for (CollectionListener<T> listener : listeners) {
        listener.onAdd(item);
      }
    }
  }
  
  public void remove(T item) {
    synchronized (lock) {
      items.remove(item);
      for (CollectionListener<T> listener : listeners) {
        listener.onRemove(item);
      }
    }
  }
  
  public void each(Visitor<T> visitor) {
    synchronized (lock) {
      for (T item : items) {
        visitor.visit(item);
      }
    }
  }
  
  public void clear() {
    synchronized (lock) {
      each(new Visitor<T>() {
        @Override
        public void visit(T model) {
          remove(model);
        }
      });
    }
  }
  
  public T findById(String id) {
    synchronized (lock) {
      for (T item : items) {
        if (getId(item).equals(id)) {
          return item;
        }
      }
      return null;
    }
  }
  
  public void addListener(CollectionListener<T> listener) {
    synchronized (lock) {
      listeners.add(listener);
    }
  }
  
  public void removeListener(CollectionListener<T> listener) {
    synchronized (lock) {
      listeners.remove(listener);
    }
  }
}
