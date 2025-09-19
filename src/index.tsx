import { forwardRef } from 'react';
import type { ViewProps } from 'react-native';
import FastSquircleViewNativeComponent from './FastSquircleViewNativeComponent';

export interface FastSquircleViewProps extends ViewProps {
  cornerSmoothing?: number;
}

const FastSquircleView = forwardRef<
  React.ComponentRef<typeof FastSquircleViewNativeComponent>,
  FastSquircleViewProps
>(({ cornerSmoothing = 0.6, ...props }, ref) => {
  if (cornerSmoothing < 0 || cornerSmoothing > 1) {
    throw new Error(
      'cornerSmoothing must be between 0 and 1, inclusive. Received: ' +
        cornerSmoothing
    );
  }

  return (
    <FastSquircleViewNativeComponent
      ref={ref}
      cornerSmoothing={cornerSmoothing}
      {...props}
    />
  );
});

FastSquircleView.displayName = 'FastSquircleView';

export default FastSquircleView;
