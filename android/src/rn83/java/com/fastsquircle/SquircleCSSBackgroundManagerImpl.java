package com.fastsquircle;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.facebook.react.uimanager.drawable.BackgroundDrawable;
import com.facebook.react.uimanager.drawable.CompositeBackgroundDrawable;

public class SquircleCSSBackgroundManagerImpl implements SquircleCSSBackgroundManager {
  @Override
  public BackgroundDrawable getCSSBackground() {
    return null;
  }

  @Override
  public void setCornerSmoothing(Drawable background) {
  }

  @Override
  public void dispatchDraw(Canvas canvas, CompositeBackgroundDrawable compositeBackgroundDrawable) {
  }

}
