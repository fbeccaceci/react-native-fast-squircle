import { StyleSheet, Text, View } from 'react-native';
import SquircleView from 'react-native-fast-squircle';

export default function App() {
  return (
    <View style={styles.container}>
      <Text>Expo SDK 58 / React Native 0.88</Text>
      <SquircleView style={styles.box} cornerSmoothing={1}>
        <Text>Squircle</Text>
      </SquircleView>
      <View style={styles.box}>
        <Text>Normal View</Text>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,

    alignItems: 'center',
    justifyContent: 'center',
    gap: 32,

    backgroundColor: '#FFFFFF',
  },
  box: {
    width: 160,
    height: 160,

    alignItems: 'center',
    justifyContent: 'center',

    borderRadius: 30,
    backgroundColor: '#C1D7EA',
  },
});
