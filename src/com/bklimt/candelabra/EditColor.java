package com.bklimt.candelabra;

import java.util.ArrayList;
import java.util.logging.Logger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class EditColor extends View {
  public EditColor(Context context) {
    super(context);
    init();
  }

  public EditColor(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public EditColor(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  public void init() {
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    getDrawingRect(drawingRect);
    if (bitmap == null || bitmap.getWidth() != drawingRect.width()
        || bitmap.getHeight() != drawingRect.height()) {
      updateBitmap(true);
    }
    canvas.drawBitmap(bitmap, null, drawingRect, paint);

    paint.setStyle(Style.STROKE);
    paint.setStrokeWidth(2.0f);
    paint.setColor(Color.BLACK);
    Point point = getCirclePoint();
    canvas.drawCircle(point.x, point.y, 5, paint);
    point = getSquarePoint();
    canvas.drawCircle(point.x, point.y, 5, paint);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    int action = event.getAction();
    switch (action) {
      case MotionEvent.ACTION_DOWN: {
        float[] hsv = { 0.0f, 0.0f, 1.0f };
        if (getColorAt((int) event.getX(), (int) event.getY(), hsv)) {
          hue = hsv[0];
          saturation = hsv[1];
          value = hsv[2];
          notifyListeners();
          invalidate();
        }
        break;
      }
      case MotionEvent.ACTION_MOVE: {
        float[] hsv = { 0.0f, 0.0f, 1.0f };
        if (getColorAt((int) event.getX(), (int) event.getY(), hsv)) {
          hue = hsv[0];
          saturation = hsv[1];
          value = hsv[2];
          notifyListeners();
          invalidate();
        }
        break;
      }
      case MotionEvent.ACTION_UP: {
        updateBitmap(false);
        invalidate();
        break;
      }
    }
    return true;
  }

  public void addListener(ColorListener listener) {
    listeners.add(listener);
  }
  
  private void notifyListeners() {
    for (ColorListener listener : listeners) {
      listener.onColorChanged(hue, saturation, value);
    }
  }
  
  private boolean getColorAt(int x, int y, float[] hsv) {
    int dx = x - cx;
    int dy = y - cy;
    int innerRadiusSquared = innerRadius * innerRadius;
    int outerRadiusSquared = outerRadius * outerRadius;
    int radiusSquared = dx * dx + dy * dy;
    if (radiusSquared >= innerRadiusSquared && radiusSquared <= outerRadiusSquared) {
      hsv[0] = (float) Math.min((Math.atan2(dy, dx) + Math.PI) * (180 / Math.PI), 359);
      hsv[1] = (float) (radiusSquared - innerRadiusSquared)
          / (outerRadiusSquared - innerRadiusSquared);
      hsv[2] = value;
      return true;
    } else if (square.contains(x, y)) {
      hsv[0] = hue;
      if (horizontal) {
        hsv[1] = (float) (y - square.top) / square.height();
        hsv[2] = (float) (x - square.left) / square.width();
      } else {
        hsv[1] = (float) (x - square.left) / square.width();
        hsv[2] = (float) (y - square.top) / square.height();
      }
      return true;
    }
    return false;
  }

  private Point getCirclePoint() {
    double angle = (hue * (Math.PI / 180.0f)) - Math.PI;
    float radius = saturation * (outerRadius - innerRadius) + innerRadius;
    float x = (float) Math.cos(angle) * radius + cx;
    float y = (float) Math.sin(angle) * radius + cy;
    return new Point((int) x, (int) y);
  }

  private Point getSquarePoint() {
    if (horizontal) {
      float x = (value * square.width()) + square.left;
      float y = (saturation * square.height()) + square.top;
      return new Point((int) x, (int) y);
    } else {
      float x = (saturation * square.width()) + square.left;
      float y = (value * square.height()) + square.top;
      return new Point((int) x, (int) y);
    }
  }

  private void updateBitmap(boolean replaceBitmap) {
    final int width = drawingRect.width();
    final int height = drawingRect.height();

    cx = 5;
    cy = 5;
    outerRadius = 5;

    square.set(0, 0, 5, 5);

    if (width > height) {
      horizontal = true;
      outerRadius = Math.min(width / 2, height) / 2;
      cx = width / 4;
      cy = height / 2;
      square.set(width / 2, 0, width, height);
    } else {
      horizontal = false;
      outerRadius = Math.min(width, height * 3 / 4) / 2;
      cx = width / 2;
      cy = outerRadius;
      square.set(0, height * 3 / 4, width, height);
    }

    innerRadius = (int) Math.round(outerRadius * 0.10);
    outerRadius = Math.round(outerRadius * 0.9f);

    if (replaceBitmap) {
      bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
    }

    new AsyncTask<Void, Void, Bitmap>() {
      @Override
      protected Bitmap doInBackground(Void... params) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        float[] hsv = { 0.0f, 1.0f, 1.0f };
        for (int x = 0; x < width; x++) {
          for (int y = 0; y < height; y++) {
            if (getColorAt(x, y, hsv)) {
              bitmap.setPixel(x, y, HSVColor.getColor(hsv));
            }
          }
        }
        return bitmap;
      }

      @Override
      protected void onPostExecute(Bitmap newBitmap) {
        bitmap = newBitmap;
        EditColor.this.invalidate();
        Logger.getLogger(getClass().getName()).info("Finished updating bitmap.");
      }
    }.execute();
  }

  private Rect drawingRect = new Rect();
  private Paint paint = new Paint();
  private Bitmap bitmap;

  private int cx;
  private int cy;
  private int innerRadius;
  private int outerRadius;

  private Rect square = new Rect();

  boolean horizontal;

  float hue = 0.0f;
  float saturation = 0.0f;
  float value = 1.0f;
  
  ArrayList<ColorListener> listeners = new ArrayList<ColorListener>();
}
