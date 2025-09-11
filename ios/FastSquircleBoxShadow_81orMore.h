//
//  FastSquircleBoxShadow.h
//  Pods
//
//  Created by Fabrizio Beccaceci on 05/08/25.
//

#import <UIKit/UIKit.h>
#import <React/RCTBoxShadow.h>
#import <vector>
#import <React/RCTBorderDrawing.h>
#import <React/RCTDefines.h>
#import <UIKit/UIKit.h>
#import <react/renderer/graphics/BoxShadow.h>

#ifndef FastSquircleBoxShadow_h
#define FastSquircleBoxShadow_h

using namespace facebook::react;

RCT_EXTERN CALayer *FastSquircleGetBoxShadowLayer(
    const facebook::react::BoxShadow &shadow,
    RCTCornerRadii cornerRadii,
    UIEdgeInsets edgeInsets,
    CGSize layerSize,
    NSNumber *cornerSmoothing);

#endif /* FastSquircleBoxShadow_h */
