package com.fastsquircle.drawables;

import static com.facebook.drawee.drawable.DrawableUtils.multiplyColorAlpha;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.LayoutDirection;

import androidx.annotation.NonNull;

import com.facebook.react.modules.i18nmanager.I18nUtil;
import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.drawable.BorderDrawable;
import com.facebook.react.uimanager.style.BorderColors;
import com.facebook.react.uimanager.style.ColorEdges;
import com.facebook.react.uimanager.style.ComputedBorderRadius;
import com.facebook.react.uimanager.style.CornerRadii;
import com.facebook.react.uimanager.style.LogicalEdge;
import com.fastsquircle.utils.SquirclePathCalculator;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

public class SquircleBorderDrawable extends ComposedDrawable {

  private BorderDrawable base;
  private float cornerSmoothing;

  private ColorEdges mComputedBorderColors = new ColorEdges();
  private Path mCenterDrawPath = null;

  public SquircleBorderDrawable(BorderDrawable base, float cornerSmoothing) {
    super(base);

    this.base = base;
    this.cornerSmoothing = cornerSmoothing;
  }

  public void setBase(BorderDrawable base) {
    super.updateBase(base);
    this.base = base;

    invalidateSelf();
  }

  public void setCornerSmoothing(float cornerSmoothing) {
    this.cornerSmoothing = cornerSmoothing;

    invalidateSelf();
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    updatePathEffect();

    var borderColors = getBorderColors();
    mComputedBorderColors = borderColors != null
      ? resolveBorderColors(borderColors, getLayoutDirection(), getContext())
      : getComputedBorderColors();

    var borderRadius = base.getBorderRadius();
    if (borderRadius != null && borderRadius.hasRoundedBorders()) {
      drawRoundedBorders(canvas);
      return;
    }

    base.draw(canvas);
  }

  private void drawRoundedBorders(Canvas canvas) {
    updatePath();
    canvas.save();

    var borderWidth = computeBorderInsets();
    var outerClipPathForBorderRadius = getOuterClipPathForBorderRadius();
    var borderPaint = getBorderPaint();
    var borderAlpha = getBorderAlpha();

    if (borderWidth.top > 0
      || borderWidth.bottom > 0
      || borderWidth.left > 0
      || borderWidth.right > 0) {

      // Clip outer border
      canvas.clipPath(
        Objects.requireNonNull(outerClipPathForBorderRadius), Region.Op.INTERSECT);

      // If it's a full and even border draw inner rect path with stroke
      final float fullBorderWidth = getFullBorderWidth();
      int borderColor = base.getBorderColor(LogicalEdge.ALL);
      if (borderWidth.top == fullBorderWidth
        && borderWidth.bottom == fullBorderWidth
        && borderWidth.left == fullBorderWidth
        && borderWidth.right == fullBorderWidth
        && mComputedBorderColors.getLeft() == borderColor
        && mComputedBorderColors.getTop() == borderColor
        && mComputedBorderColors.getRight() == borderColor
        && mComputedBorderColors.getBottom() == borderColor) {
        if (fullBorderWidth > 0) {
          borderPaint.setColor(multiplyColorAlpha(borderColor, borderAlpha));
          borderPaint.setStyle(Paint.Style.STROKE);
          borderPaint.setStrokeWidth(fullBorderWidth);

          canvas.drawPath(Objects.requireNonNull(mCenterDrawPath), borderPaint);
        }
      }
      // In the case of uneven border widths/colors draw quadrilateral in each direction
//      else {
//        mPaint.setStyle(Paint.Style.FILL);
//
//        // Clip inner border
//        canvas.clipPath(
//          Objects.requireNonNull(mInnerClipPathForBorderRadius), Region.Op.DIFFERENCE);
//
//        final boolean isRTL = getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
//        int colorStart = getBorderColor(Spacing.START);
//        int colorEnd = getBorderColor(Spacing.END);
//
//        if (I18nUtil.getInstance().doLeftAndRightSwapInRTL(mContext)) {
//          if (!isBorderColorDefined(Spacing.START)) {
//            colorStart = colorLeft;
//          }
//
//          if (!isBorderColorDefined(Spacing.END)) {
//            colorEnd = colorRight;
//          }
//
//          final int directionAwareColorLeft = isRTL ? colorEnd : colorStart;
//          final int directionAwareColorRight = isRTL ? colorStart : colorEnd;
//
//          colorLeft = directionAwareColorLeft;
//          colorRight = directionAwareColorRight;
//        } else {
//          final int directionAwareColorLeft = isRTL ? colorEnd : colorStart;
//          final int directionAwareColorRight = isRTL ? colorStart : colorEnd;
//
//          final boolean isColorStartDefined = isBorderColorDefined(Spacing.START);
//          final boolean isColorEndDefined = isBorderColorDefined(Spacing.END);
//          final boolean isDirectionAwareColorLeftDefined =
//            isRTL ? isColorEndDefined : isColorStartDefined;
//          final boolean isDirectionAwareColorRightDefined =
//            isRTL ? isColorStartDefined : isColorEndDefined;
//
//          if (isDirectionAwareColorLeftDefined) {
//            colorLeft = directionAwareColorLeft;
//          }
//
//          if (isDirectionAwareColorRightDefined) {
//            colorRight = directionAwareColorRight;
//          }
//        }
//
//        final RectF outerClipTempRect =
//          Objects.requireNonNull(mOuterClipTempRectForBorderRadius);
//        final float left = outerClipTempRect.left;
//        final float right = outerClipTempRect.right;
//        final float top = outerClipTempRect.top;
//        final float bottom = outerClipTempRect.bottom;
//
//        final PointF innerTopLeftCorner = Objects.requireNonNull(mInnerTopLeftCorner);
//        final PointF innerTopRightCorner = Objects.requireNonNull(mInnerTopRightCorner);
//        final PointF innerBottomLeftCorner = Objects.requireNonNull(mInnerBottomLeftCorner);
//        final PointF innerBottomRightCorner = Objects.requireNonNull(mInnerBottomRightCorner);
//
//        // mGapBetweenPaths is used to close the gap between the diagonal
//        // edges of the quadrilaterals on adjacent sides of the rectangle
//        if (borderWidth.left > 0) {
//          final float x1 = left;
//          final float y1 = top - mGapBetweenPaths;
//          final float x2 = innerTopLeftCorner.x;
//          final float y2 = innerTopLeftCorner.y - mGapBetweenPaths;
//          final float x3 = innerBottomLeftCorner.x;
//          final float y3 = innerBottomLeftCorner.y + mGapBetweenPaths;
//          final float x4 = left;
//          final float y4 = bottom + mGapBetweenPaths;
//
//          drawQuadrilateral(canvas, colorLeft, x1, y1, x2, y2, x3, y3, x4, y4);
//        }
//
//        if (borderWidth.top > 0) {
//          final float x1 = left - mGapBetweenPaths;
//          final float y1 = top;
//          final float x2 = innerTopLeftCorner.x - mGapBetweenPaths;
//          final float y2 = innerTopLeftCorner.y;
//          final float x3 = innerTopRightCorner.x + mGapBetweenPaths;
//          final float y3 = innerTopRightCorner.y;
//          final float x4 = right + mGapBetweenPaths;
//          final float y4 = top;
//
//          drawQuadrilateral(canvas, colorTop, x1, y1, x2, y2, x3, y3, x4, y4);
//        }
//
//        if (borderWidth.right > 0) {
//          final float x1 = right;
//          final float y1 = top - mGapBetweenPaths;
//          final float x2 = innerTopRightCorner.x;
//          final float y2 = innerTopRightCorner.y - mGapBetweenPaths;
//          final float x3 = innerBottomRightCorner.x;
//          final float y3 = innerBottomRightCorner.y + mGapBetweenPaths;
//          final float x4 = right;
//          final float y4 = bottom + mGapBetweenPaths;
//
//          drawQuadrilateral(canvas, colorRight, x1, y1, x2, y2, x3, y3, x4, y4);
//        }
//
//        if (borderWidth.bottom > 0) {
//          final float x1 = left - mGapBetweenPaths;
//          final float y1 = bottom;
//          final float x2 = innerBottomLeftCorner.x - mGapBetweenPaths;
//          final float y2 = innerBottomLeftCorner.y;
//          final float x3 = innerBottomRightCorner.x + mGapBetweenPaths;
//          final float y3 = innerBottomRightCorner.y;
//          final float x4 = right + mGapBetweenPaths;
//          final float y4 = bottom;
//
//          drawQuadrilateral(canvas, colorBottom, x1, y1, x2, y2, x3, y3, x4, y4);
//        }
//      }
    }

    canvas.restore();
  }

  private void updatePathEffect() {
    try {
      Class<?> clazz = base.getClass();
      Method privateMethod = clazz.getDeclaredMethod("updatePathEffect");
      privateMethod.setAccessible(true);

      privateMethod.invoke(base);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void updatePath() {
    var needUpdatePath = getNeedUpdatePath();
    // Call the update on the base object
    try {
      Class<?> clazz = base.getClass();
      Method privateMethod = clazz.getDeclaredMethod("updatePath");
      privateMethod.setAccessible(true);

      privateMethod.invoke(base);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    if (!needUpdatePath) {
      return;
    }

    var tempRectForCenterDrawPath = getTempRectForCenterDrawPath();
    if (tempRectForCenterDrawPath == null) {
      return;
    }

    if (mCenterDrawPath == null) {
      mCenterDrawPath = new Path();
    }
    mCenterDrawPath.reset();

    var computedBorderRadius = getComputedBorderRadius();
    var borderWidth = computeBorderInsets();

    var topLeftRadius = new CornerRadii(0f, 0f);
    var topRightRadius = new CornerRadii(0f, 0f);
    var bottomLeftRadius = new CornerRadii(0f, 0f);
    var bottomRightRadius = new CornerRadii(0f, 0f);

    if (computedBorderRadius != null) {
      topLeftRadius = new CornerRadii(
        PixelUtil.toPixelFromDIP(computedBorderRadius.getTopLeft().getHorizontal()),
        PixelUtil.toPixelFromDIP(computedBorderRadius.getTopLeft().getVertical())
      );

      topRightRadius = new CornerRadii(
        PixelUtil.toPixelFromDIP(computedBorderRadius.getTopRight().getHorizontal()),
        PixelUtil.toPixelFromDIP(computedBorderRadius.getTopRight().getVertical())
      );

      bottomLeftRadius = new CornerRadii(
        PixelUtil.toPixelFromDIP(computedBorderRadius.getBottomLeft().getHorizontal()),
        PixelUtil.toPixelFromDIP(computedBorderRadius.getBottomLeft().getVertical())
      );

      bottomRightRadius = new CornerRadii(
        PixelUtil.toPixelFromDIP(computedBorderRadius.getBottomRight().getHorizontal()),
        PixelUtil.toPixelFromDIP(computedBorderRadius.getBottomRight().getVertical())
      );
    }

    var mCenterDrawPathRadius = new ComputedBorderRadius(
      new CornerRadii(
        topLeftRadius.getHorizontal() > 0
          ? PixelUtil.toDIPFromPixel(topLeftRadius.getHorizontal() - borderWidth.left * 0.5f)
          : 0,
        topLeftRadius.getVertical() > 0
          ? PixelUtil.toDIPFromPixel(topLeftRadius.getVertical() - borderWidth.top * 0.5f)
          : 0
      ),
      new CornerRadii(
        topRightRadius.getHorizontal() > 0
          ? PixelUtil.toDIPFromPixel(topRightRadius.getHorizontal() - borderWidth.right * 0.5f)
          : 0,
        topRightRadius.getVertical() > 0
          ? PixelUtil.toDIPFromPixel(topRightRadius.getVertical() - borderWidth.top * 0.5f)
          : 0
      ),
      new CornerRadii(
        bottomLeftRadius.getHorizontal() > 0
          ? PixelUtil.toDIPFromPixel(bottomLeftRadius.getHorizontal() - borderWidth.left * 0.5f)
          : 0,
        bottomLeftRadius.getVertical() > 0
          ? PixelUtil.toDIPFromPixel(bottomLeftRadius.getVertical() - borderWidth.bottom * 0.5f)
          : 0
      ),
      new CornerRadii(
        bottomRightRadius.getHorizontal() > 0 ?
          PixelUtil.toDIPFromPixel(bottomRightRadius.getHorizontal() - borderWidth.right * 0.5f)
          : 0,
        bottomRightRadius.getVertical() > 0 ?
          PixelUtil.toDIPFromPixel(bottomRightRadius.getVertical() - borderWidth.right * 0.5f)
          : 0
      )
    );
    mCenterDrawPath.set(SquirclePathCalculator.getPath(
      mCenterDrawPathRadius,
      tempRectForCenterDrawPath.width(),
      tempRectForCenterDrawPath.height(),
      this.cornerSmoothing
    ));
    mCenterDrawPath.offset(
      borderWidth.left * 0.5f,
      borderWidth.top * 0.5f
    );
  }

  private RectF computeBorderInsets() {
    try {
      Class<?> clazz = base.getClass();
      Method privateMethod = clazz.getDeclaredMethod("computeBorderInsets");
      privateMethod.setAccessible(true);

      Object result = privateMethod.invoke(base);
      return (RectF) result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Float getFullBorderWidth() {
    try {
      Class<?> clazz = base.getClass();
      Method privateMethod = clazz.getDeclaredMethod("getFullBorderWidth");
      privateMethod.setAccessible(true);

      Object result = privateMethod.invoke(base);
      return (Float) result;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private BorderColors getBorderColors() {
    return (BorderColors) getVariableWithReflection("borderColors");
  }

  private ColorEdges getComputedBorderColors() {
    return (ColorEdges) getVariableWithReflection("computedBorderColors");
  }

  private ComputedBorderRadius getComputedBorderRadius() {
    return (ComputedBorderRadius) getVariableWithReflection("computedBorderRadius");
  }

  private Context getContext() {
    return (Context) getVariableWithReflection("context");
  }

  private Path getOuterClipPathForBorderRadius() {
    return (Path) getVariableWithReflection("outerClipPathForBorderRadius");
  }

  private Paint getBorderPaint() {
    return (Paint) getVariableWithReflection("borderPaint");
  }

  private Integer getBorderAlpha() {
    return (Integer) getVariableWithReflection("borderAlpha");
  }

  private Boolean getNeedUpdatePath() {
    return (Boolean) getVariableWithReflection("needUpdatePath");
  }

  private RectF getTempRectForCenterDrawPath() {
    return (RectF) getVariableWithReflection("tempRectForCenterDrawPath");
  }

  private Object getVariableWithReflection(String fieldName) {
    try {
      Field field = BorderDrawable.class.getDeclaredField(fieldName);
      field.setAccessible(true); // Bypass private access

      Object fieldValue = field.get(base);

      field.setAccessible(false);

      return fieldValue;
    } catch (NoSuchFieldException | IllegalAccessException ignored) {
    }

    return null;
  }

  private ColorEdges resolveBorderColors(BorderColors borderColors, int layoutDirection, Context context) {
    var edgeColors = borderColors.getEdgeColors();

    return switch (layoutDirection) {
      case LayoutDirection.LTR -> new ColorEdges(
        firstNonNull(
          edgeColors[LogicalEdge.START.ordinal()],
          edgeColors[LogicalEdge.LEFT.ordinal()],
          edgeColors[LogicalEdge.HORIZONTAL.ordinal()],
          edgeColors[LogicalEdge.ALL.ordinal()]
        ),
        firstNonNull(
          edgeColors[LogicalEdge.BLOCK_START.ordinal()],
          edgeColors[LogicalEdge.TOP.ordinal()],
          edgeColors[LogicalEdge.BLOCK.ordinal()],
          edgeColors[LogicalEdge.VERTICAL.ordinal()],
          edgeColors[LogicalEdge.ALL.ordinal()]
        ),
        firstNonNull(
          edgeColors[LogicalEdge.END.ordinal()],
          edgeColors[LogicalEdge.RIGHT.ordinal()],
          edgeColors[LogicalEdge.HORIZONTAL.ordinal()],
          edgeColors[LogicalEdge.ALL.ordinal()]
        ),
        firstNonNull(
          edgeColors[LogicalEdge.BLOCK_END.ordinal()],
          edgeColors[LogicalEdge.BOTTOM.ordinal()],
          edgeColors[LogicalEdge.BLOCK.ordinal()],
          edgeColors[LogicalEdge.VERTICAL.ordinal()],
          edgeColors[LogicalEdge.ALL.ordinal()]
        )
      );
      case LayoutDirection.RTL -> I18nUtil.getInstance().doLeftAndRightSwapInRTL(context)
        ? new ColorEdges(
        firstNonNull(
          edgeColors[LogicalEdge.END.ordinal()],
          edgeColors[LogicalEdge.RIGHT.ordinal()],
          edgeColors[LogicalEdge.HORIZONTAL.ordinal()],
          edgeColors[LogicalEdge.ALL.ordinal()]
        ),
        firstNonNull(
          edgeColors[LogicalEdge.BLOCK_START.ordinal()],
          edgeColors[LogicalEdge.TOP.ordinal()],
          edgeColors[LogicalEdge.BLOCK.ordinal()],
          edgeColors[LogicalEdge.VERTICAL.ordinal()],
          edgeColors[LogicalEdge.ALL.ordinal()]
        ),
        firstNonNull(
          edgeColors[LogicalEdge.START.ordinal()],
          edgeColors[LogicalEdge.LEFT.ordinal()],
          edgeColors[LogicalEdge.HORIZONTAL.ordinal()],
          edgeColors[LogicalEdge.ALL.ordinal()]
        ),
        firstNonNull(
          edgeColors[LogicalEdge.BLOCK_END.ordinal()],
          edgeColors[LogicalEdge.BOTTOM.ordinal()],
          edgeColors[LogicalEdge.BLOCK.ordinal()],
          edgeColors[LogicalEdge.VERTICAL.ordinal()],
          edgeColors[LogicalEdge.ALL.ordinal()]
        )
      )
        : new ColorEdges(
        firstNonNull(
          edgeColors[LogicalEdge.END.ordinal()],
          edgeColors[LogicalEdge.LEFT.ordinal()],
          edgeColors[LogicalEdge.HORIZONTAL.ordinal()],
          edgeColors[LogicalEdge.ALL.ordinal()]
        ),
        firstNonNull(
          edgeColors[LogicalEdge.BLOCK_START.ordinal()],
          edgeColors[LogicalEdge.TOP.ordinal()],
          edgeColors[LogicalEdge.BLOCK.ordinal()],
          edgeColors[LogicalEdge.VERTICAL.ordinal()],
          edgeColors[LogicalEdge.ALL.ordinal()]
        ),
        firstNonNull(
          edgeColors[LogicalEdge.START.ordinal()],
          edgeColors[LogicalEdge.RIGHT.ordinal()],
          edgeColors[LogicalEdge.HORIZONTAL.ordinal()],
          edgeColors[LogicalEdge.ALL.ordinal()]
        ),
        firstNonNull(
          edgeColors[LogicalEdge.BLOCK_END.ordinal()],
          edgeColors[LogicalEdge.BOTTOM.ordinal()],
          edgeColors[LogicalEdge.BLOCK.ordinal()],
          edgeColors[LogicalEdge.VERTICAL.ordinal()],
          edgeColors[LogicalEdge.ALL.ordinal()]
        )
      );
      default -> throw new IllegalStateException("Unexpected value: " + layoutDirection);
    };
  }

  private Integer firstNonNull(Integer... args) {
    for (Integer value : args) {
      if (value != null) return value;
    }

    return Color.BLACK;
  }
}
