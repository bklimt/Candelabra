package com.bklimt.candelabra.util;

public class Capture<T> {
  private T value;
  
  public Capture(T newValue) {
    value = newValue;
  }
  
  public T get() {
    return value;
  }
  
  public void set(T newValue) {
    value = newValue;
  }
}
