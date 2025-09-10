import { Alert, Pressable, StyleSheet, View } from 'react-native';
import SquircleView from 'react-native-fast-squircle';

export default function App() {
  function onPress() {
    Alert.alert('Pressed!');
  }

  return (
    <View style={styles.container}>
      <Pressable onPress={onPress}>
        <SquircleView style={styles.box}>
          <View
            style={{ width: '100%', height: '100%', backgroundColor: 'blue' }}
          />
        </SquircleView>
      </Pressable>

      <SquircleView style={styles.box}>
        <Pressable onPress={onPress}>
          <View
            style={{ width: '100%', height: '100%', backgroundColor: 'blue' }}
          />
        </Pressable>
      </SquircleView>

      <View style={styles.box}>
        <Pressable onPress={onPress}>
          <View
            style={{ width: '100%', height: '100%', backgroundColor: 'blue' }}
          />
        </Pressable>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',

    gap: 70,
  },

  box: {
    height: 160,
    aspectRatio: 1,
    backgroundColor: 'grey',

    padding: 20,

    borderRadius: 30,

    boxShadow: '-20px -20px 0px 5px #000000AA',
  },
});
