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
import com.facebook.react.uimanager.drawable.BackgroundDrawable;
import com.facebook.react.uimanager.drawable.BorderDrawable;
import com.facebook.react.uimanager.drawable.CompositeBackgroundDrawable;
import com.facebook.react.uimanager.drawable.OutlineDrawable;
import com.facebook.react.uimanager.drawable.OutsetBoxShadowDrawable;
import com.facebook.react.uimanager.style.Overflow;
import com.facebook.react.views.view.ReactViewGroup;
import com.fastsquircle.drawables.SquircleBackgroundDrawable;
import com.fastsquircle.drawables.SquircleBorderDrawable;
import com.fastsquircle.drawables.SquircleOutlineDrawable;
import com.fastsquircle.drawables.SquircleOutsetShadowDrawable;

import java.util.Collections;
import java.util.stream.Collectors;

public class FastSquircleView extends ReactViewGroup {

  private float cornerSmoothing = 0.0f;

  private SquircleBackgroundDrawable squircleBackgroundDrawable;
  private SquircleBorderDrawable squircleBorderDrawable;

  private SquircleOutlineDrawable squircleOutlineDrawable;

  private final SquircleCSSBackgroundManager cssBackgroundManager = new SquircleCSSBackgroundManager();
  private final SquircleBackgroundImageManager backgroundImageManager = new SquircleBackgroundImageManager();

  @OptIn(markerClass = UnstableReactNativeAPI.class)
  public FastSquircleView(@Nullable Context context) {
    super(context);

    setBackground(new CompositeBackgroundDrawable(
      getContext(),
      getBackground(),
      Collections.emptyList(),
      cssBackgroundManager.getCSSBackground(getContext()),
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
        return new SquircleOutsetShadowDrawable((OutsetBoxShadowDrawable) s, this.cornerSmoothing);
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

    var backgroundImageDrawManager = this.backgroundImageManager.getDrawManager();

    int backgroundDrawableIndex = -1;
    int borderDrawableIndex = -1;
    int outlineDrawableIndex = -1;
    for (int i = 0; i < layerDrawable.getNumberOfLayers(); i++) {
      var layer = layerDrawable.getDrawable(i);

      if (backgroundDrawableIndex < 0 && layer instanceof BackgroundDrawable) {
        backgroundDrawableIndex = i;
      }

      if (borderDrawableIndex < 0 && layer instanceof BorderDrawable) {
        borderDrawableIndex = i;
      }

      if (outlineDrawableIndex < 0 && layer instanceof OutlineDrawable) {
        outlineDrawableIndex = i;
      }

      backgroundImageDrawManager.checkLayer(layer, i);
    }

    BackgroundDrawable backgroundDrawable = null;
    if (backgroundDrawableIndex >= 0) {
      backgroundDrawable = (BackgroundDrawable) layerDrawable.getDrawable(backgroundDrawableIndex);
      if (this.squircleBackgroundDrawable == null) {
        this.squircleBackgroundDrawable = new SquircleBackgroundDrawable(backgroundDrawable, this.cornerSmoothing);
      } else {
        this.squircleBackgroundDrawable.setBase(backgroundDrawable);
      }
    }


    BorderDrawable borderDrawable = null;
    if (borderDrawableIndex >= 0) {
      borderDrawable = (BorderDrawable) layerDrawable.getDrawable(borderDrawableIndex);

      if (this.squircleBorderDrawable == null) {
        this.squircleBorderDrawable = new SquircleBorderDrawable(borderDrawable, this.cornerSmoothing);
      } else {
        this.squircleBorderDrawable.setBase(borderDrawable);
      }
    }

    OutlineDrawable outlineDrawable = null;
    if (outlineDrawableIndex >= 0) {
      outlineDrawable = (OutlineDrawable) layerDrawable.getDrawable(outlineDrawableIndex);

      if (this.squircleOutlineDrawable == null) {
        this.squircleOutlineDrawable = new SquircleOutlineDrawable(outlineDrawable, this.cornerSmoothing);
      } else {
        this.squircleOutlineDrawable.setBase(outlineDrawable);
      }
    }

    if (backgroundDrawableIndex >= 0) layerDrawable.setDrawable(backgroundDrawableIndex, this.squircleBackgroundDrawable);
    if (borderDrawableIndex >= 0) layerDrawable.setDrawable(borderDrawableIndex, this.squircleBorderDrawable);
    if (outlineDrawableIndex >= 0) layerDrawable.setDrawable(outlineDrawableIndex, this.squircleOutlineDrawable);

    backgroundImageDrawManager.beforeDraw(layerDrawable);

    super.draw(canvas);

    if (backgroundDrawableIndex >= 0) layerDrawable.setDrawable(backgroundDrawableIndex, backgroundDrawable);
    if (borderDrawableIndex >= 0) layerDrawable.setDrawable(borderDrawableIndex, borderDrawable);
    if (outlineDrawableIndex >= 0) layerDrawable.setDrawable(outlineDrawableIndex, outlineDrawable);

    backgroundImageDrawManager.afterDraw(layerDrawable);
  }

  @OptIn(markerClass = UnstableReactNativeAPI.class)
  public void setCornerSmoothing(float cornerSmoothing) {
    this.cornerSmoothing = cornerSmoothing;

    if (this.squircleBackgroundDrawable != null) {
      this.squircleBackgroundDrawable.setCornerSmoothing(cornerSmoothing);
    }

    if (this.squircleBorderDrawable != null) {
      this.squircleBorderDrawable.setCornerSmoothing(cornerSmoothing);
    }

    if (this.squircleOutlineDrawable != null) {
      this.squircleOutlineDrawable.setCornerSmoothing(cornerSmoothing);
    }

    this.cssBackgroundManager.setCornerSmoothing(getBackground(), cornerSmoothing);
    this.backgroundImageManager.setCornerSmoothing(cornerSmoothing);

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

    this.cssBackgroundManager.dispatchDraw(canvas, compositeBackground);
    super.dispatchDraw(canvas);
  }
}
