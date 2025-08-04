#import "FastSquircleView.h"

#import <react/renderer/components/FastSquircleViewSpec/ComponentDescriptors.h>
#import <react/renderer/components/FastSquircleViewSpec/EventEmitters.h>
#import <react/renderer/components/FastSquircleViewSpec/Props.h>
#import <react/renderer/components/FastSquircleViewSpec/RCTComponentViewHelpers.h>

#import "RCTFabricComponentsPlugins.h"

#import "FastSquircle-Swift.h"
#import <objc/runtime.h>

using namespace facebook::react;

const CGFloat BACKGROUND_COLOR_ZPOSITION = -1024.0f;

@interface RCTViewComponentView ()
- (void)invalidateLayer;
@end

@interface FastSquircleView () <RCTFastSquircleViewViewProtocol>

@end

@implementation FastSquircleView {
  UIView * _view;
  
  CALayer * _squircleBackgroundLayer;
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
    
    _view = [[UIView alloc] init];
    
    self.contentView = _view;
  }
  
  return self;
}

- (void)updateProps:(Props::Shared const &)props oldProps:(Props::Shared const &)oldProps
{
  //    const auto &oldViewProps = *std::static_pointer_cast<FastSquircleViewProps const>(_props);
  //    const auto &newViewProps = *std::static_pointer_cast<FastSquircleViewProps const>(props);
  
  [super updateProps:props oldProps:oldProps];
}

- (void)invalidateLayer
{
  [super invalidateLayer];
    
  Ivar backgroundColorLayerIvar = class_getInstanceVariable([RCTViewComponentView class], "_backgroundColorLayer");
  CALayer *backgroundColorLayer = object_getIvar(self, backgroundColorLayerIvar);
  
  const auto borderMetrics = _props->resolveBorderMetrics(_layoutMetrics);
  
  NSNumber *topLeftBorderRadius = [self toSingleValue:borderMetrics.borderRadii.topLeft];
  NSNumber *topRightBorderRadius = [self toSingleValue:borderMetrics.borderRadii.topRight];
  NSNumber *bottomLeftBorderRadius = [self toSingleValue:borderMetrics.borderRadii.bottomLeft];
  NSNumber *bottomRightBorderRadius = [self toSingleValue:borderMetrics.borderRadii.bottomRight];
  
  CGFloat width = self.frame.size.width;
  CGFloat height = self.frame.size.height;
  SquircleParams *squircleParams = [[SquircleParams alloc] initWithCornerSmoothing:@0.6 width:@(width) height:@(height)];
  squircleParams.topLeftCornerRadius = topLeftBorderRadius;
  squircleParams.topRightCornerRadius = topRightBorderRadius;
  squircleParams.bottomLeftCornerRadius = bottomLeftBorderRadius;
  squircleParams.bottomRightCornerRadius = bottomRightBorderRadius;
  
  UIBezierPath *squirclePath = [SquirclePathGenerator getSquirclePath:squircleParams];
  
  CAShapeLayer *maskLayer = [[CAShapeLayer alloc] init];
  maskLayer.path = squirclePath.CGPath;
  
  
  // border
  
  // if the RN code already added a background dedicated layer it is easier to just mask it
  if (backgroundColorLayer) {
    if (_squircleBackgroundLayer) {
      [_squircleBackgroundLayer removeFromSuperlayer];
    }
    
    backgroundColorLayer.mask = maskLayer;
  } else {
    CGColor *originalBackgroundColor = self.layer.backgroundColor;
    self.layer.backgroundColor = nil;
    self.layer.cornerRadius = 0;
    
    if (!_squircleBackgroundLayer) {
      _squircleBackgroundLayer = [[CALayer alloc] init];
      _squircleBackgroundLayer.zPosition = BACKGROUND_COLOR_ZPOSITION;
      [self.layer addSublayer:_squircleBackgroundLayer];
    }
    
    _squircleBackgroundLayer.frame = CGRectMake(0, 0, self.frame.size.width, self.frame.size.width);
    _squircleBackgroundLayer.mask = maskLayer;
    _squircleBackgroundLayer.backgroundColor = originalBackgroundColor;
    [_squircleBackgroundLayer removeAllAnimations];
  }
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
