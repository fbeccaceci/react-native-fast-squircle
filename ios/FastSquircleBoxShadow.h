//
//  FastSquircleBoxShadow.h
//  Pods
//
//  Created by Fabrizio Beccaceci on 05/08/25.
//

#import <UIKit/UIKit.h>
#import <React/RCTBoxShadow.h>

#ifndef FastSquircleBoxShadow_h
#define FastSquircleBoxShadow_h

using namespace facebook::react;

UIImage *FastSquircleGetBoxShadowImage(
  const std::vector<BoxShadow> &shadows,
  RCTCornerRadii cornerRadii,
  UIEdgeInsets edgeInsets,
  CGSize layerSize,
  NSNumber *cornerSmoothing);

#endif /* FastSquircleBoxShadow_h */
