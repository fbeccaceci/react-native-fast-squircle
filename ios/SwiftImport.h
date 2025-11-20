//
//  SwiftImport.h
//  Pods
//
//  Created by Fabrizio Beccaceci on 19/11/25.
//

#ifndef SwiftImport_h
#define SwiftImport_h

// #import "FastSquircle-Swift.h"
// #import <FastSquircle/FastSquircle-Swift.h>

#if __has_include(<FastSquircle/FastSquircle-Swift.h>)
#import <FastSquircle/FastSquircle-Swift.h>
#elif __has_include("FastSquircle-Swift.h")
#import "FastSquircle-Swift.h"
#else
#error "Swift bridging header not found"
#endif

#endif /* SwiftImport_h */