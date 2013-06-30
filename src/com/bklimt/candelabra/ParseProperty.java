package com.bklimt.candelabra;

import com.bindroid.utils.Action;
import com.bindroid.utils.Function;
import com.bindroid.utils.Property;
import com.parse.ParseObject;

public class ParseProperty<T> extends Property<T> {
  ParseProperty(final ParseObject object, final String key) {
    super(new Function<T>() {
      @SuppressWarnings("unchecked")
      @Override
      public T evaluate() {
        return (T) object.get(key);
      }
    }, new Action<T>() {
      @Override
      public void invoke(T parameter) {
        object.put(key, parameter);
      }
    });
  }
}
