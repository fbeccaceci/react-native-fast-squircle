import { forwardRef } from 'react';
import type { ViewProps, View } from 'react-native';
import FastSquircleViewNativeComponent from './FastSquircleViewNativeComponent';

export interface FastSquircleViewProps extends ViewProps {
  cornerSmoothing?: number;
}

const FastSquircleView = forwardRef<View, FastSquircleViewProps>(
  ({ cornerSmoothing = 0.6, ...props }, ref) => {
    if (cornerSmoothing < 0 || cornerSmoothing > 1) {
      throw new Error(
        'cornerSmoothing must be between 0 and 1, inclusive. Received: ' +
          cornerSmoothing
      );
    }

    return (
      <FastSquircleViewNativeComponent
        // @ts-expect-error - Native component expects a ref
        // There is a problem with the type of the NativeComponent, could probably be resolved
        // not really a priority right now
        ref={ref}
        cornerSmoothing={cornerSmoothing}
        {...props}
      />
    );
  }
);

FastSquircleView.displayName = 'FastSquircleView';

export default FastSquircleView;
