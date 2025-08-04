//
//  SquirclePathGenerator.swift
//  FastSquircle
//
//  Created by Fabrizio Beccaceci on 03/08/25.
//
//  Heavily inspired by https://github.com/phamfoo/figma-squircle

import UIKit

struct SquircleParams {
  let cornerRadius: CGFloat?
  let topLeftCornerRadius: CGFloat?
  let topRightCornerRadius: CGFloat?
  let bottomRightCornerRadius: CGFloat?
  let bottomLeftCornerRadius: CGFloat?
  let cornerSmoothing: CGFloat
  let width: CGFloat
  let height: CGFloat
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

func getSquirclePath(params: SquircleParams) -> UIBezierPath {
  let topLeftCornerRadius: CGFloat = params.topLeftCornerRadius ?? params.cornerRadius ?? 0
  let topRightCornerRadius: CGFloat = params.topRightCornerRadius ?? params.cornerRadius ?? 0
  let bottomLeftCornerRadius: CGFloat = params.bottomLeftCornerRadius ?? params.cornerRadius ?? 0
  let bottomRightCornerRadius: CGFloat = params.bottomRightCornerRadius ?? params.cornerRadius ?? 0
  
  let roundingAndSmoothingBudget = min(params.width, params.height) / 2
  
  if (topLeftCornerRadius == topRightCornerRadius
      && topRightCornerRadius == bottomLeftCornerRadius
      && bottomLeftCornerRadius == bottomRightCornerRadius) {
   
    let cornerRadius = min(topLeftCornerRadius, roundingAndSmoothingBudget)
   
    let pathParams = getPathParamsForCorner(params: CornerParams(
      cornerRadius: cornerRadius,
      cornerSmoothing: params.cornerSmoothing,
      roundingAndSmoothingBudget: roundingAndSmoothingBudget
    ))
    
    return getUIBezierPathFromPathParams(
      width: params.width,
      height: params.height,
      topLeftPathParams: pathParams,
      topRightPathParams: pathParams,
      bottomLeftPathParams: pathParams,
      bottomRightPathParams: pathParams
    )
  }
  
  return getUIBezierPathFromPathParams(
    width: params.width,
    height: params.height,
    topLeftPathParams: getPathParamsForCorner(params: CornerParams(
      cornerRadius: min(roundingAndSmoothingBudget, topLeftCornerRadius),
      cornerSmoothing: params.cornerSmoothing,
      roundingAndSmoothingBudget: roundingAndSmoothingBudget
    )),
    topRightPathParams: getPathParamsForCorner(params: CornerParams(
      cornerRadius: min(roundingAndSmoothingBudget, topRightCornerRadius),
      cornerSmoothing: params.cornerSmoothing,
      roundingAndSmoothingBudget: roundingAndSmoothingBudget
    )),
    bottomLeftPathParams: getPathParamsForCorner(params: CornerParams(
      cornerRadius: min(roundingAndSmoothingBudget, bottomLeftCornerRadius),
      cornerSmoothing: params.cornerSmoothing,
      roundingAndSmoothingBudget: roundingAndSmoothingBudget
    )),
    bottomRightPathParams: getPathParamsForCorner(params: CornerParams(
      cornerRadius: min(roundingAndSmoothingBudget, bottomRightCornerRadius),
      cornerSmoothing: params.cornerSmoothing,
      roundingAndSmoothingBudget: roundingAndSmoothingBudget
    )),
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
  bottomRightPathParams: CornerPathParams,
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
