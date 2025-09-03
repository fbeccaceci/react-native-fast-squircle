#import "FastSquircleView.h"

#import <react/renderer/components/FastSquircleViewSpec/ComponentDescriptors.h>
#import <react/renderer/components/FastSquircleViewSpec/EventEmitters.h>
#import <react/renderer/components/FastSquircleViewSpec/Props.h>
#import <react/renderer/components/FastSquircleViewSpec/RCTComponentViewHelpers.h>

#import "RCTFabricComponentsPlugins.h"

#import <objc/runtime.h>
#import <React/RCTBorderDrawing.h>
#import <React/RCTConversions.h>
#import "FastSquircleBorderDrawing.h"
#import "FastSquircle-Swift.h"
#import "FastSquircleBoxShadow.h"

using namespace facebook::react;

const CGFloat BACKGROUND_COLOR_ZPOSITION = -1024.0f;

static void UpdateContourEffectToSquircleToLayer(
    CALayer *layer,
    const RCTCornerRadii &cornerRadii,
    const RCTBorderColors &contourColors,
    const UIEdgeInsets &contourInsets,
    const RCTBorderStyle &contourStyle,
    NSNumber *cornerSmoothing)
{
  UIImage *image = FastSquircleGetBorderImage(
      contourStyle, layer.bounds.size, cornerRadii, contourInsets, contourColors, [UIColor clearColor], NO, cornerSmoothing);
    
  if (image == nil) {
    layer.contents = nil;
  } else {
    CGSize imageSize = image.size;
    UIEdgeInsets imageCapInsets = image.capInsets;
    CGRect contentsCenter = CGRect{
        CGPoint{imageCapInsets.left / imageSize.width, imageCapInsets.top / imageSize.height},
        CGSize{(CGFloat)1.0 / imageSize.width, (CGFloat)1.0 / imageSize.height}};
    layer.contents = (id)image.CGImage;
    layer.contentsScale = image.scale;

    BOOL isResizable = !UIEdgeInsetsEqualToEdgeInsets(image.capInsets, UIEdgeInsetsZero);
    if (isResizable) {
      layer.contentsCenter = contentsCenter;
    } else {
      layer.contentsCenter = CGRect{CGPoint{0.0, 0.0}, CGSize{1.0, 1.0}};
    }
  }

  // If mutations are applied inside of Animation block, it may cause layer to be animated.
  // To stop that, imperatively remove all animations from layer.
  [layer removeAllAnimations];
}

static RCTCornerRadii RCTCornerRadiiFromBorderRadii(BorderRadii borderRadii)
{
  return RCTCornerRadii{
      .topLeftHorizontal = (CGFloat)borderRadii.topLeft.horizontal,
      .topLeftVertical = (CGFloat)borderRadii.topLeft.vertical,
      .topRightHorizontal = (CGFloat)borderRadii.topRight.horizontal,
      .topRightVertical = (CGFloat)borderRadii.topRight.vertical,
      .bottomLeftHorizontal = (CGFloat)borderRadii.bottomLeft.horizontal,
      .bottomLeftVertical = (CGFloat)borderRadii.bottomLeft.vertical,
      .bottomRightHorizontal = (CGFloat)borderRadii.bottomRight.horizontal,
      .bottomRightVertical = (CGFloat)borderRadii.bottomRight.vertical};
}

static RCTBorderColors RCTCreateRCTBorderColorsFromBorderColors(BorderColors borderColors)
{
  return RCTBorderColors{
      .top = RCTUIColorFromSharedColor(borderColors.top),
      .left = RCTUIColorFromSharedColor(borderColors.left),
      .bottom = RCTUIColorFromSharedColor(borderColors.bottom),
      .right = RCTUIColorFromSharedColor(borderColors.right)};
}

static RCTBorderStyle RCTBorderStyleFromBorderStyle(BorderStyle borderStyle)
{
  switch (borderStyle) {
    case BorderStyle::Solid:
      return RCTBorderStyleSolid;
    case BorderStyle::Dotted:
      return RCTBorderStyleDotted;
    case BorderStyle::Dashed:
      return RCTBorderStyleDashed;
  }
}

static RCTCornerRadii RCTCreateOutlineCornerRadiiFromBorderRadii(const BorderRadii &borderRadii, CGFloat outlineWidth, CGFloat outlineOffset)
{
  return RCTCornerRadii{
      borderRadii.topLeft.horizontal != 0 ? borderRadii.topLeft.horizontal + outlineWidth + outlineOffset : 0,
      borderRadii.topLeft.vertical != 0 ? borderRadii.topLeft.vertical + outlineWidth + outlineOffset : 0,
      borderRadii.topRight.horizontal != 0 ? borderRadii.topRight.horizontal + outlineWidth + outlineOffset : 0,
      borderRadii.topRight.vertical != 0 ? borderRadii.topRight.vertical + outlineWidth + outlineOffset : 0,
      borderRadii.bottomLeft.horizontal != 0 ? borderRadii.bottomLeft.horizontal + outlineWidth + outlineOffset: 0,
      borderRadii.bottomLeft.vertical != 0 ? borderRadii.bottomLeft.vertical + outlineWidth + outlineOffset: 0,
      borderRadii.bottomRight.horizontal != 0 ? borderRadii.bottomRight.horizontal + outlineWidth + outlineOffset: 0,
      borderRadii.bottomRight.vertical != 0 ? borderRadii.bottomRight.vertical + outlineWidth + outlineOffset: 0};
}

static RCTBorderStyle RCTBorderStyleFromOutlineStyle(OutlineStyle outlineStyle)
{
  switch (outlineStyle) {
    case OutlineStyle::Solid:
      return RCTBorderStyleSolid;
    case OutlineStyle::Dotted:
      return RCTBorderStyleDotted;
    case OutlineStyle::Dashed:
      return RCTBorderStyleDashed;
  }
}

@interface RCTViewComponentView ()
- (void)invalidateLayer;
- (UIView *)currentContainerView;
- (BOOL)styleWouldClipOverflowInk;
@end

@interface FastSquircleView () <RCTFastSquircleViewViewProtocol>

@end

@implementation FastSquircleView {
  UIView * _view;
  
  CALayer * _squircleBackgroundLayer;
  CALayer * _squircleBorderLayer;
  
  float _cornerSmoothing;
}

+ (ComponentDescriptorProvider)componentDescriptorProvider
{
  return concreteComponentDescriptorProvider<FastSquircleViewComponentDescriptor>();
}

- (instancetype)initWithFrame:(CGRect)frame
{
  if (self = [super initWithFrame:frame]) {
    static const auto defaultProps = std::make_shared<const FastSquircleViewProps>();
    _props = defaultProps;
  }
  
  return self;
}

- (void)updateProps:(Props::Shared const &)props oldProps:(Props::Shared const &)oldProps
{
  const auto &oldViewProps = *std::static_pointer_cast<FastSquircleViewProps const>(_props);
  const auto &newViewProps = *std::static_pointer_cast<FastSquircleViewProps const>(props);
  
  if (oldViewProps.cornerSmoothing != newViewProps.cornerSmoothing) {
    _cornerSmoothing = newViewProps.cornerSmoothing;
    [self invalidateLayer];
  }
  
  [super updateProps:props oldProps:oldProps];
}

- (void)invalidateLayer
{
  [super invalidateLayer];
    
  Ivar backgroundColorLayerIvar = class_getInstanceVariable([RCTViewComponentView class], "_backgroundColorLayer");
  CALayer *backgroundColorLayer = object_getIvar(self, backgroundColorLayerIvar);
  
  Ivar borderLayerIvar = class_getInstanceVariable([RCTViewComponentView class], "_borderLayer");
  CALayer *borderLayer = object_getIvar(self, borderLayerIvar);
  
  Ivar outlineLayerIvar = class_getInstanceVariable([RCTViewComponentView class], "_outlineLayer");
  CALayer *outlineLayer = object_getIvar(self, outlineLayerIvar);
  
  Ivar boxShadowLayerIvar = class_getInstanceVariable([RCTViewComponentView class], "_boxShadowLayer");
  CALayer *boxShadowLayer = object_getIvar(self, boxShadowLayerIvar);
  
  const auto borderMetrics = _props->resolveBorderMetrics(_layoutMetrics);
  
  NSNumber *topLeftBorderRadius = [self toSingleValue:borderMetrics.borderRadii.topLeft];
  NSNumber *topRightBorderRadius = [self toSingleValue:borderMetrics.borderRadii.topRight];
  NSNumber *bottomLeftBorderRadius = [self toSingleValue:borderMetrics.borderRadii.bottomLeft];
  NSNumber *bottomRightBorderRadius = [self toSingleValue:borderMetrics.borderRadii.bottomRight];
  
  CGFloat width = self.frame.size.width;
  CGFloat height = self.frame.size.height;
  
  NSNumber *cornerSmoothing = @(_cornerSmoothing);
  
  SquircleParams *squircleParams = [[SquircleParams alloc] initWithCornerSmoothing:cornerSmoothing width:@(width) height:@(height)];
  squircleParams.topLeftCornerRadius = topLeftBorderRadius;
  squircleParams.topRightCornerRadius = topRightBorderRadius;
  squircleParams.bottomLeftCornerRadius = bottomLeftBorderRadius;
  squircleParams.bottomRightCornerRadius = bottomRightBorderRadius;
  
  UIBezierPath *squirclePath = [SquirclePathGenerator getSquirclePath:squircleParams];
  
  CAShapeLayer *maskLayer = [[CAShapeLayer alloc] init];
  maskLayer.path = squirclePath.CGPath;
  
  // background
  
  // if the RN code already added a background dedicated layer it is easier to just mask it
  if (backgroundColorLayer) {
    if (_squircleBackgroundLayer) {
      [_squircleBackgroundLayer removeFromSuperlayer];
      _squircleBackgroundLayer = nil;
    }
    
    backgroundColorLayer.mask = maskLayer;
    [backgroundColorLayer removeAllAnimations];
  } else {
    CGColor *originalBackgroundColor = self.layer.backgroundColor;
    self.layer.backgroundColor = nil;
    self.layer.cornerRadius = 0;
    
    if (!_squircleBackgroundLayer) {
      _squircleBackgroundLayer = [[CALayer alloc] init];
      _squircleBackgroundLayer.zPosition = BACKGROUND_COLOR_ZPOSITION;
      [self.layer addSublayer:_squircleBackgroundLayer];
    }
    
    _squircleBackgroundLayer.frame = CGRectMake(0, 0, self.frame.size.width, self.frame.size.height);
    _squircleBackgroundLayer.mask = maskLayer;
    _squircleBackgroundLayer.backgroundColor = originalBackgroundColor;
    [_squircleBackgroundLayer removeAllAnimations];
  }
  
  // border
  if (borderLayer) {
    if (_squircleBorderLayer) {
      [_squircleBorderLayer removeFromSuperlayer];
      _squircleBorderLayer = nil;
    }
    
    UpdateContourEffectToSquircleToLayer(
      borderLayer,
      RCTCornerRadiiFromBorderRadii(borderMetrics.borderRadii),
      RCTCreateRCTBorderColorsFromBorderColors(borderMetrics.borderColors),
      RCTUIEdgeInsetsFromEdgeInsets(borderMetrics.borderWidths),
      RCTBorderStyleFromBorderStyle(borderMetrics.borderStyles.left),
      cornerSmoothing);
  } else {
    if (!_squircleBorderLayer && self.layer.bounds.size.width > 0 && self.layer.bounds.size.height > 0) {
      CALayer *borderLayer = [CALayer new];
      borderLayer.zPosition = BACKGROUND_COLOR_ZPOSITION + 1;
      borderLayer.frame = self.layer.bounds;
      borderLayer.magnificationFilter = kCAFilterNearest;
      [self.layer addSublayer:borderLayer];
      _squircleBorderLayer = borderLayer;
    }
    
    _squircleBorderLayer.frame = self.layer.bounds;

    self.layer.borderWidth = 0;
    self.layer.borderColor = nil;
    self.layer.cornerRadius = 0;

    RCTBorderColors borderColors = RCTCreateRCTBorderColorsFromBorderColors(borderMetrics.borderColors);

    UpdateContourEffectToSquircleToLayer(
        _squircleBorderLayer,
        RCTCornerRadiiFromBorderRadii(borderMetrics.borderRadii),
        borderColors,
        RCTUIEdgeInsetsFromEdgeInsets(borderMetrics.borderWidths),
        RCTBorderStyleFromBorderStyle(borderMetrics.borderStyles.left),
        cornerSmoothing);
  }
  
  // clipping
  self.currentContainerView.layer.mask = nil;
  self.currentContainerView.layer.cornerRadius = 0;
  if (self.currentContainerView.clipsToBounds) {
    float borderWidth = borderMetrics.borderWidths.left;
    squircleParams.width = @(self.bounds.size.width - 2 * borderWidth);
    squircleParams.height = @(self.bounds.size.height - 2 * borderWidth);
    
    squircleParams.cornerRadius = @(fmax(0, squircleParams.cornerRadius.floatValue - borderWidth));
    squircleParams.topLeftCornerRadius = @(fmax(0, squircleParams.topLeftCornerRadius.floatValue - borderWidth));
    squircleParams.topRightCornerRadius = @(fmax(0, squircleParams.topRightCornerRadius.floatValue - borderWidth));
    squircleParams.bottomLeftCornerRadius = @(fmax(0, squircleParams.bottomLeftCornerRadius.floatValue - borderWidth));
    squircleParams.bottomRightCornerRadius = @(fmax(0, squircleParams.bottomRightCornerRadius.floatValue - borderWidth));
    UIBezierPath *squirclePath = [SquirclePathGenerator getSquirclePath:squircleParams];
    
    CGAffineTransform translation = CGAffineTransformMakeTranslation(borderWidth, borderWidth);
    [squirclePath applyTransform:translation];
    
    CAShapeLayer *maskLayer = [[CAShapeLayer alloc] init];
    maskLayer.path = squirclePath.CGPath;
    self.currentContainerView.layer.mask = maskLayer;
  }
  
  // outline
  if (outlineLayer) {
    UIColor *outlineColor = RCTUIColorFromSharedColor(_props->outlineColor);
    
    UpdateContourEffectToSquircleToLayer(
        outlineLayer,
        RCTCreateOutlineCornerRadiiFromBorderRadii(borderMetrics.borderRadii, _props->outlineWidth, _props->outlineOffset),
        RCTBorderColors{outlineColor, outlineColor, outlineColor, outlineColor},
        UIEdgeInsets{_props->outlineWidth, _props->outlineWidth, _props->outlineWidth, _props->outlineWidth},
        RCTBorderStyleFromOutlineStyle(_props->outlineStyle),
        cornerSmoothing);
  }
  
  // box shadow
  if (boxShadowLayer) {
    UIImage *boxShadowImage = FastSquircleGetBoxShadowImage(_props->boxShadow,
      RCTCornerRadiiFromBorderRadii(borderMetrics.borderRadii),
      RCTUIEdgeInsetsFromEdgeInsets(borderMetrics.borderWidths),
      self.layer.bounds.size,
      cornerSmoothing);
    
    boxShadowLayer.contents = (id)boxShadowImage.CGImage;
  }
}

- (BOOL)styleWouldClipOverflowInk
{
  // If the overflow is hidden we foce to use a subview for rendering, that allows for better clipping preserving borders
  return [super styleWouldClipOverflowInk] || self.currentContainerView.clipsToBounds;
}

-(NSNumber *)toSingleValue:(CornerRadii)cornerRadii
{
  return @(fmax(cornerRadii.vertical, cornerRadii.horizontal));
}

Class<RCTComponentViewProtocol> FastSquircleViewCls(void)
{
  return FastSquircleView.class;
}

@end
