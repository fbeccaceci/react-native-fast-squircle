package com.fastsquircle

import android.graphics.Color
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.viewmanagers.FastSquircleViewManagerInterface
import com.facebook.react.viewmanagers.FastSquircleViewManagerDelegate

@ReactModule(name = FastSquircleViewManager.NAME)
class FastSquircleViewManager : SimpleViewManager<FastSquircleView>(),
  FastSquircleViewManagerInterface<FastSquircleView> {
  private val mDelegate: ViewManagerDelegate<FastSquircleView>

  init {
    mDelegate = FastSquircleViewManagerDelegate(this)
  }

  override fun getDelegate(): ViewManagerDelegate<FastSquircleView>? {
    return mDelegate
  }

  override fun getName(): String {
    return NAME
  }

  public override fun createViewInstance(context: ThemedReactContext): FastSquircleView {
    return FastSquircleView(context)
  }

  @ReactProp(name = "color")
  override fun setColor(view: FastSquircleView?, color: String?) {
    view?.setBackgroundColor(Color.parseColor(color))
  }

  companion object {
    const val NAME = "FastSquircleView"
  }
}
