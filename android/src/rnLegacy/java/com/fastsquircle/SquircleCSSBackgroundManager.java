package com.fastsquircle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import androidx.annotation.OptIn;

import com.facebook.react.common.annotations.UnstableReactNativeAPI;
import com.facebook.react.internal.featureflags.ReactNativeFeatureFlags;
import com.facebook.react.uimanager.drawable.BackgroundDrawable;
import com.facebook.react.uimanager.drawable.CSSBackgroundDrawable;
import com.facebook.react.uimanager.drawable.CompositeBackgroundDrawable;
import com.fastsquircle.drawables.SquircleCSSBackgroundDrawable;
import com.fastsquircle.utils.SquirclePathCalculator;

public class SquircleCSSBackgroundManager {
  @OptIn(markerClass = UnstableReactNativeAPI.class)
  public SquircleCSSBackgroundDrawable getCSSBackground(Context context) {
    return ReactNativeFeatureFlags.enableNewBackgroundAndBorderDrawables()
      ? null
      : new SquircleCSSBackgroundDrawable(context, 0);
  }

  @OptIn(markerClass = UnstableReactNativeAPI.class)
  public void setCornerSmoothing(Drawable background, float cornerSmoothing) {
    if (background instanceof CompositeBackgroundDrawable compositeBackground) {

      CSSBackgroundDrawable cssBackground = compositeBackground.getCssBackground();
      if (cssBackground instanceof SquircleCSSBackgroundDrawable squircleCssBackground) {
        squircleCssBackground.setCornerSmoothing(cornerSmoothing);
      }
    }
  }

  @OptIn(markerClass = UnstableReactNativeAPI.class)
  public void dispatchDraw(Canvas canvas, CompositeBackgroundDrawable compositeBackground) {
    CSSBackgroundDrawable cssBackground = compositeBackground.getCssBackground();

    if (!(cssBackground instanceof SquircleCSSBackgroundDrawable squircleCssBackground)) {
      return;
    }

    var borderRadius = squircleCssBackground.getComputedBorderRadiusBorderRadius();
    var borderWidth = squircleCssBackground.getDirectionAwareBorderInsets();
    var cornerSmoothing = squircleCssBackground.getCornerSmoothing();

    var squirclePath = SquirclePathCalculator.getPath(
      borderRadius,
      squircleCssBackground.getBounds().width() - (borderWidth.left + borderWidth.right),
      squircleCssBackground.getBounds().height() - (borderWidth.top + borderWidth.bottom),
      cornerSmoothing
    );

    squirclePath.offset(borderWidth.left, borderWidth.top);
    canvas.clipPath(squirclePath);
  }
}
