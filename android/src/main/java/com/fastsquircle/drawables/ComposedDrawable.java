package com.fastsquircle.drawables;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Insets;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
public class ComposedDrawable extends Drawable {

  private Drawable base;
  public ComposedDrawable(Drawable base) {
    this.base = base;
    this.setBounds(base.getBounds());
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    base.draw(canvas);
  }

  @Override
  public int getOpacity() {
    return base.getOpacity();
  }

  @Override
  public void setAlpha(int i) {
    base.setAlpha(i);
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    base.setColorFilter(colorFilter);
  }

  @Override
  public void applyTheme(@NonNull Resources.Theme t) {
    base.applyTheme(t);
  }

  @Override
  public boolean canApplyTheme() {
    return base.canApplyTheme();
  }

  @Override
  public void clearColorFilter() {
    base.clearColorFilter();
  }

  @Override
  public int getAlpha() {
    return base.getAlpha();
  }

  @Nullable
  @Override
  public Callback getCallback() {
    return base.getCallback();
  }

  @Override
  public int getChangingConfigurations() {
    return base.getChangingConfigurations();
  }

  @Nullable
  @Override
  public ColorFilter getColorFilter() {
    return base.getColorFilter();
  }

  @Nullable
  @Override
  public ConstantState getConstantState() {
    return base.getConstantState();
  }

  @NonNull
  @Override
  public Drawable getCurrent() {
    return base.getCurrent();
  }

  @NonNull
  @Override
  public Rect getDirtyBounds() {
    return base.getDirtyBounds();
  }

  @Override
  public void getHotspotBounds(@NonNull Rect outRect) {
    base.getHotspotBounds(outRect);
  }

  @Override
  public int getIntrinsicHeight() {
    return base.getIntrinsicHeight();
  }

  @Override
  public int getIntrinsicWidth() {
    return base.getIntrinsicWidth();
  }

  @Override
  public int getLayoutDirection() {
    return base.getLayoutDirection();
  }

  @Override
  public int getMinimumHeight() {
    return base.getMinimumHeight();
  }

  @Override
  public int getMinimumWidth() {
    return base.getMinimumWidth();
  }

  @NonNull
  @Override
  public Insets getOpticalInsets() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      return base.getOpticalInsets();
    }

    throw new RuntimeException("getOpticalInsets called with older version sdk than supported");
  }

  @Override
  public void getOutline(@NonNull Outline outline) {
    base.getOutline(outline);
  }

  @Override
  public boolean getPadding(@NonNull Rect padding) {
    return base.getPadding(padding);
  }

  @NonNull
  @Override
  public int[] getState() {
    return base.getState();
  }

  @Nullable
  @Override
  public Region getTransparentRegion() {
    return base.getTransparentRegion();
  }

  @Override
  public boolean hasFocusStateSpecified() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      return base.hasFocusStateSpecified();
    }

    return false;
  }

  @Override
  public void inflate(@NonNull Resources r, @NonNull XmlPullParser parser, @NonNull AttributeSet attrs) throws IOException, XmlPullParserException {
    base.inflate(r, parser, attrs);
  }

  @Override
  public void inflate(@NonNull Resources r, @NonNull XmlPullParser parser, @NonNull AttributeSet attrs, @Nullable Resources.Theme theme) throws IOException, XmlPullParserException {
    base.inflate(r, parser, attrs, theme);
  }

  @Override
  public void invalidateSelf() {
    base.invalidateSelf();
  }

  @Override
  public boolean isAutoMirrored() {
    return base.isAutoMirrored();
  }

  @Override
  public boolean isFilterBitmap() {
    return base.isFilterBitmap();
  }

  @Override
  public boolean isProjected() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      return base.isProjected();
    }

    return false;
  }

  @Override
  public boolean isStateful() {
    return base.isStateful();
  }

  @Override
  public void jumpToCurrentState() {
    base.jumpToCurrentState();
  }

  @NonNull
  @Override
  public Drawable mutate() {
    return base.mutate();
  }

  @Override
  public boolean onLayoutDirectionChanged(int layoutDirection) {
    return base.onLayoutDirectionChanged(layoutDirection);
  }

  @Override
  public void scheduleSelf(@NonNull Runnable what, long when) {
    base.scheduleSelf(what, when);
  }

  @Override
  public void setAutoMirrored(boolean mirrored) {
    base.setAutoMirrored(mirrored);
  }

  @Override
  public void setBounds(@NonNull Rect bounds) {
    super.setBounds(bounds);
    base.setBounds(bounds);
  }

  @Override
  public void setBounds(int left, int top, int right, int bottom) {
    super.setBounds(left, top, right, bottom);
    base.setBounds(left, top, right, bottom);
  }

  @Override
  public void setChangingConfigurations(int configs) {
    base.setChangingConfigurations(configs);
  }

  @Override
  public void setColorFilter(int color, @NonNull PorterDuff.Mode mode) {
    base.setColorFilter(color, mode);
  }

  @Override
  public void setDither(boolean dither) {
    base.setDither(dither);
  }

  @Override
  public void setFilterBitmap(boolean filter) {
    base.setFilterBitmap(filter);
  }

  @Override
  public void setHotspot(float x, float y) {
    base.setHotspot(x, y);
  }

  @Override
  public void setHotspotBounds(int left, int top, int right, int bottom) {
    base.setHotspotBounds(left, top, right, bottom);
  }

  @Override
  public boolean setState(@NonNull int[] stateSet) {
    return base.setState(stateSet);
  }

  @Override
  public void setTint(int tintColor) {
    base.setTint(tintColor);
  }

  @Override
  public void setTintBlendMode(@Nullable BlendMode blendMode) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      base.setTintBlendMode(blendMode);
    }
  }

  @Override
  public void setTintList(@Nullable ColorStateList tint) {
    base.setTintList(tint);
  }

  @Override
  public void setTintMode(@Nullable PorterDuff.Mode tintMode) {
    base.setTintMode(tintMode);
  }

  @Override
  public boolean setVisible(boolean visible, boolean restart) {
    return base.setVisible(visible, restart);
  }

  @Override
  public void unscheduleSelf(@NonNull Runnable what) {
    base.unscheduleSelf(what);
  }

  protected void updateBase(Drawable base) {
    this.base = base;
    this.setBounds(base.getBounds());
  }

}
