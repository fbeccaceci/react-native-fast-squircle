package com.fastsquircle;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.facebook.react.uimanager.drawable.BackgroundDrawable;
import com.facebook.react.uimanager.drawable.CompositeBackgroundDrawable;
import com.facebook.react.uimanager.drawable.CSSBackgroundDrawable;
import com.fastsquircle.drawables.SquircleCSSBackgroundDrawable;

public class SquircleCSSBackgroundManagerImpl implements SquircleCSSBackgroundManager {
  @Override
  public BackgroundDrawable getCSSBackground() {
    var cssBackground = ReactNativeFeatureFlags.enableNewBackgroundAndBorderDrawables()
      ? null
      : new SquircleCSSBackgroundDrawable(getContext(), 0);
  }

  @Override
  public void setCornerSmoothing(Drawable background) {

  }

  @Override
  public void dispatchDraw(Canvas canvas, CompositeBackgroundDrawable compositeBackgroundDrawable) {

  }
}
