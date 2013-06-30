package com.bklimt.candelabra;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class ColorView extends View {
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

  public void setColor(int newColor) {
    color = newColor;
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
  
  private int color = Color.BLACK;
  private Rect drawingRect = new Rect();
  private Paint paint = new Paint();
}
