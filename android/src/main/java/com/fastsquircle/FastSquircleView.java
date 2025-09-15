package com.fastsquircle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;

import com.facebook.react.common.annotations.UnstableReactNativeAPI;
import com.facebook.react.internal.featureflags.ReactNativeFeatureFlags;
import com.facebook.react.uimanager.drawable.BackgroundDrawable;
import com.facebook.react.uimanager.drawable.CSSBackgroundDrawable;
import com.facebook.react.uimanager.drawable.CompositeBackgroundDrawable;
import com.facebook.react.uimanager.drawable.OutsetBoxShadowDrawable;
import com.facebook.react.uimanager.style.Overflow;
import com.facebook.react.views.view.ReactViewGroup;
import com.fastsquircle.drawables.SquircleBackgroundDrawable;
import com.fastsquircle.drawables.SquircleCSSBackgroundDrawable;
import com.fastsquircle.drawables.SquircleOutsetShadowDrawable;
import com.fastsquircle.utils.SquirclePathCalculator;

import java.util.Collections;
import java.util.stream.Collectors;

public class FastSquircleView extends ReactViewGroup {

  private float cornerSmoothing = 0.0f;

  private SquircleBackgroundDrawable squircleBackgroundDrawable;

  @OptIn(markerClass = UnstableReactNativeAPI.class)
  public FastSquircleView(@Nullable Context context) {
    super(context);

    var cssBackground = ReactNativeFeatureFlags.enableNewBackgroundAndBorderDrawables()
      ? null
      : new SquircleCSSBackgroundDrawable(getContext(), 0);

    setBackground(new CompositeBackgroundDrawable(
      getContext(),
      getBackground(),
      Collections.emptyList(),
      cssBackground,
      null,
      null,
      null,
      Collections.emptyList(),
      null,
      null,
      null
    ));
  }

  @Override
  public void setBackground(Drawable background) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
      super.setBackground(background);
      return;
    }

    if (!(background instanceof CompositeBackgroundDrawable compositeBackground)) {
      super.setBackground(background);
      return;
    }

    if (compositeBackground.getOuterShadows().isEmpty()) {
      super.setBackground(background);
      return;
    }

    var enhancedOutsetShadows = compositeBackground.getOuterShadows().stream().map(s -> {
      if (s instanceof OutsetBoxShadowDrawable) {
        return new SquircleOutsetShadowDrawable((OutsetBoxShadowDrawable) s);
      }

      return s;
    }).collect(Collectors.toList());

    var newBackground = compositeBackground.withNewShadows(enhancedOutsetShadows, compositeBackground.getInnerShadows());

    super.setBackground(newBackground);
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    var background = getBackground();
    if (!(background instanceof LayerDrawable layerDrawable)) {
      super.draw(canvas);
      return;
    }

    int backgroundDrawableIndex = -1;
    for (int i = 0; i < layerDrawable.getNumberOfLayers(); i++) {
      var layer = layerDrawable.getDrawable(i);

      if (layer instanceof BackgroundDrawable) {
        backgroundDrawableIndex = i;
        break;
      }
    }

    var backgroundDrawable = (BackgroundDrawable) layerDrawable.getDrawable(backgroundDrawableIndex);
    SquircleBackgroundDrawable customBgDrawable;
    if (this.squircleBackgroundDrawable == null) {
      customBgDrawable = new SquircleBackgroundDrawable(backgroundDrawable, this.cornerSmoothing);
      this.squircleBackgroundDrawable = customBgDrawable;
    } else {
      customBgDrawable = this.squircleBackgroundDrawable;
      customBgDrawable.setBase(backgroundDrawable);
    }

    layerDrawable.setDrawable(backgroundDrawableIndex, customBgDrawable);
    super.draw(canvas);
    layerDrawable.setDrawable(backgroundDrawableIndex, backgroundDrawable);
  }

  @OptIn(markerClass = UnstableReactNativeAPI.class)
  public void setCornerSmoothing(float cornerSmoothing) {
    this.cornerSmoothing = cornerSmoothing;

    if (this.squircleBackgroundDrawable != null) {
      this.squircleBackgroundDrawable.setCornerSmoothing(cornerSmoothing);
    }

    var background = getBackground();
    if (background instanceof CompositeBackgroundDrawable compositeBackground) {

      CSSBackgroundDrawable cssBackground = compositeBackground.getCssBackground();
      if (cssBackground instanceof SquircleCSSBackgroundDrawable squircleCssBackground) {
        squircleCssBackground.setCornerSmoothing(cornerSmoothing);
      }
    }

    invalidate();
    invalidateOutline();
  }

  @OptIn(markerClass = UnstableReactNativeAPI.class)
  @Override
  protected void dispatchDraw(Canvas canvas) {
    var overflowString = getOverflow();
    if (overflowString == null) {
      super.dispatchDraw(canvas);
      return;
    }

    var overflow = Overflow.fromString(overflowString);
    if (overflow == Overflow.VISIBLE) {
      super.dispatchDraw(canvas);
      return;
    }

    var background = getBackground();
    if (!(background instanceof CompositeBackgroundDrawable compositeBackground)) {
      super.dispatchDraw(canvas);
      return;
    }

    CSSBackgroundDrawable cssBackground = compositeBackground.getCssBackground();

    if (!(cssBackground instanceof SquircleCSSBackgroundDrawable squircleCssBackground)) {
      super.dispatchDraw(canvas);
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

    super.dispatchDraw(canvas);
  }
}
