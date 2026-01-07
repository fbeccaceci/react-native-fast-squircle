package com.fastsquircle.drawables;

import android.graphics.Canvas;
import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.drawable.BackgroundImageDrawable;
import com.fastsquircle.utils.SquirclePathCalculator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Wraps BackgroundImageDrawable (RN 0.83+) to render background images/gradients
 * with squircle corners instead of regular rounded rectangles.
 */
public class SquircleBackgroundImageDrawable extends ComposedDrawable {

  private BackgroundImageDrawable base;
  private float cornerSmoothing;

  public SquircleBackgroundImageDrawable(BackgroundImageDrawable base, float cornerSmoothing) {
    super(base);
    this.base = base;
    this.cornerSmoothing = cornerSmoothing;
  }

  public void setBase(BackgroundImageDrawable base) {
    super.updateBase(base);
    this.base = base;
  }

  public void setCornerSmoothing(float cornerSmoothing) {
    this.cornerSmoothing = cornerSmoothing;
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    var borderRadius = base.getBorderRadius();
    if (borderRadius == null) {
      base.draw(canvas);
      return;
    }

    var context = getContext();
    if (context == null) {
      base.draw(canvas);
      return;
    }

    var computedBorderRadius = borderRadius.resolve(
      getLayoutDirection(),
      context,
      PixelUtil.toDIPFromPixel(getBounds().width()),
      PixelUtil.toDIPFromPixel(getBounds().height())
    );

    if (computedBorderRadius == null || !computedBorderRadius.hasRoundedBorders()) {
      base.draw(canvas);
      return;
    }

    // Create squircle path and clip to it, then let base draw
    canvas.save();

    var bounds = getBounds();
    var squirclePath = SquirclePathCalculator.getPath(
      computedBorderRadius,
      bounds.width(),
      bounds.height(),
      cornerSmoothing
    );

    // Clip to the squircle path
    canvas.clipPath(squirclePath);
    
    // Let the base drawable draw itself (which will draw the gradient)
    // but it will be clipped to our squircle path
    base.draw(canvas);

    canvas.restore();
  }

  @Nullable
  private android.content.Context getContext() {
    try {
      Field field = BackgroundImageDrawable.class.getDeclaredField("context");
      field.setAccessible(true);
      Object fieldValue = field.get(base);
      field.setAccessible(false);
      return (android.content.Context) fieldValue;
    } catch (NoSuchFieldException | IllegalAccessException | ClassCastException ignored) {
    }
    return null;
  }

  @Override
  protected void onBoundsChange(@NonNull Rect bounds) {
    super.onBoundsChange(bounds);
    if (base == null) return;

    try {
      Class<?> clazz = base.getClass();
      Method privateMethod = clazz.getDeclaredMethod("onBoundsChange", Rect.class);
      privateMethod.setAccessible(true);
      privateMethod.invoke(base, bounds);
    } catch (Exception ignored) {
    }
  }
}

