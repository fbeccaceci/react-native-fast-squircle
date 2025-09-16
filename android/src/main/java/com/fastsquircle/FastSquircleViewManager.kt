package com.fastsquircle

import com.facebook.react.bridge.ReadableArray
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.viewmanagers.FastSquircleViewManagerDelegate
import com.facebook.react.viewmanagers.FastSquircleViewManagerInterface
import com.facebook.react.views.view.ReactViewGroup
import com.facebook.react.views.view.ReactViewManager

@ReactModule(name = FastSquircleViewManager.NAME)
class FastSquircleViewManager : ReactViewManager(),
  FastSquircleViewManagerInterface<FastSquircleView> {

  private val delegate: ViewManagerDelegate<ReactViewGroup>

  init {
    val specificDelegate = FastSquircleViewManagerDelegate(ViewManagerWrapper(this))
    delegate = SplitDelegate(super.getDelegate(), specificDelegate)
  }

  override fun getName(): String {
    return NAME
  }

  override fun createViewInstance(context: ThemedReactContext): FastSquircleView {
    return FastSquircleView(context)
  }

  override fun setCornerSmoothing(view: FastSquircleView?, value: Float) {
    view?.setCornerSmoothing(value);
  }

  override fun getDelegate(): ViewManagerDelegate<ReactViewGroup> {
    return delegate
  }

  companion object {
    const val NAME = "FastSquircleView"
  }

}

class ViewManagerWrapper(private val baseVm: FastSquircleViewManager) :
  SimpleViewManager<FastSquircleView>(), FastSquircleViewManagerInterface<FastSquircleView> {

  override fun createViewInstance(reactContext: ThemedReactContext): FastSquircleView {
    return baseVm.createViewInstance(reactContext)
  }

  override fun getName(): String {
    return baseVm.name
  }

  override fun setCornerSmoothing(view: FastSquircleView?, value: Float) {
    baseVm.setCornerSmoothing(view, value)
  }

}

class SplitDelegate(
  private val baseDelegate: ViewManagerDelegate<ReactViewGroup>,
  private val specificDelegate: ViewManagerDelegate<FastSquircleView>
) : ViewManagerDelegate<ReactViewGroup> {

  override fun setProperty(view: ReactViewGroup, propName: String, value: Any?) {
    baseDelegate.setProperty(view, propName, value)

    // For some reason i cannot understand handling the outlineColor in the specificDelegate causes
    // a crash so we avoid that, it will still be handles by baseDelegate so should not be a problem.
    if (propName == "outlineColor") return

    if (view is FastSquircleView)
      specificDelegate.setProperty(view, propName, value)
  }

  override fun receiveCommand(view: ReactViewGroup, commandName: String, args: ReadableArray) {
    baseDelegate.receiveCommand(view, commandName, args)

    if (view is FastSquircleView)
      specificDelegate.setProperty(view, commandName, args)
  }

}
