import codegenNativeComponent from 'react-native/Libraries/Utilities/codegenNativeComponent';
import type { ViewProps } from 'react-native';
import type { Float } from 'react-native/Libraries/Types/CodegenTypes';

interface NativeProps extends ViewProps {
  cornerSmoothing: Float;
}

export default codegenNativeComponent<NativeProps>('FastSquircleView');
