//
//  ViewLayersUpdater.swift
//  FastSquircle
//
//  Created by Fabrizio Beccaceci on 03/08/25.
//

import UIKit

@objc class UpdateLayersParams : NSObject {
  @objc var view: UIView
  @objc var backgroundColorLayer: CALayer?
  
  @objc var cornerRadius: CGFloat = -1
  @objc var topLeftCornerRadius: CGFloat = -1
  @objc var topRightCornerRadius: CGFloat = -1
  @objc var bottomRightCornerRadius: CGFloat = -1
  @objc var bottomLeftCornerRadius: CGFloat = -1
  
  @objc var cornerSmoothing: CGFloat = 0
  
  @objc init(view: UIView) {
    self.view = view
  }
}

@objc class ViewLayersUpdater : NSObject {
  
  private static let BACKGROUND_COLOR_ZPOSITION: CGFloat = -1024.0;
  
  private var additionalBackgroundColorLayer: CALayer? = nil
  
  @objc func updateLayers(params: UpdateLayersParams) {
    let viewFrame = params.view.frame
    
    let squircleParams = SquircleParams(
      cornerRadius: params.cornerRadius,
      topLeftCornerRadius: ifPresent(params.topLeftCornerRadius),
      topRightCornerRadius: ifPresent(params.topRightCornerRadius),
      bottomRightCornerRadius: ifPresent(params.bottomRightCornerRadius),
      bottomLeftCornerRadius: ifPresent(params.bottomLeftCornerRadius),
      cornerSmoothing: params.cornerSmoothing,
      width: viewFrame.width,
      height: viewFrame.height
    )
    
    let path = getSquirclePath(params: squircleParams)
    
    let maskLayer = CAShapeLayer()
    maskLayer.path = path.cgPath
    
    if let backgroundColorLayer = params.backgroundColorLayer {
      additionalBackgroundColorLayer?.removeFromSuperlayer()
      additionalBackgroundColorLayer = nil
      
      backgroundColorLayer.mask = maskLayer
    } else {
      let originalBackgroundColor = params.view.layer.backgroundColor
      params.view.layer.backgroundColor = nil
      params.view.layer.cornerRadius = 0
      
      if (additionalBackgroundColorLayer == nil) {
        additionalBackgroundColorLayer = CALayer()
        additionalBackgroundColorLayer!.zPosition = ViewLayersUpdater.BACKGROUND_COLOR_ZPOSITION
        params.view.layer.addSublayer(additionalBackgroundColorLayer!)
      }
      
      additionalBackgroundColorLayer!.frame = CGRect(x: 0, y: 0, width: params.view.layer.bounds.size.width, height: params.view.layer.bounds.size.height)
      additionalBackgroundColorLayer!.mask = maskLayer
      additionalBackgroundColorLayer!.backgroundColor = originalBackgroundColor
      additionalBackgroundColorLayer!.removeAllAnimations()
    }
  }
  
  private func ifPresent(_ radius: CGFloat) -> CGFloat? {
    return radius >= 0 ? radius : nil
  }
  
}
