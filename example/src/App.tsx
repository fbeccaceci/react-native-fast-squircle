import { View, StyleSheet } from 'react-native';
import { FastSquircleView } from 'react-native-fast-squircle';

export default function App() {
  return (
    <View style={styles.container}>
      <FastSquircleView style={[styles.box]} cornerSmoothing={1} />
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

    backgroundColor: '#DDDAD0',

    borderRadius: 40,
    // borderTopLeftRadius: 100,
    // borderTopRightRadius: 100,

    borderWidth: 1,
    borderColor: '#bfbfae',

    boxShadow: '10px 10px 5px red',
  },

  innerBox: {
    width: '100%',
    height: '100%',
    // backgroundColor: 'red',
  },
});
