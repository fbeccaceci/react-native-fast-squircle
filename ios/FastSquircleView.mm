#import "FastSquircleView.h"

#import <react/renderer/components/FastSquircleViewSpec/ComponentDescriptors.h>
#import <react/renderer/components/FastSquircleViewSpec/EventEmitters.h>
#import <react/renderer/components/FastSquircleViewSpec/Props.h>
#import <react/renderer/components/FastSquircleViewSpec/RCTComponentViewHelpers.h>

#import "RCTFabricComponentsPlugins.h"

using namespace facebook::react;

@interface FastSquircleView () <RCTFastSquircleViewViewProtocol>

@end

@implementation FastSquircleView {
    UIView * _view;
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
    const auto &oldViewProps = *std::static_pointer_cast<FastSquircleViewProps const>(_props);
    const auto &newViewProps = *std::static_pointer_cast<FastSquircleViewProps const>(props);

    [super updateProps:props oldProps:oldProps];
}

Class<RCTComponentViewProtocol> FastSquircleViewCls(void)
{
    return FastSquircleView.class;
}

- hexStringToColor:(NSString *)stringToConvert
{
    NSString *noHashString = [stringToConvert stringByReplacingOccurrencesOfString:@"#" withString:@""];
    NSScanner *stringScanner = [NSScanner scannerWithString:noHashString];

    unsigned hex;
    if (![stringScanner scanHexInt:&hex]) return nil;
    int r = (hex >> 16) & 0xFF;
    int g = (hex >> 8) & 0xFF;
    int b = (hex) & 0xFF;

    return [UIColor colorWithRed:r / 255.0f green:g / 255.0f blue:b / 255.0f alpha:1.0f];
}

@end
