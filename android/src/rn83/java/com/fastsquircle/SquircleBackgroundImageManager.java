package com.fastsquircle;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

import com.facebook.react.uimanager.drawable.BackgroundImageDrawable;
import com.fastsquircle.drawables.SquircleBackgroundImageDrawable;

public class SquircleBackgroundImageManager {

  private float cornerSmoothing = 0.0f;
  private SquircleBackgroundImageDrawable squircleBackgroundImageDrawable;

  public void setCornerSmoothing(float cornerSmoothing) {
    this.cornerSmoothing = cornerSmoothing;

    if (this.squircleBackgroundImageDrawable != null) {
      this.squircleBackgroundImageDrawable.setCornerSmoothing(cornerSmoothing);
    }
  }

  public DrawManager getDrawManager() {
    return new DrawManager();
  }

  public class DrawManager {
    private int backgroundImageDrawableIndex = -1;
    private BackgroundImageDrawable backgroundImageDrawable = null;

    private DrawManager() {}

    public void checkLayer(Drawable layer, int index) {
      if (backgroundImageDrawableIndex < 0 && layer instanceof BackgroundImageDrawable) {
        backgroundImageDrawableIndex = index;
      }
    }

    public void beforeDraw(LayerDrawable layerDrawable) {
      if (backgroundImageDrawableIndex >= 0) {
        backgroundImageDrawable = (BackgroundImageDrawable) layerDrawable.getDrawable(backgroundImageDrawableIndex);
        if (squircleBackgroundImageDrawable == null) {
          squircleBackgroundImageDrawable = new SquircleBackgroundImageDrawable(backgroundImageDrawable, cornerSmoothing);
        } else {
          squircleBackgroundImageDrawable.setBase(backgroundImageDrawable);
        }
      }

      if (backgroundImageDrawableIndex >= 0) layerDrawable.setDrawable(backgroundImageDrawableIndex, squircleBackgroundImageDrawable);
    }

    public void afterDraw(LayerDrawable layerDrawable) {
      if (backgroundImageDrawableIndex >= 0) layerDrawable.setDrawable(backgroundImageDrawableIndex, backgroundImageDrawable);
    }

  }

}
