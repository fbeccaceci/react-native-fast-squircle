package com.fastsquircle;

import android.content.Context;
import android.graphics.Canvas;
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
  public void draw(@NonNull Canvas canvas) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
      super.draw(canvas);
      return;
    }
    var originalBackground = getBackground();
    if (!(originalBackground instanceof CompositeBackgroundDrawable compositeBackground)) {
      super.draw(canvas);
      return;
    }

    if (compositeBackground.getOuterShadows().isEmpty()) {
      super.draw(canvas);
      return;
    }

    var enhancedOutsetShadows = compositeBackground.getOuterShadows().stream().map(s -> {
      if (s instanceof OutsetBoxShadowDrawable) return new SquircleOutsetShadowDrawable((OutsetBoxShadowDrawable) s);
      return s;
    }).collect(Collectors.toList());

    var newBackground = compositeBackground.withNewShadows(enhancedOutsetShadows, compositeBackground.getInnerShadows());
    setBackground(newBackground);
    super.draw(canvas);
    setBackground(originalBackground);
  }

}
