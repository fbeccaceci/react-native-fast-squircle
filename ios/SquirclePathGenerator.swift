//
//  SquirclePathGenerator.swift
//  FastSquircle
//
//  Created by Fabrizio Beccaceci on 03/08/25.
//
//  Heavily inspired by https://github.com/phamfoo/figma-squircle

import UIKit

@objc
public class SquircleParams : NSObject {
  @objc public var cornerRadius: NSNumber?
  @objc public var topLeftCornerRadius: NSNumber?
  @objc public var topRightCornerRadius: NSNumber?
  @objc public var bottomRightCornerRadius: NSNumber?
  @objc public var bottomLeftCornerRadius: NSNumber?
  @objc public var cornerSmoothing: NSNumber
  @objc public var width: NSNumber
  @objc public var height: NSNumber
  
  @objc public init(cornerSmoothing: NSNumber, width: NSNumber, height: NSNumber) {
    self.cornerSmoothing = cornerSmoothing
    self.width = width
    self.height = height
  }
}

struct CornerParams {
  let cornerRadius: CGFloat
  let cornerSmoothing: CGFloat
  let roundingAndSmoothingBudget: CGFloat
}

struct CornerPathParams {
  let a: CGFloat
  let b: CGFloat
  let c: CGFloat
  let d: CGFloat
  let p: CGFloat
  let cornerRadius: CGFloat
  let arcSectionLength: CGFloat
}

@objc public class SquirclePathGenerator : NSObject {
  @objc public static func getSquirclePath(_ params: SquircleParams) -> UIBezierPath {
    return FastSquircle.getSquirclePath(params: params)
  }
}

func getSquirclePath(params: SquircleParams) -> UIBezierPath {
  let topLeftCornerRadius: CGFloat = CGFloat(truncating: params.topLeftCornerRadius ?? params.cornerRadius ?? 0)
  let topRightCornerRadius: CGFloat = CGFloat(truncating: params.topRightCornerRadius ?? params.cornerRadius ?? 0)
  let bottomLeftCornerRadius: CGFloat = CGFloat(truncating: params.bottomLeftCornerRadius ?? params.cornerRadius ?? 0)
  let bottomRightCornerRadius: CGFloat = CGFloat(truncating: params.bottomRightCornerRadius ?? params.cornerRadius ?? 0)
  
  let cornerSmoothing = CGFloat(truncating: params.cornerSmoothing)
  let width = CGFloat(truncating: params.width)
  let height = CGFloat(truncating: params.height)
  
  let roundingAndSmoothingBudget = min(width, height) / 2
  
  
  if (topLeftCornerRadius == topRightCornerRadius
      && topRightCornerRadius == bottomLeftCornerRadius
      && bottomLeftCornerRadius == bottomRightCornerRadius) {
   
    let cornerRadius = min(topLeftCornerRadius, roundingAndSmoothingBudget)
   
    let pathParams = getPathParamsForCorner(params: CornerParams(
      cornerRadius: cornerRadius,
      cornerSmoothing: cornerSmoothing,
      roundingAndSmoothingBudget: roundingAndSmoothingBudget
    ))
    
    return getUIBezierPathFromPathParams(
      width: width,
      height: height,
      topLeftPathParams: pathParams,
      topRightPathParams: pathParams,
      bottomLeftPathParams: pathParams,
      bottomRightPathParams: pathParams
    )
  }
  
  return getUIBezierPathFromPathParams(
    width: width,
    height: height,
    topLeftPathParams: getPathParamsForCorner(params: CornerParams(
      cornerRadius: min(roundingAndSmoothingBudget, topLeftCornerRadius),
      cornerSmoothing: cornerSmoothing,
      roundingAndSmoothingBudget: roundingAndSmoothingBudget
    )),
    topRightPathParams: getPathParamsForCorner(params: CornerParams(
      cornerRadius: min(roundingAndSmoothingBudget, topRightCornerRadius),
      cornerSmoothing: cornerSmoothing,
      roundingAndSmoothingBudget: roundingAndSmoothingBudget
    )),
    bottomLeftPathParams: getPathParamsForCorner(params: CornerParams(
      cornerRadius: min(roundingAndSmoothingBudget, bottomLeftCornerRadius),
      cornerSmoothing: cornerSmoothing,
      roundingAndSmoothingBudget: roundingAndSmoothingBudget
    )),
    bottomRightPathParams: getPathParamsForCorner(params: CornerParams(
      cornerRadius: min(roundingAndSmoothingBudget, bottomRightCornerRadius),
      cornerSmoothing: cornerSmoothing,
      roundingAndSmoothingBudget: roundingAndSmoothingBudget
    ))
  )
}

fileprivate func getPathParamsForCorner(params: CornerParams) -> CornerPathParams {
  let p = min((1 + params.cornerSmoothing) * params.cornerRadius, params.roundingAndSmoothingBudget)
  let maxCornerSmoothing = params.roundingAndSmoothingBudget / params.cornerRadius - 1
  let cornerSmoothing = min(maxCornerSmoothing, params.cornerSmoothing)
  
  let arcMeasure = 90 * (1 - cornerSmoothing)
  let arcSectionLength = sin(toRadians(arcMeasure / 2)) * params.cornerRadius * sqrt(2)
  
  let angleAlpha = (90 - arcMeasure) / 2
  let p3ToP4Distance  = params.cornerRadius * tan(toRadians(angleAlpha / 2))
  
  let angleBeta = 45 * cornerSmoothing
  let c = p3ToP4Distance * cos(toRadians(angleBeta))
  let d = c * tan(toRadians(angleBeta))
  
  let b = (p - arcSectionLength - c - d) / 3
  let a = 2 * b
  
  return CornerPathParams(a: a, b: b, c: c, d: d, p: p, cornerRadius: params.cornerRadius, arcSectionLength: arcSectionLength)
}

fileprivate func toRadians(_ degrees: CGFloat) -> CGFloat {
  return (degrees * .pi) / 180
}

fileprivate func getUIBezierPathFromPathParams(
  width: CGFloat,
  height: CGFloat,
  topLeftPathParams: CornerPathParams,
  topRightPathParams: CornerPathParams,
  bottomLeftPathParams: CornerPathParams,
  bottomRightPathParams: CornerPathParams
) -> UIBezierPath {
  let path = UIBezierPath()
  let containerSize = CGSize(width: width, height: height)
  
  var currentPoint = CGPoint(x: width - topRightPathParams.p, y: 0)
  path.move(to: currentPoint)
  drawTopRightPath(path: path, params: topRightPathParams, currentPoint: currentPoint, containerSize: containerSize)
  
  currentPoint = CGPoint(x: width, y: height - bottomRightPathParams.p)
  path.addLine(to: currentPoint)
  drawBottomRightPath(path: path, params: bottomRightPathParams, currentPoint: currentPoint, containerSize: containerSize)
  
  currentPoint = CGPoint(x: bottomLeftPathParams.p, y: height)
  path.addLine(to: currentPoint)
  drawBottomLeftPath(path: path, params: bottomLeftPathParams, currentPoint: currentPoint, containerSize: containerSize)
  
  currentPoint = CGPoint(x: 0, y: topLeftPathParams.p)
  path.addLine(to: currentPoint)
  drawTopLeftPath(path: path, params: topLeftPathParams, currentPoint: currentPoint, containerSize: containerSize)
  path.close()
  
  return path
}

fileprivate func drawTopRightPath(path: UIBezierPath, params: CornerPathParams, currentPoint: CGPoint, containerSize: CGSize) {
  if params.cornerRadius <= 0 { return }
  
  var curvePoint1 = currentPoint + CGPoint(x: params.a, y: 0)
  var curvePoint2 = currentPoint + CGPoint(x: (params.a + params.b), y: 0)
  var curveDestination = currentPoint + CGPoint(x: params.a + params.b + params.c, y: params.d)
  
  path.addCurve(to: curveDestination, controlPoint1: curvePoint1, controlPoint2: curvePoint2)
  
  let archDestination = curveDestination + CGPoint(x: params.arcSectionLength, y: params.arcSectionLength)
  let center = CGPoint(x: containerSize.width - params.cornerRadius, y: params.cornerRadius)
  
  let startAngle = -atan(abs(center.y - curveDestination.y) / abs(center.x - curveDestination.x))
  let endAngle = -atan(abs(center.y - archDestination.y) / abs(center.x - archDestination.x))
  
  path.addArc(withCenter: center,
              radius: params.cornerRadius,
              startAngle: startAngle,
              endAngle: endAngle,
              clockwise: true)
  
  curvePoint1 = archDestination + CGPoint(x: params.d, y: params.c)
  curvePoint2 = archDestination + CGPoint(x: params.d, y: params.b + params.c)
  curveDestination = archDestination + CGPoint(x: params.d, y: params.a + params.b + params.c)
  
  path.addCurve(to: curveDestination, controlPoint1: curvePoint1, controlPoint2: curvePoint2)
}

fileprivate func drawBottomRightPath(path: UIBezierPath, params: CornerPathParams, currentPoint: CGPoint, containerSize: CGSize) {
  if params.cornerRadius <= 0 { return }
  
  var curvePoint1 = currentPoint + CGPoint(x: 0, y: params.a)
  var curvePoint2 = currentPoint + CGPoint(x: 0, y: params.a + params.b)
  var curveDestination = currentPoint + CGPoint(x: -params.d, y: params.a + params.b + params.c)
  
  path.addCurve(to: curveDestination, controlPoint1: curvePoint1, controlPoint2: curvePoint2)
  
  let archDestination = curveDestination + CGPoint(x: -params.arcSectionLength, y: params.arcSectionLength)
  let center = CGPoint(x: containerSize.width - params.cornerRadius, y: containerSize.height - params.cornerRadius)
  
  let startAngle = atan(abs(center.y - curveDestination.y) / abs(center.x - curveDestination.x))
  let endAngle = atan(abs(center.y - archDestination.y) / abs(center.x - archDestination.x))
  
  path.addArc(withCenter: center,
              radius: params.cornerRadius,
              startAngle: startAngle,
              endAngle: endAngle,
              clockwise: true)
  
  curvePoint1 = archDestination + CGPoint(x: -params.c, y: params.d)
  curvePoint2 = archDestination + CGPoint(x: -(params.b + params.c), y: params.d)
  curveDestination = archDestination + CGPoint(x: -(params.a + params.b + params.c), y: params.d)
  
  path.addCurve(to: curveDestination, controlPoint1: curvePoint1, controlPoint2: curvePoint2)
}

fileprivate func drawBottomLeftPath(path: UIBezierPath, params: CornerPathParams, currentPoint: CGPoint, containerSize: CGSize) {
  if params.cornerRadius <= 0 { return }
  
  var curvePoint1 = currentPoint + CGPoint(x: -params.a, y: 0)
  var curvePoint2 = currentPoint + CGPoint(x: -(params.a + params.b), y: 0)
  var curveDestination = currentPoint + CGPoint(x: -(params.a + params.b + params.c), y: -params.d)
  
  path.addCurve(to: curveDestination, controlPoint1: curvePoint1, controlPoint2: curvePoint2)
  
  let archDestination = curveDestination + CGPoint(x: -params.arcSectionLength, y: -params.arcSectionLength)
  let center = CGPoint(x: params.cornerRadius, y: containerSize.height - params.cornerRadius)
  
  let startAngle = .pi - atan(abs(center.y - curveDestination.y) / abs(center.x - curveDestination.x))
  let endAngle = .pi - atan(abs(center.y - archDestination.y) / abs(center.x - archDestination.x))
  
  path.addArc(withCenter: center,
              radius: params.cornerRadius,
              startAngle: startAngle,
              endAngle: endAngle,
              clockwise: true)
  
  curvePoint1 = archDestination + CGPoint(x: -params.d, y: -params.c)
  curvePoint2 = archDestination + CGPoint(x: -params.d, y: -(params.b + params.c))
  curveDestination = archDestination + CGPoint(x: -params.d, y: -(params.a + params.b + params.c))
  
  path.addCurve(to: curveDestination, controlPoint1: curvePoint1, controlPoint2: curvePoint2)
}

fileprivate func drawTopLeftPath(path: UIBezierPath, params: CornerPathParams, currentPoint: CGPoint, containerSize: CGSize) {
  if params.cornerRadius <= 0 { return }
  
  var curvePoint1 = currentPoint + CGPoint(x: 0, y: -params.a)
  var curvePoint2 = currentPoint + CGPoint(x: 0, y: -(params.a + params.b))
  var curveDestination = currentPoint + CGPoint(x: params.d, y: -(params.a + params.b + params.c))
  
  path.addCurve(to: curveDestination, controlPoint1: curvePoint1, controlPoint2: curvePoint2)
  
  let archDestination = curveDestination + CGPoint(x: params.arcSectionLength, y: -params.arcSectionLength)
  let center = CGPoint(x: params.cornerRadius, y: params.cornerRadius)
  
  let startAngle = .pi + atan(abs(center.y - curveDestination.y) / abs(center.x - curveDestination.x))
  let endAngle = .pi + atan(abs(center.y - archDestination.y) / abs(center.x - archDestination.x))
  
  path.addArc(withCenter: center,
              radius: params.cornerRadius,
              startAngle: startAngle,
              endAngle: endAngle,
              clockwise: true)
  
  curvePoint1 = archDestination + CGPoint(x: params.c, y: -params.d)
  curvePoint2 = archDestination + CGPoint(x: params.b + params.c, y: -params.d)
  curveDestination = archDestination + CGPoint(x: params.a + params.b + params.c, y: -params.d)
  
  path.addCurve(to: curveDestination, controlPoint1: curvePoint1, controlPoint2: curvePoint2)
}

fileprivate extension CGPoint {
    static func + (lhs: CGPoint, rhs: CGPoint) -> CGPoint {
        return CGPoint(x: lhs.x + rhs.x, y: lhs.y + rhs.y)
    }
}
