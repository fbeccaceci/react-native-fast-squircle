package com.fastsquircle;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

public class SquircleBackgroundImageManager {
  public void setCornerSmoothing(float cornerSmoothing) {
  }

  public DrawManager getDrawManager() {
    return new DrawManager();
  }

  public static class DrawManager {
    private DrawManager() {}

    public void checkLayer(Drawable layer, int index) {
    }

    public void beforeDraw(LayerDrawable layerDrawable) {
    }

    public void afterDraw(LayerDrawable layerDrawable) {
    }

  }

}
