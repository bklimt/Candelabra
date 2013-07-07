package com.bklimt.candelabra.backbone;

public interface CollectionListener<T> {
  void onAdd(T item);
  void onRemove(T item);
}
