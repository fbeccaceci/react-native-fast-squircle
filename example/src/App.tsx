import { useEffect, useRef } from 'react';
import { Platform, StyleSheet, View } from 'react-native';
import SquircleView from 'react-native-fast-squircle';

export default function App() {
  const ref = useRef<View>(null);

  useEffect(() => {
    ref.current?.measure((x, y, width, height, pageX, pageY) => {
      console.log(`${Platform.OS} Measured:`, {
        x,
        y,
        width,
        height,
        pageX,
        pageY,
      });
    });
  }, []);

  return (
    <View style={styles.container}>
      <SquircleView style={styles.box} ref={ref}>
        {/* <View style={{ width: 50, aspectRatio: 1, backgroundColor: 'blue' }} /> */}
      </SquircleView>

      <View style={styles.box} />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },

  box: {
    width: 200,
    height: 200,
    margin: 10,

    backgroundColor: '#DDDAD0',
    borderColor: '#c2bfb6',

    borderRadius: 30,
    borderWidth: 1,

    boxShadow: '10px 5px 5px teal',

    // overflow: 'hidden',
  },
});
