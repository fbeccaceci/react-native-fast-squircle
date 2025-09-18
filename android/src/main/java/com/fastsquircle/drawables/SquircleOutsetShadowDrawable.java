package com.fastsquircle.drawables;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.drawable.OutsetBoxShadowDrawable;
import com.facebook.react.uimanager.style.ComputedBorderRadius;
import com.facebook.react.uimanager.style.CornerRadii;
import com.fastsquircle.utils.SquirclePathCalculator;

import java.lang.reflect.Field;

@RequiresApi(api = Build.VERSION_CODES.P)
public class SquircleOutsetShadowDrawable extends ComposedDrawable {

  private final OutsetBoxShadowDrawable base;

  private final float cornerSmoothing;

  public SquircleOutsetShadowDrawable(OutsetBoxShadowDrawable base, float cornerSmoothing) {
    super(base);
    this.base = base;
    this.cornerSmoothing = cornerSmoothing;
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    var borderRadius = base.getBorderRadius();
    if (borderRadius == null) {
      base.draw(canvas);
      return;
    }

    var computedBorderRadius = borderRadius.resolve(
      getLayoutDirection(),
      getContext(),
      getBounds().width(),
      getBounds().height()
    );

    if (computedBorderRadius.isUniform() && computedBorderRadius.getTopLeft().getHorizontal() == 0f) {
      base.draw(canvas);
      return;
    }

    var spreadExtent = PixelUtil.toPixelFromDIP(getSpread());
    var shadowRect = new RectF(getBounds());
    shadowRect.inset(-spreadExtent, -spreadExtent);
    shadowRect.offset(PixelUtil.toPixelFromDIP(getOffsetX()), PixelUtil.toPixelFromDIP(getOffsetY()));

    var saveCount = canvas.save();

    var radiusIncrease = PixelUtil.toDIPFromPixel(spreadExtent);

    var squirclePathBorderRadius = new ComputedBorderRadius(
      new CornerRadii(
        computedBorderRadius.getTopLeft().getHorizontal() + radiusIncrease,
        computedBorderRadius.getTopLeft().getVertical() + radiusIncrease
      ),
      new CornerRadii(
        computedBorderRadius.getTopRight().getHorizontal() + radiusIncrease,
        computedBorderRadius.getTopRight().getVertical() + radiusIncrease
        ),
      new CornerRadii(
        computedBorderRadius.getBottomLeft().getHorizontal() + radiusIncrease,
        computedBorderRadius.getBottomLeft().getVertical() + radiusIncrease
        ),
      new CornerRadii(
        computedBorderRadius.getBottomRight().getHorizontal() + radiusIncrease,
        computedBorderRadius.getBottomRight().getVertical() + radiusIncrease
        )
    );

    var squirclePath = SquirclePathCalculator.getPath(
      squirclePathBorderRadius,
      shadowRect.width(),
      shadowRect.height(),
      cornerSmoothing
    );
    squirclePath.offset(PixelUtil.toPixelFromDIP(getOffsetX()) - spreadExtent, PixelUtil.toPixelFromDIP(getOffsetY()) - spreadExtent);

    // We inset the clip slightly, to avoid Skia artifacts with antialiased
    // clipping. This inset is only visible when no background is present.
    // https://neugierig.org/software/chromium/notes/2010/07/clipping.html
    var subpixelInsetBounds = new RectF(getBounds());
    subpixelInsetBounds.inset(0.4f, 0.4f);

    var clipPathCornerRadius = new ComputedBorderRadius(
      new CornerRadii(
        computedBorderRadius.getTopLeft().getHorizontal(),
        computedBorderRadius.getTopLeft().getVertical()
      ),
      new CornerRadii(
        computedBorderRadius.getTopRight().getHorizontal(),
        computedBorderRadius.getTopRight().getVertical()
      ),
      new CornerRadii(
        computedBorderRadius.getBottomLeft().getHorizontal(),
        computedBorderRadius.getBottomLeft().getVertical()
      ),
      new CornerRadii(
        computedBorderRadius.getBottomRight().getHorizontal(),
        computedBorderRadius.getBottomRight().getVertical()
      )
    );

    var clipPath = SquirclePathCalculator.getPath(
      clipPathCornerRadius,
      subpixelInsetBounds.width(),
      subpixelInsetBounds.height(),
      cornerSmoothing
    );
    canvas.clipOutPath(clipPath);

    var shadowPaint = getShadowPaint();
    canvas.drawPath(squirclePath, shadowPaint);

    canvas.restoreToCount(saveCount);
  }

  private Context getContext() {
    return (Context) getVariableWithReflection("context", this.base);
  }

  private float getSpread() {
    return (float) getVariableWithReflection("spread", this.base);
  }

  private float getOffsetX() {
    return (float) getVariableWithReflection("offsetX", this.base);
  }

  private float getOffsetY() {
    return (float) getVariableWithReflection("offsetY", this.base);
  }

  private Paint getShadowPaint() {
    return (Paint) getVariableWithReflection("shadowPaint", this.base);
  }

  @Override
  public void setAlpha(int alpha) {
    base.setAlpha(alpha);
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    base.setColorFilter(colorFilter);
    invalidateSelf();
  }

  @Override
  public int getOpacity() {
    return base.getOpacity();
  }

  public static Object getVariableWithReflection(String fieldName, Object obj) {
    try {
      Field field = OutsetBoxShadowDrawable.class.getDeclaredField(fieldName);
      field.setAccessible(true); // Bypass private access

      Object fieldValue = field.get(obj);

      field.setAccessible(false);

      return fieldValue;
    } catch (NoSuchFieldException | IllegalAccessException ignored) {
    }

    return null;
  }

}
