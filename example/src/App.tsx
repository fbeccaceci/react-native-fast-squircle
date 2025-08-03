import { View, StyleSheet } from 'react-native';
import { FastSquircleView } from 'react-native-fast-squircle';

export default function App() {
  return (
    <View style={styles.container}>
      <FastSquircleView style={styles.box} />
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
  },
});
