//
//  FastSquircleBoxShadow_81orMore.m
//  FastSquircle
//
//  Created by Fabrizio Beccaceci on 10/09/25.
//

#if REACT_NATIVE_MINOR_VERSION >= 81

#import "FastSquircleBoxShadow_81orMore.h"
#import <React/RCTConversions.h>
#import "SwiftImport.h"

static CGFloat adjustedCornerRadius(CGFloat cornerRadius, CGFloat spreadDistance)
{
  CGFloat adjustment = spreadDistance;
  (void)adjustment;
  if (cornerRadius < abs(spreadDistance)) {
    const CGFloat r = cornerRadius / (CGFloat)abs(spreadDistance);
    const CGFloat p = (CGFloat)pow(r - 1.0, 3.0);
    adjustment *= 1.0 + p;
  }
  
  return fmax(cornerRadius + adjustment, 0);
}

static RCTCornerRadii cornerRadiiForBoxShadow(RCTCornerRadii cornerRadii, CGFloat spreadDistance)
{
  return {
    adjustedCornerRadius(cornerRadii.topLeftHorizontal, spreadDistance),
    adjustedCornerRadius(cornerRadii.topLeftVertical, spreadDistance),
    adjustedCornerRadius(cornerRadii.topRightHorizontal, spreadDistance),
    adjustedCornerRadius(cornerRadii.topRightVertical, spreadDistance),
    adjustedCornerRadius(cornerRadii.bottomLeftHorizontal, spreadDistance),
    adjustedCornerRadius(cornerRadii.bottomLeftVertical, spreadDistance),
    adjustedCornerRadius(cornerRadii.bottomRightHorizontal, spreadDistance),
    adjustedCornerRadius(cornerRadii.bottomRightVertical, spreadDistance)};
}

static CGColorRef colorRefFromSharedColor(const SharedColor &color)
{
  CGColorRef colorRef = RCTUIColorFromSharedColor(color).CGColor;
  return colorRef ? colorRef : [UIColor blackColor].CGColor;
}

static CALayer *initBoxShadowLayer(const BoxShadow &shadow, CGSize layerSize)
{
  CALayer *shadowLayer = [CALayer layer];
  shadowLayer.frame = CGRectMake(0, 0, layerSize.width, layerSize.height);
  shadowLayer.shadowColor = colorRefFromSharedColor(shadow.color);
  // Default is (0, -3) believe it or not
  shadowLayer.shadowOffset = CGSizeMake(0, 0);
  shadowLayer.shadowOpacity = 1;
  // Apple's blur is not quite what we want and seems to be a bit overbearing
  // with the radius. This is an eyeballed adjustment that has the blur looking
  // more like the web.
  shadowLayer.shadowRadius = shadow.blurRadius / 2;
  shadowLayer.contentsScale = [UIScreen mainScreen].scale;
  
  return shadowLayer;
}

static CALayer *
FastSquircleGetOutsetBoxShadowLayer(const facebook::react::BoxShadow &shadow, RCTCornerRadii cornerRadii, CGSize layerSize, NSNumber *cornerSmoothing)
{
  CALayer *shadowLayer = initBoxShadowLayer(shadow, layerSize);
  
  const RCTCornerInsets shadowRectCornerInsets =
  RCTGetCornerInsets(cornerRadiiForBoxShadow(cornerRadii, shadow.spreadDistance), UIEdgeInsetsZero);
  
  CGRect shadowRect = CGRectInset(shadowLayer.bounds, -shadow.spreadDistance, -shadow.spreadDistance);
  shadowRect = CGRectOffset(shadowRect, shadow.offsetX, shadow.offsetY);
  
  CGFloat width = shadowRect.size.width;
  CGFloat height = shadowRect.size.height;
  
  NSLog(@"width: %f, height: %f", width, height);
  
  SquircleParams *squircleParams = [[SquircleParams alloc] initWithCornerSmoothing:cornerSmoothing width:@(width) height:@(height)];
  squircleParams.topLeftCornerRadius = @(fmax(cornerRadii.topLeftVertical, cornerRadii.topLeftHorizontal));
  squircleParams.topRightCornerRadius = @(fmax(cornerRadii.topRightVertical, cornerRadii.topRightHorizontal));
  squircleParams.bottomLeftCornerRadius = @(fmax(cornerRadii.bottomLeftVertical, cornerRadii.bottomLeftHorizontal));
  squircleParams.bottomRightCornerRadius = @(fmax(cornerRadii.bottomRightVertical, cornerRadii.bottomRightHorizontal));
  
  UIBezierPath *squirclePath = [SquirclePathGenerator getSquirclePath:squircleParams];
  CGAffineTransform translation = CGAffineTransformMakeTranslation(shadowRect.origin.x, shadowRect.origin.y);
  [squirclePath applyTransform:translation];
  
  CGPathRef shadowPath = CGPathCreateCopy(squirclePath.CGPath);
  shadowLayer.shadowPath = shadowPath;

  CAShapeLayer *mask = [CAShapeLayer new];
  [mask setContentsScale:[UIScreen mainScreen].scale];
  CGMutablePathRef path = CGPathCreateMutable();
  
  // Create a large rectangle that covers the entire shadow area
  CGRect maskRect = CGRectInset(shadowRect, -2 * (shadow.blurRadius + 1), -2 * (shadow.blurRadius + 1));
  CGPathAddRect(path, NULL, maskRect);
  
  // Add the squircle path as a hole (using original layer bounds for the squircle)
  squircleParams.width = @(shadowLayer.bounds.size.width);
  squircleParams.height = @(shadowLayer.bounds.size.height);
  UIBezierPath *squircleLayerPath = [SquirclePathGenerator getSquirclePath:squircleParams];
  
  CGPathRef layerPath = CGPathCreateCopy(squircleLayerPath.CGPath);
  CGPathAddPath(path, NULL, layerPath);
  
  // Use even-odd fill rule to create the cutout effect
  mask.fillRule = kCAFillRuleEvenOdd;
  mask.path = path;
  shadowLayer.mask = mask;
  
  CGPathRelease(path);
  CGPathRelease(layerPath);
  CGPathRelease(shadowPath);
  
  return shadowLayer;
}

RCT_EXTERN CALayer *FastSquircleGetBoxShadowLayer(
                                                  const facebook::react::BoxShadow &shadow,
                                                  RCTCornerRadii cornerRadii,
                                                  UIEdgeInsets edgeInsets,
                                                  CGSize layerSize,
                                                  NSNumber *cornerSmoothing) {
  if (shadow.inset) {
    // We don't suppport inset squircle shadow right now
    return RCTGetBoxShadowLayer(shadow, cornerRadii, edgeInsets, layerSize);
  }
  
  return FastSquircleGetOutsetBoxShadowLayer(shadow, cornerRadii, layerSize, cornerSmoothing);
}

#endif
