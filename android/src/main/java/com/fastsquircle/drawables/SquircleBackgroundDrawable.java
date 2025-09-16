package com.fastsquircle.drawables;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.facebook.react.uimanager.drawable.BackgroundDrawable;
import com.facebook.react.uimanager.style.ComputedBorderRadius;
import com.fastsquircle.utils.SquirclePathCalculator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SquircleBackgroundDrawable extends ComposedDrawable {

  private BackgroundDrawable base;
  private float cornerSmoothing;

  public SquircleBackgroundDrawable(BackgroundDrawable base, float cornerSmoothing) {
    super(base);
    this.base = base;
    this.cornerSmoothing = cornerSmoothing;
  }

  public void setBase(BackgroundDrawable base) {
    super.updateBase(base);
    this.base = base;
  }

  public void setCornerSmoothing(float cornerSmoothing) {
    this.cornerSmoothing = cornerSmoothing;
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    updatePath();

    var backgroundPaint = getBackgroundPaint();
    var computedBorderRadius = getComputedBorderRadius();
    var backgroundRect = getBackgroundRect();

    if (computedBorderRadius == null) {
      base.draw(canvas);
      return;
    }

    if (computedBorderRadius.isUniform()
      && computedBorderRadius.getTopLeft().getVertical() == 0
      && computedBorderRadius.getTopLeft().getHorizontal() == 0) {
      base.draw(canvas);
      return;
    }

    canvas.save();

    if (backgroundPaint.getAlpha() != 0) {
      var squirclePath = SquirclePathCalculator.getPath(
        computedBorderRadius,
        backgroundRect.width(),
        backgroundRect.height(),
        cornerSmoothing
      );

      canvas.drawPath(squirclePath, backgroundPaint);
    }

    canvas.restore();
  }

  private void updatePath() {
    try {
      Class<?> clazz = base.getClass();
      Method privateMethod = clazz.getDeclaredMethod("updatePath");
      privateMethod.setAccessible(true);

      privateMethod.invoke(base);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Paint getBackgroundPaint() {
    return (Paint) getVariableWithReflection("backgroundPaint");
  }

  private ComputedBorderRadius getComputedBorderRadius() {
    return (ComputedBorderRadius) getVariableWithReflection("computedBorderRadius");
  }

  private RectF getBackgroundRect() {
    return (RectF) getVariableWithReflection("backgroundRect");
  }

  private Object getVariableWithReflection(String fieldName) {
    try {
      Field field = BackgroundDrawable.class.getDeclaredField(fieldName);
      field.setAccessible(true); // Bypass private access

      Object fieldValue = field.get(base);

      field.setAccessible(false);

      return fieldValue;
    } catch (NoSuchFieldException | IllegalAccessException ignored) {
    }

    return null;
  }

}
