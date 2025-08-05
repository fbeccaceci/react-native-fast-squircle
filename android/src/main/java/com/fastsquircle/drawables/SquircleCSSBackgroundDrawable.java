package com.fastsquircle.drawables;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.DashPathEffect;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathEffect;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.Shader;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.facebook.react.common.annotations.UnstableReactNativeAPI;
import com.facebook.react.modules.i18nmanager.I18nUtil;
import com.facebook.react.uimanager.FloatUtil;
import com.facebook.react.uimanager.LengthPercentage;
import com.facebook.react.uimanager.PixelUtil;
import com.facebook.react.uimanager.Spacing;
import com.facebook.react.uimanager.drawable.CSSBackgroundDrawable;
import com.facebook.react.uimanager.style.BackgroundImageLayer;
import com.facebook.react.uimanager.style.BorderRadiusProp;
import com.facebook.react.uimanager.style.BorderRadiusStyle;
import com.facebook.react.uimanager.style.BorderStyle;
import com.facebook.react.uimanager.style.ComputedBorderRadius;
import com.facebook.react.uimanager.style.CornerRadii;
import com.fastsquircle.utils.SquirclePathCalculator;

import java.util.List;
import java.util.Objects;

@UnstableReactNativeAPI
public class SquircleCSSBackgroundDrawable extends CSSBackgroundDrawable {

  private static final int DEFAULT_BORDER_COLOR = Color.BLACK;
  private static final int DEFAULT_BORDER_RGB = 0x00FFFFFF & DEFAULT_BORDER_COLOR;
  private static final int DEFAULT_BORDER_ALPHA = (0xFF000000 & DEFAULT_BORDER_COLOR) >>> 24;
  // ~0 == 0xFFFFFFFF, all bits set to 1.
  private static final int ALL_BITS_SET = ~0;
  // 0 == 0x00000000, all bits set to 0.
  private static final int ALL_BITS_UNSET = 0;

  private static @Nullable PathEffect getPathEffect(BorderStyle style, float borderWidth) {
    switch (style) {
      case SOLID:
        return null;

      case DASHED:
        return new DashPathEffect(
          new float[] {borderWidth * 3, borderWidth * 3, borderWidth * 3, borderWidth * 3}, 0);

      case DOTTED:
        return new DashPathEffect(
          new float[] {borderWidth, borderWidth, borderWidth, borderWidth}, 0);

      default:
        return null;
    }
  }

  /* Value at Spacing.ALL index used for rounded borders, whole array used by rectangular borders */
  private @Nullable Spacing mBorderWidth;
  private @Nullable Spacing mBorderRGB;
  private @Nullable Spacing mBorderAlpha;
  private @Nullable BorderStyle mBorderStyle;

  private @Nullable Path mInnerClipPathForBorderRadius;
  private @Nullable Path mBackgroundColorRenderPath;
  private @Nullable Path mOuterClipPathForBorderRadius;
  private @Nullable Path mPathForBorderRadiusOutline;
  private @Nullable Path mPathForBorder;
  private final Path mPathForSingleBorder = new Path();
  private @Nullable Path mCenterDrawPath;
  private @Nullable RectF mInnerClipTempRectForBorderRadius;
  private @Nullable RectF mOuterClipTempRectForBorderRadius;
  private @Nullable RectF mTempRectForBorderRadiusOutline;
  private @Nullable RectF mTempRectForCenterDrawPath;
  private @Nullable PointF mInnerTopLeftCorner;
  private @Nullable PointF mInnerTopRightCorner;
  private @Nullable PointF mInnerBottomRightCorner;
  private @Nullable PointF mInnerBottomLeftCorner;
  private boolean mNeedUpdatePathForBorderRadius = false;

  /* Used by all types of background and for drawing borders */
  private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
  private int mColor = Color.TRANSPARENT;
  private @Nullable List<BackgroundImageLayer> mBackgroundImageLayers = null;
  private int mAlpha = 255;

  // There is a small gap between the edges of adjacent paths
  // such as between the mBackgroundColorRenderPath and its border.
  // The smallest amount (found to be 0.8f) is used to extend
  // the paths, overlapping them and closing the visible gap.
  private final float mGapBetweenPaths = 0.8f;

  private BorderRadiusStyle mBorderRadius = new BorderRadiusStyle();
  private ComputedBorderRadius mComputedBorderRadius = new ComputedBorderRadius();
  private final Context mContext;

  // Should be removed after migrating to Android layout direction.
  private int mLayoutDirectionOverride = -1;

  public SquircleCSSBackgroundDrawable(Context context) {
    super(context);
    mContext = context;
  }

  @Override
  public void draw(Canvas canvas) {
    updatePathEffect();
    if (!hasRoundedBorders()) {
      super.draw(canvas);
    } else {
      drawSquircleBackgroundWithBorders(canvas);
    }
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    super.onBoundsChange(bounds);
    mNeedUpdatePathForBorderRadius = true;
  }

  @Override
  public void setAlpha(int alpha) {
    if (alpha != mAlpha) {
      mAlpha = alpha;
    }

    super.setAlpha(alpha);
  }

  @Deprecated
  public void setLayoutDirectionOverride(int layoutDirection) {
    if (mLayoutDirectionOverride != layoutDirection) {
      mLayoutDirectionOverride = layoutDirection;
    }

    super.setLayoutDirectionOverride(layoutDirection);
  }

  /* Android's elevation implementation requires this to be implemented to know where to draw the  . */
  @Override
  public void getOutline(Outline outline) {
    if (hasRoundedBorders()) {
      updatePath();

      outline.setConvexPath(Objects.requireNonNull(mPathForBorderRadiusOutline));
    } else {
      outline.setRect(getBounds());
    }
  }

  public void setBorderWidth(int position, float width) {
    if (mBorderWidth == null) {
      mBorderWidth = new Spacing();
    }
    if (!FloatUtil.floatsEqual(mBorderWidth.getRaw(position), width)) {
      mBorderWidth.set(position, width);
      switch (position) {
        case Spacing.ALL:
        case Spacing.LEFT:
        case Spacing.BOTTOM:
        case Spacing.RIGHT:
        case Spacing.TOP:
        case Spacing.START:
        case Spacing.END:
          mNeedUpdatePathForBorderRadius = true;
      }
    }

    super.setBorderWidth(position, width);
  }

  public void setBorderColor(int position, @Nullable Integer color) {
    float rgbComponent = color == null ? Float.NaN : (float) ((int) color & 0x00FFFFFF);
    float alphaComponent = color == null ? Float.NaN : (float) ((int) color >>> 24);

    this.setBorderRGB(position, rgbComponent);
    this.setBorderAlpha(position, alphaComponent);
    mNeedUpdatePathForBorderRadius = true;

    super.setBorderColor(position, color);
  }

  private void setBorderRGB(int position, float rgb) {
    // set RGB component
    if (mBorderRGB == null) {
      mBorderRGB = new Spacing(DEFAULT_BORDER_RGB);
    }
    if (!FloatUtil.floatsEqual(mBorderRGB.getRaw(position), rgb)) {
      mBorderRGB.set(position, rgb);
      invalidateSelf();
    }
  }

  private void setBorderAlpha(int position, float alpha) {
    // set Alpha component
    if (mBorderAlpha == null) {
      mBorderAlpha = new Spacing(DEFAULT_BORDER_ALPHA);
    }
    if (!FloatUtil.floatsEqual(mBorderAlpha.getRaw(position), alpha)) {
      mBorderAlpha.set(position, alpha);
      invalidateSelf();
    }
  }


  public void setBorderStyle(@Nullable BorderStyle borderStyle) {
    if (mBorderStyle != borderStyle) {
      mBorderStyle = borderStyle;
      mNeedUpdatePathForBorderRadius = true;
    }

    super.setBorderStyle(borderStyle);
  }


  /**
   * @deprecated Use {@link #setBorderRadius(BorderRadiusProp, LengthPercentage)} instead.
   * @noinspection removal
   */
  @Deprecated(since = "0.75.0", forRemoval = true)
  public void setRadius(float radius, int position) {
    @Nullable Float boxedRadius = Float.isNaN(radius) ? null : Float.valueOf(radius);

    if (boxedRadius == null) {
      mBorderRadius.set(BorderRadiusProp.values()[position], null);
    }

    super.setRadius(radius, position);
  }

  public void setBorderRadius(BorderRadiusProp property, @Nullable LengthPercentage radius) {
    if (!Objects.equals(radius, mBorderRadius.get(property))) {
      mBorderRadius.set(property, radius);
      mNeedUpdatePathForBorderRadius = true;
    }

    super.setBorderRadius(property, radius);
  }

  public void setBorderRadius(BorderRadiusStyle radius) {
    mBorderRadius = radius;
    super.setBorderRadius(radius);
  }

  public void setColor(int color) {
    mColor = color;
    super.setColor(color);
  }

  public void setBackgroundImage(@Nullable List<BackgroundImageLayer> backgroundImageLayers) {
    mBackgroundImageLayers = backgroundImageLayers;
    super.setBackgroundImage(backgroundImageLayers);
  }

  public @Nullable Path getBorderBoxPath() {
    if (hasRoundedBorders()) {
      updatePath();
      return super.getBorderBoxPath();
    }

    return null;
  }

  public @Nullable Path getPaddingBoxPath() {
    if (hasRoundedBorders()) {
      updatePath();
      return super.getBorderBoxPath();
    }

    return null;
  }

  private void updatePath() {
    if (!mNeedUpdatePathForBorderRadius) {
      return;
    }

    mNeedUpdatePathForBorderRadius = false;

    if (mInnerClipPathForBorderRadius == null) {
      mInnerClipPathForBorderRadius = new Path();
    }

    if (mBackgroundColorRenderPath == null) {
      mBackgroundColorRenderPath = new Path();
    }

    if (mOuterClipPathForBorderRadius == null) {
      mOuterClipPathForBorderRadius = new Path();
    }

    if (mPathForBorderRadiusOutline == null) {
      mPathForBorderRadiusOutline = new Path();
    }

    if (mCenterDrawPath == null) {
      mCenterDrawPath = new Path();
    }

    if (mInnerClipTempRectForBorderRadius == null) {
      mInnerClipTempRectForBorderRadius = new RectF();
    }

    if (mOuterClipTempRectForBorderRadius == null) {
      mOuterClipTempRectForBorderRadius = new RectF();
    }

    if (mTempRectForBorderRadiusOutline == null) {
      mTempRectForBorderRadiusOutline = new RectF();
    }

    if (mTempRectForCenterDrawPath == null) {
      mTempRectForCenterDrawPath = new RectF();
    }

    mInnerClipPathForBorderRadius.reset();
    mBackgroundColorRenderPath.reset();
    mOuterClipPathForBorderRadius.reset();
    mPathForBorderRadiusOutline.reset();
    mCenterDrawPath.reset();

    mInnerClipTempRectForBorderRadius.set(getBounds());
    mOuterClipTempRectForBorderRadius.set(getBounds());
    mTempRectForBorderRadiusOutline.set(getBounds());
    mTempRectForCenterDrawPath.set(getBounds());

    final RectF borderWidth = getDirectionAwareBorderInsets();

    int colorLeft = getBorderColor(Spacing.LEFT);
    int colorTop = getBorderColor(Spacing.TOP);
    int colorRight = getBorderColor(Spacing.RIGHT);
    int colorBottom = getBorderColor(Spacing.BOTTOM);
    int borderColor = getBorderColor(Spacing.ALL);

    int colorBlock = getBorderColor(Spacing.BLOCK);
    int colorBlockStart = getBorderColor(Spacing.BLOCK_START);
    int colorBlockEnd = getBorderColor(Spacing.BLOCK_END);

    if (isBorderColorDefined(Spacing.BLOCK)) {
      colorBottom = colorBlock;
      colorTop = colorBlock;
    }
    if (isBorderColorDefined(Spacing.BLOCK_END)) {
      colorBottom = colorBlockEnd;
    }
    if (isBorderColorDefined(Spacing.BLOCK_START)) {
      colorTop = colorBlockStart;
    }

    // Clip border ONLY if at least one edge is non-transparent
    if (Color.alpha(colorLeft) != 0
      || Color.alpha(colorTop) != 0
      || Color.alpha(colorRight) != 0
      || Color.alpha(colorBottom) != 0
      || Color.alpha(borderColor) != 0) {

      mInnerClipTempRectForBorderRadius.top += borderWidth.top;
      mInnerClipTempRectForBorderRadius.bottom -= borderWidth.bottom;
      mInnerClipTempRectForBorderRadius.left += borderWidth.left;
      mInnerClipTempRectForBorderRadius.right -= borderWidth.right;
    }

    mTempRectForCenterDrawPath.top += borderWidth.top * 0.5f;
    mTempRectForCenterDrawPath.bottom -= borderWidth.bottom * 0.5f;
    mTempRectForCenterDrawPath.left += borderWidth.left * 0.5f;
    mTempRectForCenterDrawPath.right -= borderWidth.right * 0.5f;

    mComputedBorderRadius =
      mBorderRadius.resolve(
        getLayoutDirection(),
        mContext,
        PixelUtil.toDIPFromPixel(mOuterClipTempRectForBorderRadius.width()),
        PixelUtil.toDIPFromPixel(mOuterClipTempRectForBorderRadius.height()));
    CornerRadii topLeftRadius = mComputedBorderRadius.getTopLeft().toPixelFromDIP();
    CornerRadii topRightRadius = mComputedBorderRadius.getTopRight().toPixelFromDIP();
    CornerRadii bottomLeftRadius = mComputedBorderRadius.getBottomLeft().toPixelFromDIP();
    CornerRadii bottomRightRadius = mComputedBorderRadius.getBottomRight().toPixelFromDIP();

    final float innerTopLeftRadiusX =
      getInnerBorderRadius(topLeftRadius.getHorizontal(), borderWidth.left);
    final float innerTopLeftRadiusY =
      getInnerBorderRadius(topLeftRadius.getVertical(), borderWidth.top);
    final float innerTopRightRadiusX =
      getInnerBorderRadius(topRightRadius.getHorizontal(), borderWidth.right);
    final float innerTopRightRadiusY =
      getInnerBorderRadius(topRightRadius.getVertical(), borderWidth.top);
    final float innerBottomRightRadiusX =
      getInnerBorderRadius(bottomRightRadius.getHorizontal(), borderWidth.right);
    final float innerBottomRightRadiusY =
      getInnerBorderRadius(bottomRightRadius.getVertical(), borderWidth.bottom);
    final float innerBottomLeftRadiusX =
      getInnerBorderRadius(bottomLeftRadius.getHorizontal(), borderWidth.left);
    final float innerBottomLeftRadiusY =
      getInnerBorderRadius(bottomLeftRadius.getVertical(), borderWidth.bottom);

    mInnerClipPathForBorderRadius.addRoundRect(
      mInnerClipTempRectForBorderRadius,
      new float[] {
        innerTopLeftRadiusX,
        innerTopLeftRadiusY,
        innerTopRightRadiusX,
        innerTopRightRadiusY,
        innerBottomRightRadiusX,
        innerBottomRightRadiusY,
        innerBottomLeftRadiusX,
        innerBottomLeftRadiusY,
      },
      Path.Direction.CW);

    // There is a small gap between mBackgroundColorRenderPath and its
    // border. mGapBetweenPaths is used to slightly enlarge the rectangle
    // (mInnerClipTempRectForBorderRadius), ensuring the border can be
    // drawn on top without the gap.
    // only close gap between border and main path if we draw the border, otherwise
    // we wind up pixelating small pixel-radius curves
    var leftSizeIncrease = (borderWidth.left > 0 ? mGapBetweenPaths : 0f);
    var rightSizeIncrease = (borderWidth.right > 0 ? mGapBetweenPaths : 0f);
    var topSizeIncrease = (borderWidth.top > 0 ? mGapBetweenPaths : 0f);
    var bottomSizeIncrease = (borderWidth.bottom > 0 ? mGapBetweenPaths : 0f);
    var squirclePath = SquirclePathCalculator.getPath(
      mComputedBorderRadius,
      getBounds().width() + leftSizeIncrease + rightSizeIncrease - (borderWidth.left + borderWidth.right),
      getBounds().height() + topSizeIncrease + bottomSizeIncrease - (borderWidth.top + borderWidth.bottom)
    );

    var horizontalOffset = borderWidth.left;
    if (leftSizeIncrease > 0) horizontalOffset -= mGapBetweenPaths;
    if (rightSizeIncrease > 0) horizontalOffset += mGapBetweenPaths;

    var verticalOffset = borderWidth.top;
    if (topSizeIncrease > 0) verticalOffset -= mGapBetweenPaths;
    if (bottomSizeIncrease > 0) verticalOffset -= mGapBetweenPaths;

    squirclePath.offset(horizontalOffset, verticalOffset);
    mBackgroundColorRenderPath.set(squirclePath);

    mOuterClipPathForBorderRadius.addRoundRect(
      mOuterClipTempRectForBorderRadius,
      new float[] {
        topLeftRadius.getHorizontal(),
        topLeftRadius.getVertical(),
        topRightRadius.getHorizontal(),
        topRightRadius.getVertical(),
        bottomRightRadius.getHorizontal(),
        bottomRightRadius.getVertical(),
        bottomLeftRadius.getHorizontal(),
        bottomLeftRadius.getVertical()
      },
      Path.Direction.CW);

    float extraRadiusForOutline = 0;

    if (mBorderWidth != null) {
      extraRadiusForOutline = mBorderWidth.get(Spacing.ALL) / 2f;
    }

    var mPathForBorderRadiusOutlineRadius = new ComputedBorderRadius(
      new CornerRadii(
        PixelUtil.toDIPFromPixel(topLeftRadius.getHorizontal() + extraRadiusForOutline),
        PixelUtil.toDIPFromPixel(topLeftRadius.getVertical() + extraRadiusForOutline)
      ),
      new CornerRadii(
        PixelUtil.toDIPFromPixel(topRightRadius.getHorizontal() + extraRadiusForOutline),
        PixelUtil.toDIPFromPixel(topRightRadius.getVertical() + extraRadiusForOutline)
      ),
      new CornerRadii(
        PixelUtil.toDIPFromPixel(bottomLeftRadius.getHorizontal() + extraRadiusForOutline),
        PixelUtil.toDIPFromPixel(bottomLeftRadius.getVertical() + extraRadiusForOutline)
      ),
      new CornerRadii(
        PixelUtil.toDIPFromPixel(bottomRightRadius.getHorizontal() + extraRadiusForOutline),
        PixelUtil.toDIPFromPixel(bottomRightRadius.getVertical() + extraRadiusForOutline)
      )
    );

    mPathForBorderRadiusOutline.set(SquirclePathCalculator.getPath(
      mPathForBorderRadiusOutlineRadius,
      mTempRectForBorderRadiusOutline.width(),
      mTempRectForBorderRadiusOutline.height()
    ));

    var mCenterDrawPathRadius = new ComputedBorderRadius(
      new CornerRadii(
        topLeftRadius.getHorizontal() > 0
        ? PixelUtil.toDIPFromPixel(topLeftRadius.getHorizontal() + borderWidth.left * 0.5f)
        : 0,
        topLeftRadius.getVertical() > 0
        ? PixelUtil.toDIPFromPixel(topLeftRadius.getVertical() + borderWidth.top * 0.5f)
        : 0
      ),
      new CornerRadii(
        topRightRadius.getHorizontal() > 0
        ? PixelUtil.toDIPFromPixel(topRightRadius.getHorizontal() + borderWidth.right * 0.5f)
        : 0,
        topRightRadius.getVertical() > 0
        ? PixelUtil.toDIPFromPixel(topRightRadius.getVertical() + borderWidth.top * 0.5f)
        : 0
      ),
      new CornerRadii(
        bottomLeftRadius.getHorizontal() > 0
        ? PixelUtil.toDIPFromPixel(bottomLeftRadius.getHorizontal() + borderWidth.left * 0.5f)
        : 0,
        bottomLeftRadius.getVertical() > 0
        ? PixelUtil.toDIPFromPixel(bottomLeftRadius.getVertical() + borderWidth.bottom * 0.5f)
        : 0
      ),
      new CornerRadii(
        bottomRightRadius.getHorizontal() > 0 ?
          PixelUtil.toDIPFromPixel(bottomRightRadius.getHorizontal() + borderWidth.right * 0.5f)
          : 0,
        bottomRightRadius.getVertical() > 0 ?
          PixelUtil.toDIPFromPixel(bottomRightRadius.getVertical() + borderWidth.right * 0.5f)
          : 0
      )
    );
    mCenterDrawPath.set(SquirclePathCalculator.getPath(
      mCenterDrawPathRadius,
      mTempRectForCenterDrawPath.width(),
      mTempRectForCenterDrawPath.height()
    ));
    mCenterDrawPath.offset(
      borderWidth.left * 0.5f,
      borderWidth.top * 0.5f
    );

    /**
     * Rounded Multi-Colored Border Algorithm:
     *
     * <p>Let O (for outer) = (top, left, bottom, right) be the rectangle that represents the size
     * and position of a view V. Since the box-sizing of all React Native views is border-box, any
     * border of V will render inside O.
     *
     * <p>Let BorderWidth = (borderTop, borderLeft, borderBottom, borderRight).
     *
     * <p>Let I (for inner) = O - BorderWidth.
     *
     * <p>Then, remembering that O and I are rectangles and that I is inside O, O - I gives us the
     * border of V. Therefore, we can use canvas.clipPath to draw V's border.
     *
     * <p>canvas.clipPath(O, Region.OP.INTERSECT);
     *
     * <p>canvas.clipPath(I, Region.OP.DIFFERENCE);
     *
     * <p>canvas.drawRect(O, paint);
     *
     * <p>This lets us draw non-rounded single-color borders.
     *
     * <p>To extend this algorithm to rounded single-color borders, we:
     *
     * <p>1. Curve the corners of O by the (border radii of V) using Path#addRoundRect.
     *
     * <p>2. Curve the corners of I by (border radii of V - border widths of V) using
     * Path#addRoundRect.
     *
     * <p>Let O' = curve(O, border radii of V).
     *
     * <p>Let I' = curve(I, border radii of V - border widths of V)
     *
     * <p>The rationale behind this decision is the (first sentence of the) following section in the
     * CSS Backgrounds and Borders Module Level 3:
     * https://www.w3.org/TR/css3-background/#the-border-radius.
     *
     * <p>After both O and I have been curved, we can execute the following lines once again to
     * render curved single-color borders:
     *
     * <p>canvas.clipPath(O, Region.OP.INTERSECT);
     *
     * <p>canvas.clipPath(I, Region.OP.DIFFERENCE);
     *
     * <p>canvas.drawRect(O, paint);
     *
     * <p>To extend this algorithm to rendering multi-colored rounded borders, we render each side
     * of the border as its own quadrilateral. Suppose that we were handling the case where all the
     * border radii are 0. Then, the four quadrilaterals would be:
     *
     * <p>Left: (O.left, O.top), (I.left, I.top), (I.left, I.bottom), (O.left, O.bottom)
     *
     * <p>Top: (O.left, O.top), (I.left, I.top), (I.right, I.top), (O.right, O.top)
     *
     * <p>Right: (O.right, O.top), (I.right, I.top), (I.right, I.bottom), (O.right, O.bottom)
     *
     * <p>Bottom: (O.right, O.bottom), (I.right, I.bottom), (I.left, I.bottom), (O.left, O.bottom)
     *
     * <p>Now, lets consider what happens when we render a rounded border (radii != 0). For the sake
     * of simplicity, let's focus on the top edge of the Left border:
     *
     * <p>Let borderTopLeftRadius = 5. Let borderLeftWidth = 1. Let borderTopWidth = 2.
     *
     * <p>We know that O is curved by the ellipse E_O (a = 5, b = 5). We know that I is curved by
     * the ellipse E_I (a = 5 - 1, b = 5 - 2).
     *
     * <p>Since we have clipping, it should be safe to set the top-left point of the Left
     * quadrilateral's top edge to (O.left, O.top).
     *
     * <p>But, what should the top-right point be?
     *
     * <p>The fact that the border is curved shouldn't change the slope (nor the position) of the
     * line connecting the top-left and top-right points of the Left quadrilateral's top edge.
     * Therefore, The top-right point should lie somewhere on the line L = (1 - a) * (O.left, O.top)
     * + a * (I.left, I.top).
     *
     * <p>a != 0, because then the top-left and top-right points would be the same and
     * borderLeftWidth = 1. a != 1, because then the top-right point would not touch an edge of the
     * ellipse E_I. We want the top-right point to touch an edge of the inner ellipse because the
     * border curves with E_I on the top-left corner of V.
     *
     * <p>Therefore, it must be the case that a > 1. Two natural locations of the top-right point
     * exist: 1. The first intersection of L with E_I. 2. The second intersection of L with E_I.
     *
     * <p>We choose the top-right point of the top edge of the Left quadrilateral to be an arbitrary
     * intersection of L with E_I.
     */
    if (mInnerTopLeftCorner == null) {
      mInnerTopLeftCorner = new PointF();
    }

    /** Compute mInnerTopLeftCorner */
    mInnerTopLeftCorner.x = mInnerClipTempRectForBorderRadius.left;
    mInnerTopLeftCorner.y = mInnerClipTempRectForBorderRadius.top;

    getEllipseIntersectionWithLine(
      // Ellipse Bounds
      mInnerClipTempRectForBorderRadius.left,
      mInnerClipTempRectForBorderRadius.top,
      mInnerClipTempRectForBorderRadius.left + 2 * innerTopLeftRadiusX,
      mInnerClipTempRectForBorderRadius.top + 2 * innerTopLeftRadiusY,

      // Line Start
      mOuterClipTempRectForBorderRadius.left,
      mOuterClipTempRectForBorderRadius.top,

      // Line End
      mInnerClipTempRectForBorderRadius.left,
      mInnerClipTempRectForBorderRadius.top,

      // Result
      mInnerTopLeftCorner);

    /** Compute mInnerBottomLeftCorner */
    if (mInnerBottomLeftCorner == null) {
      mInnerBottomLeftCorner = new PointF();
    }

    mInnerBottomLeftCorner.x = mInnerClipTempRectForBorderRadius.left;
    mInnerBottomLeftCorner.y = mInnerClipTempRectForBorderRadius.bottom;

    getEllipseIntersectionWithLine(
      // Ellipse Bounds
      mInnerClipTempRectForBorderRadius.left,
      mInnerClipTempRectForBorderRadius.bottom - 2 * innerBottomLeftRadiusY,
      mInnerClipTempRectForBorderRadius.left + 2 * innerBottomLeftRadiusX,
      mInnerClipTempRectForBorderRadius.bottom,

      // Line Start
      mOuterClipTempRectForBorderRadius.left,
      mOuterClipTempRectForBorderRadius.bottom,

      // Line End
      mInnerClipTempRectForBorderRadius.left,
      mInnerClipTempRectForBorderRadius.bottom,

      // Result
      mInnerBottomLeftCorner);

    /** Compute mInnerTopRightCorner */
    if (mInnerTopRightCorner == null) {
      mInnerTopRightCorner = new PointF();
    }

    mInnerTopRightCorner.x = mInnerClipTempRectForBorderRadius.right;
    mInnerTopRightCorner.y = mInnerClipTempRectForBorderRadius.top;

    getEllipseIntersectionWithLine(
      // Ellipse Bounds
      mInnerClipTempRectForBorderRadius.right - 2 * innerTopRightRadiusX,
      mInnerClipTempRectForBorderRadius.top,
      mInnerClipTempRectForBorderRadius.right,
      mInnerClipTempRectForBorderRadius.top + 2 * innerTopRightRadiusY,

      // Line Start
      mOuterClipTempRectForBorderRadius.right,
      mOuterClipTempRectForBorderRadius.top,

      // Line End
      mInnerClipTempRectForBorderRadius.right,
      mInnerClipTempRectForBorderRadius.top,

      // Result
      mInnerTopRightCorner);

    /** Compute mInnerBottomRightCorner */
    if (mInnerBottomRightCorner == null) {
      mInnerBottomRightCorner = new PointF();
    }

    mInnerBottomRightCorner.x = mInnerClipTempRectForBorderRadius.right;
    mInnerBottomRightCorner.y = mInnerClipTempRectForBorderRadius.bottom;

    getEllipseIntersectionWithLine(
      // Ellipse Bounds
      mInnerClipTempRectForBorderRadius.right - 2 * innerBottomRightRadiusX,
      mInnerClipTempRectForBorderRadius.bottom - 2 * innerBottomRightRadiusY,
      mInnerClipTempRectForBorderRadius.right,
      mInnerClipTempRectForBorderRadius.bottom,

      // Line Start
      mOuterClipTempRectForBorderRadius.right,
      mOuterClipTempRectForBorderRadius.bottom,

      // Line End
      mInnerClipTempRectForBorderRadius.right,
      mInnerClipTempRectForBorderRadius.bottom,

      // Result
      mInnerBottomRightCorner);
  }

  private static void getEllipseIntersectionWithLine(
    double ellipseBoundsLeft,
    double ellipseBoundsTop,
    double ellipseBoundsRight,
    double ellipseBoundsBottom,
    double lineStartX,
    double lineStartY,
    double lineEndX,
    double lineEndY,
    PointF result) {
    final double ellipseCenterX = (ellipseBoundsLeft + ellipseBoundsRight) / 2;
    final double ellipseCenterY = (ellipseBoundsTop + ellipseBoundsBottom) / 2;

    /**
     * Step 1:
     *
     * <p>Translate the line so that the ellipse is at the origin.
     *
     * <p>Why? It makes the math easier by changing the ellipse equation from ((x -
     * ellipseCenterX)/a)^2 + ((y - ellipseCenterY)/b)^2 = 1 to (x/a)^2 + (y/b)^2 = 1.
     */
    lineStartX -= ellipseCenterX;
    lineStartY -= ellipseCenterY;
    lineEndX -= ellipseCenterX;
    lineEndY -= ellipseCenterY;

    /**
     * Step 2:
     *
     * <p>Ellipse equation: (x/a)^2 + (y/b)^2 = 1 Line equation: y = mx + c
     */
    final double a = Math.abs(ellipseBoundsRight - ellipseBoundsLeft) / 2;
    final double b = Math.abs(ellipseBoundsBottom - ellipseBoundsTop) / 2;
    final double m = (lineEndY - lineStartY) / (lineEndX - lineStartX);
    final double c = lineStartY - m * lineStartX; // Just a point on the line

    /**
     * Step 3:
     *
     * <p>Substitute the Line equation into the Ellipse equation. Solve for x. Eventually, you'll
     * have to use the quadratic formula.
     *
     * <p>Quadratic formula: Ax^2 + Bx + C = 0
     */
    final double A = (b * b + a * a * m * m);
    final double B = 2 * a * a * c * m;
    final double C = (a * a * (c * c - b * b));

    /**
     * Step 4:
     *
     * <p>Apply Quadratic formula. D = determinant / 2A
     */
    final double D = Math.sqrt(-C / A + Math.pow(B / (2 * A), 2));
    final double x2 = -B / (2 * A) - D;
    final double y2 = m * x2 + c;

    /**
     * Step 5:
     *
     * <p>Undo the space transformation in Step 5.
     */
    final double x = x2 + ellipseCenterX;
    final double y = y2 + ellipseCenterY;

    if (!Double.isNaN(x) && !Double.isNaN(y)) {
      result.x = (float) x;
      result.y = (float) y;
    }
  }


  /** Set type of border */
  private void updatePathEffect() {
    // Used for rounded border and rounded background
    PathEffect mPathEffectForBorderStyle =
      mBorderStyle != null ? getPathEffect(mBorderStyle, getFullBorderWidth()) : null;

    mPaint.setPathEffect(mPathEffectForBorderStyle);
  }

  private void updatePathEffect(int borderWidth) {
    PathEffect pathEffectForBorderStyle = null;
    if (mBorderStyle != null) {
      pathEffectForBorderStyle = getPathEffect(mBorderStyle, borderWidth);
    }
    mPaint.setPathEffect(pathEffectForBorderStyle);
  }

  /**
   * Quickly determine if all the set border colors are equal. Bitwise AND all the set colors
   * together, then OR them all together. If the AND and the OR are the same, then the colors are
   * compatible, so return this color.
   *
   * <p>Used to avoid expensive path creation and expensive calls to canvas.drawPath
   *
   * @return A compatible border color, or zero if the border colors are not compatible.
   */
  private static int fastBorderCompatibleColorOrZero(
    int borderLeft,
    int borderTop,
    int borderRight,
    int borderBottom,
    int colorLeft,
    int colorTop,
    int colorRight,
    int colorBottom) {
    int andSmear =
      (borderLeft > 0 ? colorLeft : ALL_BITS_SET)
        & (borderTop > 0 ? colorTop : ALL_BITS_SET)
        & (borderRight > 0 ? colorRight : ALL_BITS_SET)
        & (borderBottom > 0 ? colorBottom : ALL_BITS_SET);
    int orSmear =
      (borderLeft > 0 ? colorLeft : ALL_BITS_UNSET)
        | (borderTop > 0 ? colorTop : ALL_BITS_UNSET)
        | (borderRight > 0 ? colorRight : ALL_BITS_UNSET)
        | (borderBottom > 0 ? colorBottom : ALL_BITS_UNSET);
    return andSmear == orSmear ? andSmear : 0;
  }

  private void drawSquircleBackgroundWithBorders(Canvas canvas) {
    updatePath();
    canvas.save();

    // Draws the View without its border first (with background color fill)
    int useColor = ColorUtils.setAlphaComponent(mColor, (Color.alpha(mColor) * mAlpha) >> 8);
    if (Color.alpha(useColor) != 0) {
      mPaint.setColor(useColor);
      mPaint.setStyle(Paint.Style.FILL);
      canvas.drawPath(Objects.requireNonNull(mBackgroundColorRenderPath), mPaint);
    }

    if (mBackgroundImageLayers != null && !mBackgroundImageLayers.isEmpty()) {
      mPaint.setShader(getBackgroundImageShader());
      mPaint.setStyle(Paint.Style.FILL);
      canvas.drawPath(Objects.requireNonNull(mBackgroundColorRenderPath), mPaint);
      mPaint.setShader(null);
    }

    final RectF borderWidth = getDirectionAwareBorderInsets();
    int colorLeft = getBorderColor(Spacing.LEFT);
    int colorTop = getBorderColor(Spacing.TOP);
    int colorRight = getBorderColor(Spacing.RIGHT);
    int colorBottom = getBorderColor(Spacing.BOTTOM);

    int colorBlock = getBorderColor(Spacing.BLOCK);
    int colorBlockStart = getBorderColor(Spacing.BLOCK_START);
    int colorBlockEnd = getBorderColor(Spacing.BLOCK_END);

    if (isBorderColorDefined(Spacing.BLOCK)) {
      colorBottom = colorBlock;
      colorTop = colorBlock;
    }
    if (isBorderColorDefined(Spacing.BLOCK_END)) {
      colorBottom = colorBlockEnd;
    }
    if (isBorderColorDefined(Spacing.BLOCK_START)) {
      colorTop = colorBlockStart;
    }

    if (borderWidth.top > 0
      || borderWidth.bottom > 0
      || borderWidth.left > 0
      || borderWidth.right > 0) {

      // Clip outer border
      canvas.clipPath(
        Objects.requireNonNull(mOuterClipPathForBorderRadius), Region.Op.INTERSECT);

      // If it's a full and even border draw inner rect path with stroke
      final float fullBorderWidth = getFullBorderWidth();
      int borderColor = getBorderColor(Spacing.ALL);
      if (borderWidth.top == fullBorderWidth
        && borderWidth.bottom == fullBorderWidth
        && borderWidth.left == fullBorderWidth
        && borderWidth.right == fullBorderWidth
        && colorLeft == borderColor
        && colorTop == borderColor
        && colorRight == borderColor
        && colorBottom == borderColor) {
        if (fullBorderWidth > 0) {
          mPaint.setColor(multiplyColorAlpha(borderColor, mAlpha));
          mPaint.setStyle(Paint.Style.STROKE);
          mPaint.setStrokeWidth(fullBorderWidth);
          canvas.drawPath(Objects.requireNonNull(mCenterDrawPath), mPaint);
        }
      }
      // In the case of uneven border widths/colors draw quadrilateral in each direction
      else {
        mPaint.setStyle(Paint.Style.FILL);

        // Clip inner border
        canvas.clipPath(
          Objects.requireNonNull(mInnerClipPathForBorderRadius), Region.Op.DIFFERENCE);

        final boolean isRTL = getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
        int colorStart = getBorderColor(Spacing.START);
        int colorEnd = getBorderColor(Spacing.END);

        if (I18nUtil.getInstance().doLeftAndRightSwapInRTL(mContext)) {
          if (!isBorderColorDefined(Spacing.START)) {
            colorStart = colorLeft;
          }

          if (!isBorderColorDefined(Spacing.END)) {
            colorEnd = colorRight;
          }

          final int directionAwareColorLeft = isRTL ? colorEnd : colorStart;
          final int directionAwareColorRight = isRTL ? colorStart : colorEnd;

          colorLeft = directionAwareColorLeft;
          colorRight = directionAwareColorRight;
        } else {
          final int directionAwareColorLeft = isRTL ? colorEnd : colorStart;
          final int directionAwareColorRight = isRTL ? colorStart : colorEnd;

          final boolean isColorStartDefined = isBorderColorDefined(Spacing.START);
          final boolean isColorEndDefined = isBorderColorDefined(Spacing.END);
          final boolean isDirectionAwareColorLeftDefined =
            isRTL ? isColorEndDefined : isColorStartDefined;
          final boolean isDirectionAwareColorRightDefined =
            isRTL ? isColorStartDefined : isColorEndDefined;

          if (isDirectionAwareColorLeftDefined) {
            colorLeft = directionAwareColorLeft;
          }

          if (isDirectionAwareColorRightDefined) {
            colorRight = directionAwareColorRight;
          }
        }

        final RectF outerClipTempRect =
          Objects.requireNonNull(mOuterClipTempRectForBorderRadius);
        final float left = outerClipTempRect.left;
        final float right = outerClipTempRect.right;
        final float top = outerClipTempRect.top;
        final float bottom = outerClipTempRect.bottom;

        final PointF innerTopLeftCorner = Objects.requireNonNull(mInnerTopLeftCorner);
        final PointF innerTopRightCorner = Objects.requireNonNull(mInnerTopRightCorner);
        final PointF innerBottomLeftCorner = Objects.requireNonNull(mInnerBottomLeftCorner);
        final PointF innerBottomRightCorner = Objects.requireNonNull(mInnerBottomRightCorner);

        // mGapBetweenPaths is used to close the gap between the diagonal
        // edges of the quadrilaterals on adjacent sides of the rectangle
        if (borderWidth.left > 0) {
          final float x1 = left;
          final float y1 = top - mGapBetweenPaths;
          final float x2 = innerTopLeftCorner.x;
          final float y2 = innerTopLeftCorner.y - mGapBetweenPaths;
          final float x3 = innerBottomLeftCorner.x;
          final float y3 = innerBottomLeftCorner.y + mGapBetweenPaths;
          final float x4 = left;
          final float y4 = bottom + mGapBetweenPaths;

          drawQuadrilateral(canvas, colorLeft, x1, y1, x2, y2, x3, y3, x4, y4);
        }

        if (borderWidth.top > 0) {
          final float x1 = left - mGapBetweenPaths;
          final float y1 = top;
          final float x2 = innerTopLeftCorner.x - mGapBetweenPaths;
          final float y2 = innerTopLeftCorner.y;
          final float x3 = innerTopRightCorner.x + mGapBetweenPaths;
          final float y3 = innerTopRightCorner.y;
          final float x4 = right + mGapBetweenPaths;
          final float y4 = top;

          drawQuadrilateral(canvas, colorTop, x1, y1, x2, y2, x3, y3, x4, y4);
        }

        if (borderWidth.right > 0) {
          final float x1 = right;
          final float y1 = top - mGapBetweenPaths;
          final float x2 = innerTopRightCorner.x;
          final float y2 = innerTopRightCorner.y - mGapBetweenPaths;
          final float x3 = innerBottomRightCorner.x;
          final float y3 = innerBottomRightCorner.y + mGapBetweenPaths;
          final float x4 = right;
          final float y4 = bottom + mGapBetweenPaths;

          drawQuadrilateral(canvas, colorRight, x1, y1, x2, y2, x3, y3, x4, y4);
        }

        if (borderWidth.bottom > 0) {
          final float x1 = left - mGapBetweenPaths;
          final float y1 = bottom;
          final float x2 = innerBottomLeftCorner.x - mGapBetweenPaths;
          final float y2 = innerBottomLeftCorner.y;
          final float x3 = innerBottomRightCorner.x + mGapBetweenPaths;
          final float y3 = innerBottomRightCorner.y;
          final float x4 = right + mGapBetweenPaths;
          final float y4 = bottom;

          drawQuadrilateral(canvas, colorBottom, x1, y1, x2, y2, x3, y3, x4, y4);
        }
      }
    }

    canvas.restore();
  }

  private void drawQuadrilateral(
    Canvas canvas,
    int fillColor,
    float x1,
    float y1,
    float x2,
    float y2,
    float x3,
    float y3,
    float x4,
    float y4) {
    if (fillColor == Color.TRANSPARENT) {
      return;
    }

    if (mPathForBorder == null) {
      mPathForBorder = new Path();
    }

    mPaint.setColor(fillColor);
    mPathForBorder.reset();
    mPathForBorder.moveTo(x1, y1);
    mPathForBorder.lineTo(x2, y2);
    mPathForBorder.lineTo(x3, y3);
    mPathForBorder.lineTo(x4, y4);
    mPathForBorder.lineTo(x1, y1);
    canvas.drawPath(mPathForBorder, mPaint);
  }

  private static int colorFromAlphaAndRGBComponents(float alpha, float rgb) {
    int rgbComponent = 0x00FFFFFF & (int) rgb;
    int alphaComponent = 0xFF000000 & ((int) alpha) << 24;

    return rgbComponent | alphaComponent;
  }

  private boolean isBorderColorDefined(int position) {
    final float rgb = mBorderRGB != null ? mBorderRGB.get(position) : Float.NaN;
    final float alpha = mBorderAlpha != null ? mBorderAlpha.get(position) : Float.NaN;
    return !Float.isNaN(rgb) && !Float.isNaN(alpha);
  }

  public int getBorderColor(int position) {
    float rgb = mBorderRGB != null ? mBorderRGB.get(position) : DEFAULT_BORDER_RGB;
    float alpha = mBorderAlpha != null ? mBorderAlpha.get(position) : DEFAULT_BORDER_ALPHA;

    return colorFromAlphaAndRGBComponents(alpha, rgb);
  }

  private @Nullable Shader getBackgroundImageShader() {
    if (mBackgroundImageLayers == null) {
      return null;
    }

    Shader compositeShader = null;
    for (BackgroundImageLayer backgroundImageLayer : mBackgroundImageLayers) {
      Shader currentShader = backgroundImageLayer.getShader(getBounds());
      if (currentShader == null) {
        continue;
      }
      if (compositeShader == null) {
        compositeShader = currentShader;
      } else {
        compositeShader =
          new ComposeShader(currentShader, compositeShader, PorterDuff.Mode.SRC_OVER);
      }
    }
    return compositeShader;
  }

  /**
   * Multiplies the color with the given alpha.
   *
   * @param color color to be multiplied
   * @param alpha value between 0 and 255
   * @return multiplied color
   */
  private static int multiplyColorAlpha(int color, int alpha) {
    if (alpha == 255) {
      return color;
    }
    if (alpha == 0) {
      return color & 0x00FFFFFF;
    }
    alpha = alpha + (alpha >> 7); // make it 0..256
    int colorAlpha = color >>> 24;
    int multipliedAlpha = colorAlpha * alpha >> 8;
    return (multipliedAlpha << 24) | (color & 0x00FFFFFF);
  }

}
