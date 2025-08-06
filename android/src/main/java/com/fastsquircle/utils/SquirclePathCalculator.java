package com.fastsquircle.utils;


import android.graphics.Path;
import android.graphics.RectF;

import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.style.ComputedBorderRadius;
import com.facebook.react.uimanager.style.CornerRadii;

public class SquirclePathCalculator {
//  private static final float CORNER_SMOOTHING = 0.6f;

  public static Path getPath(ComputedBorderRadius computedBorderRadius, float w, float h, float cornerSmoothing) {
    float minSize = Math.min(w, h);
    float roundingAndSmoothingBudget = minSize / 2f;

    float topLeftRadius = getEffectiveRequestedBorderRadius(computedBorderRadius.getTopLeft(), w, h);
    float topRightRadius = getEffectiveRequestedBorderRadius(computedBorderRadius.getTopRight(), w, h);
    float bottomLeftRadius = getEffectiveRequestedBorderRadius(computedBorderRadius.getBottomLeft(), w, h);
    float bottomRightRadius = getEffectiveRequestedBorderRadius(computedBorderRadius.getBottomRight(), w, h);

    CornerParams topLeft = getPathParamsForCorner(topLeftRadius, roundingAndSmoothingBudget, cornerSmoothing);
    CornerParams topRight = getPathParamsForCorner(topRightRadius, roundingAndSmoothingBudget, cornerSmoothing);
    CornerParams bottomLeft = getPathParamsForCorner(bottomLeftRadius, roundingAndSmoothingBudget, cornerSmoothing);
    CornerParams bottomRight = getPathParamsForCorner(bottomRightRadius, roundingAndSmoothingBudget, cornerSmoothing);

    return getSVGPathFromPathParams(w, h, topLeft, topRight, bottomLeft, bottomRight);
  }

  private static float getEffectiveRequestedBorderRadius(CornerRadii radii, float w, float h) {
    // We use the min size since we don't support different horizontal and vertical radius
    // when the borderRadius is percentage type
    float minRadius = Math.min(radii.getHorizontal(), radii.getVertical());

    float minSize = Math.min(w, h);
    return Math.min(
      PixelUtil.toPixelFromDIP(minRadius),
      minSize / 2f
    );
  }

  private static CornerParams getPathParamsForCorner(float cornerRadius, float budget, float targetCornerSmoothing) {
    float p = (1 + targetCornerSmoothing) * cornerRadius;

    float maxCornerSmoothing = budget / cornerRadius - 1;
    float cornerSmoothing = Math.min(targetCornerSmoothing, maxCornerSmoothing);
    p = Math.min(p, budget);

    float arcMeasure = 90 * (1 - cornerSmoothing);
    float arcSectionLength = (float) (Math.sin(toRadians(arcMeasure / 2)) * cornerRadius * Math.sqrt(2));

    float angleAlpha = (90 - arcMeasure) / 2;
    float p3ToP4Distance = (float) (cornerRadius * Math.tan(toRadians(angleAlpha / 2)));

    float angleBeta = 45 * cornerSmoothing;
    float c = (float) (p3ToP4Distance * Math.cos(toRadians(angleBeta)));
    float d = (float) (c * Math.tan(toRadians(angleBeta)));

    float b = (p - arcSectionLength - c - d) / 3;
    float a = 2 * b;

    return new CornerParams(a, b, c, d, p, arcSectionLength, cornerRadius);
  }

  private static Path getSVGPathFromPathParams(float width, float height,
                                               CornerParams topLeft, CornerParams topRight,
                                               CornerParams bottomLeft, CornerParams bottomRight) {
    Path path = new Path();

    // Helper method inside the function for arc drawing
    class ArcHelper {
      void arcFromTo(float cx, float cy, float R, float x1, float y1, float x2, float y2) {
        double startMath = Math.toDegrees(Math.atan2(cy - y1, x1 - cx));
        double endMath = Math.toDegrees(Math.atan2(cy - y2, x2 - cx));
        float startAngleAndroid = (float) -startMath;
        float sweep = (float) ((startMath - endMath) % 360);
        if (sweep < 0) sweep += 360;
        RectF rect = new RectF(cx - R, cy - R, cx + R, cy + R);
        path.arcTo(rect, startAngleAndroid, sweep, false);
      }
    }

    ArcHelper helper = new ArcHelper();

    // --- TOP-RIGHT CORNER ---
    {
      float R = topRight.cornerRadius;
      float p = topRight.p;
      float a = topRight.a, b = topRight.b, c = topRight.c, d = topRight.d, arc = topRight.arcSectionLength;
      float startX = width - p;
      float startY = 0f;
      path.moveTo(startX, startY);

      if (R > 0f) {
        float cp1X = startX + a;
        float cp1Y = startY;
        float cp2X = startX + a + b;
        float cp2Y = startY;
        float end1X = startX + a + b + c;
        float end1Y = startY + d;
        path.cubicTo(cp1X, cp1Y, cp2X, cp2Y, end1X, end1Y);

        float cx = width - R;
        float cy = R;
        float end2X = end1X + arc;
        float end2Y = end1Y + arc;
        helper.arcFromTo(cx, cy, R, end1X, end1Y, end2X, end2Y);

        float finalX = width;
        float finalY = p;
        float cp3X = end2X + d;
        float cp3Y = end2Y + c;
        float cp4X = end2X + d;
        float cp4Y = end2Y + b + c;
        path.cubicTo(cp3X, cp3Y, cp4X, cp4Y, finalX, finalY);
      } else {
        path.lineTo(width, 0f);
      }
    }

    path.lineTo(width, height - bottomRight.p);

    // --- BOTTOM-RIGHT CORNER ---
    {
      float R = bottomRight.cornerRadius;
      float p = bottomRight.p;
      float a = bottomRight.a, b = bottomRight.b, c = bottomRight.c, d = bottomRight.d, arc = bottomRight.arcSectionLength;
      if (R > 0f) {
        float startX = width;
        float startY = height - p;
        float cp1X = startX;
        float cp1Y = startY + a;
        float cp2X = startX;
        float cp2Y = startY + a + b;
        float end1X = startX - d;
        float end1Y = startY + a + b + c;
        path.cubicTo(cp1X, cp1Y, cp2X, cp2Y, end1X, end1Y);

        float cx = width - R;
        float cy = height - R;
        float end2X = end1X - arc;
        float end2Y = end1Y + arc;
        helper.arcFromTo(cx, cy, R, end1X, end1Y, end2X, end2Y);

        float finalX = width - p;
        float finalY = height;
        float cp3X = end2X - c;
        float cp3Y = end2Y + d;
        float cp4X = end2X - b - c;
        float cp4Y = end2Y + d;
        path.cubicTo(cp3X, cp3Y, cp4X, cp4Y, finalX, finalY);
      } else {
        path.lineTo(width, height);
      }
    }

    path.lineTo(bottomLeft.p, height);

    // --- BOTTOM-LEFT CORNER ---
    {
      float R = bottomLeft.cornerRadius;
      float p = bottomLeft.p;
      float a = bottomLeft.a, b = bottomLeft.b, c = bottomLeft.c, d = bottomLeft.d, arc = bottomLeft.arcSectionLength;
      if (R > 0f) {
        float startX = p;
        float startY = height;
        float cp1X = startX - a;
        float cp1Y = startY;
        float cp2X = startX - a - b;
        float cp2Y = startY;
        float end1X = startX - a - b - c;
        float end1Y = startY - d;
        path.cubicTo(cp1X, cp1Y, cp2X, cp2Y, end1X, end1Y);

        float cx = R;
        float cy = height - R;
        float end2X = end1X - arc;
        float end2Y = end1Y - arc;
        helper.arcFromTo(cx, cy, R, end1X, end1Y, end2X, end2Y);

        float finalX = 0f;
        float finalY = height - p;
        float cp3X = end2X - d;
        float cp3Y = end2Y - c;
        float cp4X = end2X - d;
        float cp4Y = end2Y - b - c;
        path.cubicTo(cp3X, cp3Y, cp4X, cp4Y, finalX, finalY);
      } else {
        path.lineTo(0f, height);
      }
    }

    path.lineTo(0f, topLeft.p);

    // --- TOP-LEFT CORNER ---
    {
      float R = topLeft.cornerRadius;
      float p = topLeft.p;
      float a = topLeft.a, b = topLeft.b, c = topLeft.c, d = topLeft.d, arc = topLeft.arcSectionLength;
      if (R > 0f) {
        float startX = 0f;
        float startY = p;
        float cp1X = startX;
        float cp1Y = startY - a;
        float cp2X = startX;
        float cp2Y = startY - a - b;
        float end1X = startX + d;
        float end1Y = startY - a - b - c;
        path.cubicTo(cp1X, cp1Y, cp2X, cp2Y, end1X, end1Y);

        float cx = R;
        float cy = R;
        float end2X = end1X + arc;
        float end2Y = end1Y - arc;
        helper.arcFromTo(cx, cy, R, end1X, end1Y, end2X, end2Y);

        float finalX = p;
        float finalY = 0f;
        float cp3X = end2X + c;
        float cp3Y = end2Y - d;
        float cp4X = end2X + b + c;
        float cp4Y = end2Y - d;
        path.cubicTo(cp3X, cp3Y, cp4X, cp4Y, finalX, finalY);
      } else {
        path.lineTo(0f, 0f);
      }
    }

    path.close();
    return path;
  }

  private static float toRadians(float degrees) {
    return (float) (degrees * Math.PI / 180f);
  }

  private static class CornerParams {
    public final float a, b, c, d, p, arcSectionLength, cornerRadius;

    public CornerParams(float a, float b, float c, float d, float p, float arcSectionLength, float cornerRadius) {
      this.a = a;
      this.b = b;
      this.c = c;
      this.d = d;
      this.p = p;
      this.arcSectionLength = arcSectionLength;
      this.cornerRadius = cornerRadius;
    }
  }
}
