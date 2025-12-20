package com.fastsquircle;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.facebook.react.uimanager.drawable.BackgroundDrawable;
import com.facebook.react.uimanager.drawable.CompositeBackgroundDrawable;

public interface SquircleCSSBackgroundManager {

  BackgroundDrawable getCSSBackground();

  void setCornerSmoothing(Drawable background);

  void dispatchDraw(Canvas canvas, CompositeBackgroundDrawable compositeBackgroundDrawable);

}
