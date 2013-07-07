package com.bklimt.candelabra.models;

import com.bklimt.candelabra.backbone.Model;

public class Light extends Model {
  public void setDefaults() {
    set("color", new HSVColor());
  }
  
  public HSVColor getColor() {
    return (HSVColor) get("color");
  }

  public String getId() {
    return (String) get("id");
  }

  public void setId(String newId) {
    set("id", newId);
  }

  public String getName() {
    return (String) get("name");
  }

  public void setName(String newName) {
    set("name", newName);
  }
  
  public void applyPreset(Light preset) {
    getColor().setJSON(preset.getColor().toJSON());
  }
}
