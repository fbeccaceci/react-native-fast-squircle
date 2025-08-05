//
//  FastSquircleBorderDrawing.h
//  Pods
//
//  Created by Fabrizio Beccaceci on 04/08/25.
//

#import <UIKit/UIKit.h>

#import <React/RCTBorderStyle.h>
#import <React/RCTDefines.h>
#import <React/RCTBorderDrawing.h>

#ifndef FastSquircleBorderDrawing_h
#define FastSquircleBorderDrawing_h

RCT_EXTERN UIImage *FastSquircleGetBorderImage(
  RCTBorderStyle borderStyle,
  CGSize viewSize,
  RCTCornerRadii cornerRadii,
  UIEdgeInsets borderInsets,
  RCTBorderColors borderColors,
  UIColor *backgroundColor,
  BOOL drawToEdge);

#endif /* FastSquircleBorderDrawing_h */
