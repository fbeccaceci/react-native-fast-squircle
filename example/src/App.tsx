import { StyleSheet, View } from 'react-native';
import { FastSquircleView } from 'react-native-fast-squircle';

export default function App() {
  return (
    <View style={styles.container}>
      <FastSquircleView style={styles.box} cornerSmoothing={1}>
        {/* <View style={{ width: 50, aspectRatio: 1, backgroundColor: 'blue' }} /> */}
      </FastSquircleView>
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

    boxShadow: '10px 5px 5px purple',

    // overflow: 'hidden',
  },
});
