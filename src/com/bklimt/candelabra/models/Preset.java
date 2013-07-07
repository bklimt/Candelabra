package com.bklimt.candelabra.models;

import com.bklimt.candelabra.backbone.Model;

public class Preset extends Model {
  public void setDefaults() {
    set("lights", new LightSet());
  }

  public String getName() {
    return (String) get("name");
  }

  public void setName(String newName) {
    set("name", newName);
  }
  
  public LightSet getLights() {
    return (LightSet) get("lights");
  }
}
