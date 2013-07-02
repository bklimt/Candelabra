package com.bklimt.candelabra.models;

import com.bindroid.trackable.TrackableInt;

import android.graphics.Color;

public class HSVColor {
  public static int getColor(float[] hsv) {
    int color = Color.HSVToColor(hsv);
    return (color != 0 ? color : ghettoColor(hsv));
  }

  private static int ghettoColor(float[] hsv) {
    float hue = hsv[0];
    float saturation = hsv[1];
    float value = hsv[2];
    
    float c = value * saturation;
    float x = c * (1 - Math.abs(hue / 60f % 2 - 1));
    float m = value - c;
    float r = 0;
    float g = 0;
    float b = 0;
    if (hue < 60) {
      r = c;
      g = x;
    } else if (hue < 120) {
      r = x;
      g = c;
    } else if (hue < 180) {
      g = c;
      b = x;
    } else if (hue < 240) {
      g = x;
      b = c;
    } else if (hue < 300) {
      r = x;
      b = c;
    } else if (hue < 360) {
      r = c;
      b = x;
    } else {
      return 0;
    }
    r += m;
    g += m;
    b += m;
    return Color.rgb((int) (r * 255), (int) (g * 255), (int) (b * 255));
  }
  
  public HSVColor() {
  }

  private TrackableInt hue = new TrackableInt();
  public int getHue() {
    return hue.get();
  }
  public void setHue(int newHue) {
    hue.set(newHue);
  }
  
  private TrackableInt sat = new TrackableInt();
  public int getSat() {
    return sat.get();
  }
  public void setSat(int newSat) {
    sat.set(newSat);
  }
  
  private TrackableInt bri = new TrackableInt();
  public int getBri() {
    return bri.get();
  }
  public void setBri(int newBri) {
    bri.set(newBri);
  }

  public int getColor() {
    float[] hsv = { hue.get() * (360.0f / 0x10000), sat.get() / 255.0f, bri.get() / 255.0f };
    return getColor(hsv);
  }
}
