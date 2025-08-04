#import "FastSquircleView.h"

#import <react/renderer/components/FastSquircleViewSpec/ComponentDescriptors.h>
#import <react/renderer/components/FastSquircleViewSpec/EventEmitters.h>
#import <react/renderer/components/FastSquircleViewSpec/Props.h>
#import <react/renderer/components/FastSquircleViewSpec/RCTComponentViewHelpers.h>

#import "RCTFabricComponentsPlugins.h"

#import "FastSquircle-Swift.h"
#import <objc/runtime.h>

using namespace facebook::react;

@interface RCTViewComponentView ()
- (void)invalidateLayer;
@end

@interface FastSquircleView () <RCTFastSquircleViewViewProtocol>

@end

@implementation FastSquircleView {
  UIView * _view;
  ViewLayersUpdater * _viewLayersUpdater;
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
    
    _viewLayersUpdater = [[ViewLayersUpdater alloc] init];
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
  
  UpdateLayersParams *params = [[UpdateLayersParams alloc] initWithView:self];
  
  Ivar backgroundColorLayerIvar = class_getInstanceVariable([RCTViewComponentView class], "_backgroundColorLayer");
  CALayer *backgroundColorLayer = object_getIvar(self, backgroundColorLayerIvar);
  
  params.backgroundColorLayer = backgroundColorLayer;
  
//  const auto &viewProps = *std::static_pointer_cast<FastSquircleViewProps const>(self.props);
  
  const auto borderMetrics = _props->resolveBorderMetrics(_layoutMetrics);
  
  if (borderMetrics.borderRadii.isUniform()) {
    params.cornerRadius = [self toSingleValue:borderMetrics.borderRadii.topLeft];
  } else {
    params.topLeftCornerRadius = [self toSingleValue:borderMetrics.borderRadii.topLeft];
    params.topRightCornerRadius = [self toSingleValue:borderMetrics.borderRadii.topRight];
    params.bottomLeftCornerRadius = [self toSingleValue:borderMetrics.borderRadii.bottomLeft];
    params.bottomRightCornerRadius = [self toSingleValue:borderMetrics.borderRadii.bottomRight];
  }

  params.cornerSmoothing = 0.6;
  
  [_viewLayersUpdater updateLayersWithParams:params];
}

-(float)toSingleValue:(CornerRadii)cornerRadii
{
  return fmax(cornerRadii.vertical, cornerRadii.horizontal);
}

Class<RCTComponentViewProtocol> FastSquircleViewCls(void)
{
  return FastSquircleView.class;
}

@end
