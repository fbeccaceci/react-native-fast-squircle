import {
  forwardRef,
  type ComponentRef,
  type ForwardRefExoticComponent,
  type RefAttributes,
} from 'react';
import type { ViewProps, View } from 'react-native';
import FastSquircleViewNativeComponent from './FastSquircleViewNativeComponent';

// `View` is a class before RN 0.88 and a function component from 0.88 on.
// ComponentRef resolves to its instance type in both cases.
type ViewRef = ComponentRef<typeof View>;

export interface FastSquircleViewProps extends ViewProps {
  cornerSmoothing?: number;
}

// Annotated so the declaration keeps ViewRef instead of the type it resolves
// to on the RN version this package is built against.
const FastSquircleView: ForwardRefExoticComponent<
  FastSquircleViewProps & RefAttributes<ViewRef>
> = forwardRef<ViewRef, FastSquircleViewProps>(
  ({ cornerSmoothing = 0.6, ...props }, ref) => {
    if (cornerSmoothing < 0 || cornerSmoothing > 1) {
      throw new Error(
        'cornerSmoothing must be between 0 and 1, inclusive. Received: ' +
          cornerSmoothing
      );
    }

    return (
      <FastSquircleViewNativeComponent
        // Before RN 0.88 the View instance type differs from the native
        // component's; from 0.88 on they match. @ts-expect-error would fail
        // there as unused, so this has to be @ts-ignore.
        // @ts-ignore
        ref={ref}
        cornerSmoothing={cornerSmoothing}
        {...props}
      />
    );
  }
);

FastSquircleView.displayName = 'FastSquircleView';

export default FastSquircleView;
