package com.bklimt.candelabra.views;

import com.bklimt.candelabra.backbone.ModelListener;
import com.bklimt.candelabra.models.HSVColor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class ColorView extends View implements ModelListener {
  public ColorView(Context context) {
    super(context);
    init();
  }

  public ColorView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public ColorView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  public void init() {
  }

  public void setColor(HSVColor newColor) {
    if (hsvColor != null) {
      hsvColor.removeListener(this);
    }
    hsvColor = newColor;
    hsvColor.addListener(this);
    color = newColor.getColor();
    invalidate();
  }

  @Override
  public void onChanged(String key, Object value) {
    color = hsvColor.getColor();
    invalidate();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    paint.setColor(color);
    paint.setStyle(Style.FILL);
    getDrawingRect(drawingRect);
    canvas.drawRect(drawingRect, paint);
  }
  
  private HSVColor hsvColor = new HSVColor();
  private int color = Color.BLACK;
  private Rect drawingRect = new Rect();
  private Paint paint = new Paint();
}
