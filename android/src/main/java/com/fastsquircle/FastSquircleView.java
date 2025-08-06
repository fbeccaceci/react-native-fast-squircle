package com.fastsquircle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;

import com.facebook.react.common.annotations.UnstableReactNativeAPI;
import com.facebook.react.uimanager.drawable.CompositeBackgroundDrawable;
import com.facebook.react.uimanager.drawable.OutsetBoxShadowDrawable;
import com.facebook.react.views.view.ReactViewGroup;
import com.fastsquircle.drawables.SquircleCSSBackgroundDrawable;
import com.fastsquircle.drawables.SquircleOutsetShadowDrawable;

import java.util.Collections;
import java.util.stream.Collectors;

public class FastSquircleView extends ReactViewGroup {

  @OptIn(markerClass = UnstableReactNativeAPI.class)
  public FastSquircleView(@Nullable Context context) {
    super(context);

    setBackground(new CompositeBackgroundDrawable(
      getContext(),
      getBackground(),
      Collections.emptyList(),
      new SquircleCSSBackgroundDrawable(getContext()),
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
    System.out.println("Running inside the draw method");

    super.draw(canvas);
  }
}
