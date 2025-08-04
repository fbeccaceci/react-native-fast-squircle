import { View, StyleSheet } from 'react-native';
import { FastSquircleView } from 'react-native-fast-squircle';

export default function App() {
  return (
    <View style={styles.container}>
      <FastSquircleView style={styles.box}>
        <View style={styles.innerBox} />
      </FastSquircleView>

      <View style={styles.box}>
        <View style={styles.innerBox} />
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
    height: 200,
    marginVertical: 20,

    backgroundColor: 'blue',

    borderRadius: 40,
    // borderTopLeftRadius: 20,
    // borderTopRightRadius: 80,
  },

  innerBox: {
    width: '100%',
    height: '100%',
    // backgroundColor: 'red',
  },
});
