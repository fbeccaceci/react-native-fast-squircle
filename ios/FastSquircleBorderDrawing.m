//
//  FastSquircleBorderDrawing.m
//  FastSquircle
//
//  Created by Fabrizio Beccaceci on 04/08/25.
//

#import "FastSquircleBorderDrawing.h"
#import <React/RCTUtils.h>
#import "SwiftImport.h"

static const CGFloat RCTViewBorderThreshold = 0.001;

NS_INLINE BOOL RCTCornerRadiiAreAboveThreshold(RCTCornerRadii cornerRadii)
{
  return (
      cornerRadii.topLeftHorizontal > RCTViewBorderThreshold || cornerRadii.topLeftVertical > RCTViewBorderThreshold ||
      cornerRadii.topRightHorizontal > RCTViewBorderThreshold ||
      cornerRadii.topRightVertical > RCTViewBorderThreshold ||
      cornerRadii.bottomLeftHorizontal > RCTViewBorderThreshold ||
      cornerRadii.bottomLeftVertical > RCTViewBorderThreshold ||
      cornerRadii.bottomRightHorizontal > RCTViewBorderThreshold ||
      cornerRadii.bottomRightVertical > RCTViewBorderThreshold);
}

static UIEdgeInsets RCTRoundInsetsToPixel(UIEdgeInsets edgeInsets)
{
  edgeInsets.top = RCTRoundPixelValue(edgeInsets.top);
  edgeInsets.bottom = RCTRoundPixelValue(edgeInsets.bottom);
  edgeInsets.left = RCTRoundPixelValue(edgeInsets.left);
  edgeInsets.right = RCTRoundPixelValue(edgeInsets.right);

  return edgeInsets;
}

static UIGraphicsImageRenderer *
RCTMakeUIGraphicsImageRenderer(CGSize size, UIColor *backgroundColor, BOOL hasCornerRadii, BOOL drawToEdge)
{
  const CGFloat alpha = CGColorGetAlpha(backgroundColor.CGColor);
  const BOOL opaque = (drawToEdge || !hasCornerRadii) && alpha == 1.0;
  UIGraphicsImageRendererFormat *const rendererFormat = [UIGraphicsImageRendererFormat defaultFormat];
  rendererFormat.opaque = opaque;
  UIGraphicsImageRenderer *const renderer = [[UIGraphicsImageRenderer alloc] initWithSize:size format:rendererFormat];
  return renderer;
}

static CGPathRef RCTPathCreateOuterOutline(BOOL drawToEdge, CGRect rect, RCTCornerRadii cornerRadii, NSNumber *cornerSmoothing)
{
  if (drawToEdge) {
    return CGPathCreateWithRect(rect, NULL);
  }
  
  NSNumber *topLeftBorderRadius = @(fmax(cornerRadii.topLeftVertical, cornerRadii.topLeftHorizontal));
  NSNumber *topRightBorderRadius = @(fmax(cornerRadii.topRightVertical, cornerRadii.topRightHorizontal));
  NSNumber *bottomLeftBorderRadius = @(fmax(cornerRadii.bottomLeftVertical, cornerRadii.bottomLeftHorizontal));
  NSNumber *bottomRightBorderRadius = @(fmax(cornerRadii.bottomRightVertical, cornerRadii.bottomRightHorizontal));
  
  CGFloat width = rect.size.width;
  CGFloat height = rect.size.height;
  SquircleParams *squircleParams = [[SquircleParams alloc] initWithCornerSmoothing:cornerSmoothing width:@(width) height:@(height)];
  squircleParams.topLeftCornerRadius = topLeftBorderRadius;
  squircleParams.topRightCornerRadius = topRightBorderRadius;
  squircleParams.bottomLeftCornerRadius = bottomLeftBorderRadius;
  squircleParams.bottomRightCornerRadius = bottomRightBorderRadius;
  
  UIBezierPath *squirclePath = [SquirclePathGenerator getSquirclePath:squircleParams];

  return CGPathCreateCopy(squirclePath.CGPath);
}

static CGPathRef PathCreateInnerOutline(CGRect rect, RCTCornerRadii cornerRadii, CGFloat insetAmount, NSNumber *cornerSmoothing)
{
  NSNumber *topLeftBorderRadius = @(fmax(fmax(cornerRadii.topLeftVertical, cornerRadii.topLeftHorizontal) - insetAmount, 0));
  NSNumber *topRightBorderRadius = @(fmax(fmax(cornerRadii.topRightVertical, cornerRadii.topRightHorizontal) - insetAmount, 0));
  NSNumber *bottomLeftBorderRadius = @(fmax(fmax(cornerRadii.bottomLeftVertical, cornerRadii.bottomLeftHorizontal) - insetAmount, 0));
  NSNumber *bottomRightBorderRadius = @(fmax(fmax(cornerRadii.bottomRightVertical, cornerRadii.bottomRightHorizontal) - insetAmount, 0));
  
  CGFloat width = rect.size.width;
  CGFloat height = rect.size.height;
  SquircleParams *squircleParams = [[SquircleParams alloc] initWithCornerSmoothing:cornerSmoothing width:@(width) height:@(height)];
  squircleParams.topLeftCornerRadius = topLeftBorderRadius;
  squircleParams.topRightCornerRadius = topRightBorderRadius;
  squircleParams.bottomLeftCornerRadius = bottomLeftBorderRadius;
  squircleParams.bottomRightCornerRadius = bottomRightBorderRadius;
  
  UIBezierPath *squirclePath = [SquirclePathGenerator getSquirclePath:squircleParams];
  
  CGAffineTransform translation = CGAffineTransformMakeTranslation(insetAmount, insetAmount);
  [squirclePath applyTransform:translation];

  return CGPathCreateCopy(squirclePath.CGPath);
}

static void
RCTEllipseGetIntersectionsWithLine(CGRect ellipseBounds, CGPoint lineStart, CGPoint lineEnd, CGPoint intersections[2])
{
  const CGPoint ellipseCenter = {CGRectGetMidX(ellipseBounds), CGRectGetMidY(ellipseBounds)};

  lineStart.x -= ellipseCenter.x;
  lineStart.y -= ellipseCenter.y;
  lineEnd.x -= ellipseCenter.x;
  lineEnd.y -= ellipseCenter.y;

  const CGFloat m = (lineEnd.y - lineStart.y) / (lineEnd.x - lineStart.x);
  const CGFloat a = ellipseBounds.size.width / 2;
  const CGFloat b = ellipseBounds.size.height / 2;
  const CGFloat c = lineStart.y - m * lineStart.x;
  const CGFloat A = (b * b + a * a * m * m);
  const CGFloat B = 2 * a * a * c * m;
  const CGFloat D = sqrt((a * a * (b * b - c * c)) / A + pow(B / (2 * A), 2));

  const CGFloat x_ = -B / (2 * A);
  const CGFloat x1 = x_ + D;
  const CGFloat x2 = x_ - D;
  const CGFloat y1 = m * x1 + c;
  const CGFloat y2 = m * x2 + c;

  intersections[0] = (CGPoint){x1 + ellipseCenter.x, y1 + ellipseCenter.y};
  intersections[1] = (CGPoint){x2 + ellipseCenter.x, y2 + ellipseCenter.y};
}


static UIImage *RCTGetSolidBorderImage(
    RCTCornerRadii cornerRadii,
    CGSize viewSize,
    UIEdgeInsets borderInsets,
    RCTBorderColors borderColors,
    UIColor *backgroundColor,
    BOOL drawToEdge,
    NSNumber *cornerSmoothing)
{
  const BOOL hasCornerRadii = RCTCornerRadiiAreAboveThreshold(cornerRadii);
  const RCTCornerInsets cornerInsets = RCTGetCornerInsets(cornerRadii, borderInsets);

  // Incorrect render for borders that are not proportional to device pixel: borders get stretched and become
  // significantly bigger than expected.
  // Rdar: http://www.openradar.me/15959788
  borderInsets = RCTRoundInsetsToPixel(borderInsets);

  const BOOL makeStretchable =
      (borderInsets.left + cornerInsets.topLeft.width + borderInsets.right + cornerInsets.bottomRight.width <=
       viewSize.width) &&
      (borderInsets.left + cornerInsets.bottomLeft.width + borderInsets.right + cornerInsets.topRight.width <=
       viewSize.width) &&
      (borderInsets.top + cornerInsets.topLeft.height + borderInsets.bottom + cornerInsets.bottomRight.height <=
       viewSize.height) &&
      (borderInsets.top + cornerInsets.topRight.height + borderInsets.bottom + cornerInsets.bottomLeft.height <=
       viewSize.height);

  UIEdgeInsets edgeInsets = (UIEdgeInsets){
      borderInsets.top + MAX(cornerInsets.topLeft.height, cornerInsets.topRight.height),
      borderInsets.left + MAX(cornerInsets.topLeft.width, cornerInsets.bottomLeft.width),
      borderInsets.bottom + MAX(cornerInsets.bottomLeft.height, cornerInsets.bottomRight.height),
      borderInsets.right + MAX(cornerInsets.bottomRight.width, cornerInsets.topRight.width)};
  
  const CGSize size = viewSize;

  UIGraphicsImageRenderer *const imageRenderer =
      RCTMakeUIGraphicsImageRenderer(size, backgroundColor, hasCornerRadii, drawToEdge);

  UIImage *image = [imageRenderer imageWithActions:^(UIGraphicsImageRendererContext *_Nonnull rendererContext) {
    const CGContextRef context = rendererContext.CGContext;
    const CGRect rect = {.size = size};
    CGPathRef path = RCTPathCreateOuterOutline(drawToEdge, rect, cornerRadii, cornerSmoothing);

    if (backgroundColor) {
      CGContextSetFillColorWithColor(context, backgroundColor.CGColor);
      CGContextAddPath(context, path);
      CGContextFillPath(context);
    }

    CGContextAddPath(context, path);
    CGPathRelease(path);

    CGRect adjustedRect = UIEdgeInsetsInsetRect(rect, borderInsets);
    CGPathRef insetPath = PathCreateInnerOutline(adjustedRect, cornerRadii, borderInsets.left, cornerSmoothing);

    CGContextAddPath(context, insetPath);
    CGContextEOClip(context);

    BOOL hasEqualColors = RCTBorderColorsAreEqual(borderColors);
    if ((drawToEdge || !hasCornerRadii) && hasEqualColors) {
      CGContextSetFillColorWithColor(context, borderColors.left.CGColor);
      CGContextAddRect(context, rect);
      CGContextAddPath(context, insetPath);
      CGContextEOFillPath(context);

    } else {
      CGPoint topLeft = (CGPoint){borderInsets.left, borderInsets.top};
      if (cornerInsets.topLeft.width > 0 && cornerInsets.topLeft.height > 0) {
        CGPoint points[2];
        RCTEllipseGetIntersectionsWithLine(
            (CGRect){topLeft, {2 * cornerInsets.topLeft.width, 2 * cornerInsets.topLeft.height}},
            CGPointZero,
            topLeft,
            points);
        if (!isnan(points[1].x) && !isnan(points[1].y)) {
          topLeft = points[1];
        }
      }

      CGPoint bottomLeft = (CGPoint){borderInsets.left, size.height - borderInsets.bottom};
      if (cornerInsets.bottomLeft.width > 0 && cornerInsets.bottomLeft.height > 0) {
        CGPoint points[2];
        RCTEllipseGetIntersectionsWithLine(
            (CGRect){
                {bottomLeft.x, bottomLeft.y - 2 * cornerInsets.bottomLeft.height},
                {2 * cornerInsets.bottomLeft.width, 2 * cornerInsets.bottomLeft.height}},
            (CGPoint){0, size.height},
            bottomLeft,
            points);
        if (!isnan(points[1].x) && !isnan(points[1].y)) {
          bottomLeft = points[1];
        }
      }

      CGPoint topRight = (CGPoint){size.width - borderInsets.right, borderInsets.top};
      if (cornerInsets.topRight.width > 0 && cornerInsets.topRight.height > 0) {
        CGPoint points[2];
        RCTEllipseGetIntersectionsWithLine(
            (CGRect){
                {topRight.x - 2 * cornerInsets.topRight.width, topRight.y},
                {2 * cornerInsets.topRight.width, 2 * cornerInsets.topRight.height}},
            (CGPoint){size.width, 0},
            topRight,
            points);
        if (!isnan(points[0].x) && !isnan(points[0].y)) {
          topRight = points[0];
        }
      }

      CGPoint bottomRight = (CGPoint){size.width - borderInsets.right, size.height - borderInsets.bottom};
      if (cornerInsets.bottomRight.width > 0 && cornerInsets.bottomRight.height > 0) {
        CGPoint points[2];
        RCTEllipseGetIntersectionsWithLine(
            (CGRect){
                {bottomRight.x - 2 * cornerInsets.bottomRight.width,
                 bottomRight.y - 2 * cornerInsets.bottomRight.height},
                {2 * cornerInsets.bottomRight.width, 2 * cornerInsets.bottomRight.height}},
            (CGPoint){size.width, size.height},
            bottomRight,
            points);
        if (!isnan(points[0].x) && !isnan(points[0].y)) {
          bottomRight = points[0];
        }
      }

      UIColor *currentColor = nil;

      // RIGHT
      if (borderInsets.right > 0) {
        const CGPoint points[] = {
            (CGPoint){size.width, 0},
            topRight,
            bottomRight,
            (CGPoint){size.width, size.height},
        };

        currentColor = borderColors.right;
        CGContextAddLines(context, points, sizeof(points) / sizeof(*points));
      }

      // BOTTOM
      if (borderInsets.bottom > 0) {
        const CGPoint points[] = {
            (CGPoint){0, size.height},
            bottomLeft,
            bottomRight,
            (CGPoint){size.width, size.height},
        };

        if (!CGColorEqualToColor(currentColor.CGColor, borderColors.bottom.CGColor)) {
          CGContextSetFillColorWithColor(context, currentColor.CGColor);
          CGContextFillPath(context);
          currentColor = borderColors.bottom;
        }
        CGContextAddLines(context, points, sizeof(points) / sizeof(*points));
      }

      // LEFT
      if (borderInsets.left > 0) {
        const CGPoint points[] = {
            CGPointZero,
            topLeft,
            bottomLeft,
            (CGPoint){0, size.height},
        };

        if (!CGColorEqualToColor(currentColor.CGColor, borderColors.left.CGColor)) {
          CGContextSetFillColorWithColor(context, currentColor.CGColor);
          CGContextFillPath(context);
          currentColor = borderColors.left;
        }
        CGContextAddLines(context, points, sizeof(points) / sizeof(*points));
      }

      // TOP
      if (borderInsets.top > 0) {
        const CGPoint points[] = {
            CGPointZero,
            topLeft,
            topRight,
            (CGPoint){size.width, 0},
        };

        if (!CGColorEqualToColor(currentColor.CGColor, borderColors.top.CGColor)) {
          CGContextSetFillColorWithColor(context, currentColor.CGColor);
          CGContextFillPath(context);
          currentColor = borderColors.top;
        }
        CGContextAddLines(context, points, sizeof(points) / sizeof(*points));
      }

      CGContextSetFillColorWithColor(context, currentColor.CGColor);
      CGContextFillPath(context);
    }

    CGPathRelease(insetPath);
  }];

  if (makeStretchable) {
    image = [image resizableImageWithCapInsets:edgeInsets];
  }

  return image;
}

UIImage *FastSquircleGetBorderImage(
  RCTBorderStyle borderStyle,
  CGSize viewSize,
  RCTCornerRadii cornerRadii,
  UIEdgeInsets borderInsets,
  RCTBorderColors borderColors,
  UIColor *backgroundColor,
  BOOL drawToEdge,
  NSNumber *cornerSmoothing)
{
  switch (borderStyle) {
    case RCTBorderStyleSolid:
      return RCTGetSolidBorderImage(cornerRadii, viewSize, borderInsets, borderColors, backgroundColor, drawToEdge, cornerSmoothing);
//    case RCTBorderStyleDashed:
//    case RCTBorderStyleDotted:
//      return RCTGetDashedOrDottedBorderImage(
//          borderStyle, cornerRadii, viewSize, borderInsets, borderColors, backgroundColor, drawToEdge);
    case RCTBorderStyleUnset:
      break;
  }

  return nil;
}
