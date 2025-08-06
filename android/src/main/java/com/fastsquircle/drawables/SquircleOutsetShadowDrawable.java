package com.fastsquircle.drawables;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.drawable.OutsetBoxShadowDrawable;
import com.fastsquircle.utils.SquirclePathCalculator;

import java.lang.reflect.Field;

@RequiresApi(api = Build.VERSION_CODES.P)
public class SquircleOutsetShadowDrawable extends Drawable {

  private final OutsetBoxShadowDrawable base;

  public SquircleOutsetShadowDrawable(OutsetBoxShadowDrawable base) {
    this.base = base;

    var color = getShadowPaint().getColor();

    int r = Color.red(color);
    int g = Color.green(color);
    int b = Color.blue(color);

    System.out.println(String.format("Creating SquircleOutsetShadowDrawable with color: (%d, %d, %d)", r, g, b));
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    var spreadExtent = PixelUtil.toPixelFromDIP(getSpread());
    var shadowRect = new RectF(getBounds());
    shadowRect.inset(-spreadExtent, -spreadExtent);
    shadowRect.offset(PixelUtil.toPixelFromDIP(getOffsetX()), PixelUtil.toPixelFromDIP(getOffsetY()));

    var saveCount = canvas.save();

    var computedBorderRadius = base.getBorderRadius().resolve(
      getLayoutDirection(),
      getContext(),
      getBounds().width(),
      getBounds().height()
    );

    var squirclePath = SquirclePathCalculator.getPath(
      computedBorderRadius,
      shadowRect.width(),
      shadowRect.height()
    );
    squirclePath.offset(PixelUtil.toPixelFromDIP(getOffsetX()), PixelUtil.toPixelFromDIP(getOffsetY()));

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
