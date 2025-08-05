package com.fastsquircle

import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.viewmanagers.FastSquircleViewManagerInterface
import com.facebook.react.views.view.ReactViewManager

@ReactModule(name = FastSquircleViewManager.NAME)
class FastSquircleViewManager : ReactViewManager(),
  FastSquircleViewManagerInterface<FastSquircleView> {

  override fun getName(): String {
    return NAME
  }

  override fun createViewInstance(context: ThemedReactContext): FastSquircleView {
    return FastSquircleView(context)
  }

  override fun setCornerSmoothing(view: FastSquircleView?, value: Float) {
  }

  companion object {
    const val NAME = "FastSquircleView"
  }
}
