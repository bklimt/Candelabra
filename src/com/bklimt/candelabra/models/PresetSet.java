package com.bklimt.candelabra.models;

import com.bklimt.candelabra.backbone.Collection;

public class PresetSet extends Collection<Preset> {
  @Override
  protected Preset create() {
    return new Preset();
  }

  @Override
  protected String getId(Preset preset) {
    return preset.getName();
  }
}
