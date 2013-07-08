package com.bklimt.candelabra.util;

public interface Callback<T> {
  void callback(T result, Exception error);
}
