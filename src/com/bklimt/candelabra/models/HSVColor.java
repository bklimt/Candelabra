package com.bklimt.candelabra.models;

import com.bklimt.candelabra.backbone.Model;

import android.graphics.Color;

public class HSVColor extends Model {
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

  public int getHue() {
    return (Integer)get("hue");
  }

  public void setHue(int newHue) {
    set("hue", newHue);
  }

  public int getSat() {
    return (Integer)get("sat");
  }

  public void setSat(int newSat) {
    set("sat", newSat);
  }

  public int getBri() {
    return (Integer)get("bri");
  }

  public void setBri(int newBri) {
    set("bri", newBri);
  }

  public void setHSV(float[] hsv) {
    setHue((int)(hsv[0] * 0x10000) / 360);
    setSat((int)(hsv[1] * 255));
    setBri((int)(hsv[2] * 255));
  }
  
  public void getHSV(float hsv[]) {
    hsv[0] = getHue() * (360.0f / 0x10000);
    hsv[1] = getSat() / 255.0f;
    hsv[2] = getBri() / 255.0f;
  }
  
  public int getColor() {
    synchronized (lock) {
      float[] hsv = { 0, 1, 1 };
      getHSV(hsv);
      return getColor(hsv);
    }
  }
}
