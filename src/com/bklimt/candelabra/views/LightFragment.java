package com.bklimt.candelabra.views;

import com.bklimt.candelabra.R;
import com.bklimt.candelabra.models.Light;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class LightFragment extends Fragment {
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.light, container);

    colorView = (ColorView) view.findViewById(R.id.color);
    colorEdit = (EditColor) view.findViewById(R.id.color_edit);

    if (light != null) {
      colorView.setColor(light.getColor());
      colorEdit.setColor(light.getColor());
    }

    return view;
  }

  public void setLight(Light newLight) {
    light = newLight;
    if (colorEdit != null) {
      colorEdit.setColor(newLight.getColor());
    }
    if (colorView != null) {
      colorView.setColor(newLight.getColor());
    }
  }

  ColorView colorView = null;
  EditColor colorEdit = null;
  private Light light = null;
}
