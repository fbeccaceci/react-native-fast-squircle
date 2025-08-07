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
        <View
          style={{ width: '100%', height: '100%', backgroundColor: 'blue' }}
        />
      </SquircleView>

      <View style={styles.box}>
        <View
          style={{ width: '100%', height: '100%', backgroundColor: 'blue' }}
        />
      </View>
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
    height: 300,
    margin: 10,

    backgroundColor: '#DDDAD0',

    borderRadius: 20,
    // borderTopRightRadius: 40,

    overflow: 'hidden',

    // padding: 30,

    borderWidth: 4,
    borderColor: 'red',

    // outlineWidth: 4,
    // outlineColor: 'green',

    // boxShadow: '0 0 10px rgba(0, 0, 0, 0.5)',
  },
});
