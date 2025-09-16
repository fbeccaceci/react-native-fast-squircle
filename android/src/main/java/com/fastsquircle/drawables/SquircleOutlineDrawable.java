package com.fastsquircle.drawables;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;

import androidx.annotation.NonNull;

import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.drawable.OutlineDrawable;
import com.facebook.react.uimanager.style.BorderRadiusStyle;
import com.facebook.react.uimanager.style.ComputedBorderRadius;
import com.facebook.react.uimanager.style.CornerRadii;
import com.fastsquircle.utils.SquirclePathCalculator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SquircleOutlineDrawable extends ComposedDrawable {

  private OutlineDrawable base;

  private float cornerSmoothing;

  private ComputedBorderRadius mComputedBorderRadius = null;

  public SquircleOutlineDrawable(OutlineDrawable base, float cornerSmoothing) {
    super(base);
    this.base = base;
    this.cornerSmoothing = cornerSmoothing;
  }

  public void setBase(OutlineDrawable base) {
    super.updateBase(base);
    this.base = base;
  }

  public void setCornerSmoothing(float cornerSmoothing) {
    this.cornerSmoothing = cornerSmoothing;
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    var outlineWidth = getOutlineWidth();
    if (outlineWidth == 0f) {
      super.draw(canvas);
      return;
    }

    var borderRadius = base.getBorderRadius();
    var layoutDirection = base.getLayoutDirection();
    var context = getContext();
    var bounds = base.getBounds();

    if (borderRadius != null) {
      this.mComputedBorderRadius = borderRadius.resolve(
        layoutDirection,
        context,
        PixelUtil.toDIPFromPixel(bounds.width()),
        PixelUtil.toDIPFromPixel(bounds.height())
      );
    }

    if (mComputedBorderRadius != null && !mComputedBorderRadius.hasRoundedBorders()) {
      super.draw(canvas);
      return;
    }

    updateOutlineRect();

    var topLeftRadius = new CornerRadii(0f, 0f);
    var topRightRadius = new CornerRadii(0f, 0f);
    var bottomLeftRadius = new CornerRadii(0f, 0f);
    var bottomRightRadius = new CornerRadii(0f, 0f);

    var outlineOffset = base.getOutlineOffset();
    var radiusIncrement = PixelUtil.toDIPFromPixel(outlineWidth / 2f + outlineOffset);

    if (this.mComputedBorderRadius != null) {
      topLeftRadius = new CornerRadii(
        this.mComputedBorderRadius.getTopLeft().getHorizontal() + radiusIncrement,
        this.mComputedBorderRadius.getTopLeft().getVertical() + radiusIncrement
      );

      topRightRadius = new CornerRadii(
        this.mComputedBorderRadius.getTopRight().getHorizontal() + radiusIncrement,
        this.mComputedBorderRadius.getTopRight().getVertical() + radiusIncrement
      );

      bottomLeftRadius = new CornerRadii(
        this.mComputedBorderRadius.getBottomRight().getHorizontal() + radiusIncrement,
        this.mComputedBorderRadius.getBottomLeft().getVertical() + radiusIncrement
      );

      bottomRightRadius = new CornerRadii(
        this.mComputedBorderRadius.getBottomRight().getHorizontal() + radiusIncrement,
        this.mComputedBorderRadius.getBottomRight().getVertical() + radiusIncrement
      );
    }

    var outlinePathBorderRadius = new ComputedBorderRadius(
      topLeftRadius, topRightRadius, bottomLeftRadius, bottomRightRadius
    );

    var tempRectForOutline = getTempRectForOutline();
    var outlinePaint = getOutlinePaint();

    var outlinePath = SquirclePathCalculator.getPath(
      outlinePathBorderRadius,
      tempRectForOutline.width(),
      tempRectForOutline.height(),
      this.cornerSmoothing
    );

    var distance = -outlineWidth / 2f - outlineOffset;
    outlinePath.offset(distance, distance);

    canvas.drawPath(outlinePath, outlinePaint);
  }

  private Float getOutlineWidth() {
    return (Float) getVariableWithReflection("outlineWidth");
  }

  private Context getContext() {
    return (Context) getVariableWithReflection("context");
  }

  private RectF getTempRectForOutline() {
    return (RectF)getVariableWithReflection("tempRectForOutline");
  }

  private Paint getOutlinePaint() {
    return (Paint) getVariableWithReflection("outlinePaint");
  }

  private void updateOutlineRect() {
    try {
      Class<?> clazz = base.getClass();
      Method privateMethod = clazz.getDeclaredMethod("updateOutlineRect");
      privateMethod.setAccessible(true);

      privateMethod.invoke(base);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Object getVariableWithReflection(String fieldName) {
    try {
      Field field = OutlineDrawable.class.getDeclaredField(fieldName);
      field.setAccessible(true); // Bypass private access

      Object fieldValue = field.get(base);

      field.setAccessible(false);

      return fieldValue;
    } catch (NoSuchFieldException | IllegalAccessException ignored) {
    }

    return null;
  }
}
