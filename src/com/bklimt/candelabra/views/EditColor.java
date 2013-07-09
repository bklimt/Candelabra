package com.bklimt.candelabra.views;

import java.util.logging.Logger;

import com.bklimt.candelabra.backbone.ModelListener;
import com.bklimt.candelabra.models.HSVColor;
import com.bklimt.candelabra.util.SingleItemQueue;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class EditColor extends View implements ModelListener {
  private Rect drawingRect = new Rect();
  private Point point = new Point();
  private Paint paint = new Paint();
  private Bitmap bitmap;

  private int cx;
  private int cy;
  private int innerRadius;
  private int outerRadius;

  private Rect square = new Rect();

  private boolean horizontal;

  private HSVColor color = null;
  private float hue = 0.0f;
  private float saturation = 0.0f;
  private float value = 1.0f;

  private SingleItemQueue bitmapQueue = new SingleItemQueue("Bitmap Queue");

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

  public void setColor(HSVColor newColor) {
    if (color != null) {
      color.removeListener(this);
    }
    color = newColor;
    color.addListener(this);

    float[] hsv = { 0, 1, 1 };
    color.getHSV(hsv);
    hue = hsv[0];
    saturation = hsv[1];
    value = hsv[2];
    if (bitmap != null) {
      updateBitmap(false);
    }
    invalidate();
  }

  public void onChanged(String key, Object newValue) {
    float[] hsv = { 0, 1, 1 };
    color.getHSV(hsv);
    hue = hsv[0];
    saturation = hsv[1];
    value = hsv[2];
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    getDrawingRect(drawingRect);
    if (bitmap == null || bitmap.getWidth() != drawingRect.width() / 2
        || bitmap.getHeight() != drawingRect.height() / 2) {
      updateBitmap(true);
    }
    canvas.drawBitmap(bitmap, null, drawingRect, paint);

    paint.setStyle(Style.STROKE);
    paint.setStrokeWidth(4.0f);
    paint.setColor(Color.WHITE);
    getCirclePoint(point);
    canvas.drawCircle(point.x, point.y, 5, paint);
    getSquarePoint(point);
    canvas.drawCircle(point.x, point.y, 5, paint);

    paint.setStyle(Style.STROKE);
    paint.setStrokeWidth(2.0f);
    paint.setColor(Color.BLACK);
    getCirclePoint(point);
    canvas.drawCircle(point.x, point.y, 5, paint);
    getSquarePoint(point);
    canvas.drawCircle(point.x, point.y, 5, paint);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    int action = event.getAction();
    switch (action) {
      case MotionEvent.ACTION_DOWN: {
        float[] hsv = { 0.0f, 0.0f, 1.0f };
        if (getColorAt((int) event.getX() / 2, (int) event.getY() / 2, hsv)) {
          if (color != null) {
            color.setHSV(hsv);
          }
          hue = hsv[0];
          saturation = hsv[1];
          value = hsv[2];
          // updateBitmap(false);
          invalidate();
        }
        break;
      }
      case MotionEvent.ACTION_MOVE: {
        float[] hsv = { 0.0f, 0.0f, 1.0f };
        if (getColorAt((int) event.getX() / 2, (int) event.getY() / 2, hsv)) {
          if (color != null) {
            color.setHSV(hsv);
          }
          hue = hsv[0];
          saturation = hsv[1];
          value = hsv[2];
          // updateBitmap(false);
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

  private boolean getColorAt(int x, int y, float[] hsv) {
    return getColorAtBasedOn(x, y, hue, value, hsv);
  }

  private boolean getColorAtBasedOn(int x, int y, float hue, float value, float[] hsv) {
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
        hsv[1] = (float) (x - square.left) / square.width();
        hsv[2] = (float) (y - square.top) / square.height();
      } else {
        hsv[1] = (float) (y - square.top) / square.height();
        hsv[2] = (float) (x - square.left) / square.width();
      }
      return true;
    }
    return false;
  }

  private void getCirclePoint(Point point) {
    double angle = (hue * (Math.PI / 180.0f)) - Math.PI;
    float radius = saturation * (outerRadius - innerRadius) + innerRadius;
    float x = (float) Math.cos(angle) * radius + cx;
    float y = (float) Math.sin(angle) * radius + cy;
    point.set((int) x * 2, (int) y * 2);
  }

  private void getSquarePoint(Point point) {
    if (horizontal) {
      float x = (saturation * square.width()) + square.left;
      float y = (value * square.height()) + square.top;
      point.set((int) x * 2, (int) y * 2);
    } else {
      float x = (value * square.width()) + square.left;
      float y = (saturation * square.height()) + square.top;
      point.set((int) x * 2, (int) y * 2);
    }
  }

  private void updateBitmap(boolean replaceBitmap) {
    final int width = drawingRect.width() / 2;
    final int height = drawingRect.height() / 2;

    cx = 5;
    cy = 5;
    outerRadius = 5;

    square.set(0, 0, 5, 5);

    if (width > height) {
      horizontal = true;
      outerRadius = Math.min((width * 2) / 3, height) / 2;
      cx = width / 3;
      cy = height / 2;
      square.set((width * 2) / 3, 0, width, height);
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
    
    final float originalHue = hue;
    final float originalValue = value;
    
    bitmapQueue.enqueue("BITMAP", new Runnable() {
      public void run() {
        final Bitmap newBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        float[] hsv = { 0.0f, 1.0f, 1.0f };
        for (int x = 0; x < width; x++) {
          for (int y = 0; y < height; y++) {
            if (getColorAtBasedOn(x, y, originalHue, originalValue, hsv)) {
              newBitmap.setPixel(x, y, HSVColor.getColor(hsv));
            }
          }
        }
        new Handler(Looper.getMainLooper()).post(new Runnable() {
          public void run() {
            bitmap = newBitmap;
            EditColor.this.invalidate();
            Logger.getLogger(getClass().getName()).info("Finished updating bitmap.");
          }
        });
      }
    });
  }
}
