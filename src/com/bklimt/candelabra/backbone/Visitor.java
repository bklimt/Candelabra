package com.bklimt.candelabra.backbone;

public interface Visitor<T extends Model> {
  void visit(T model);
}
