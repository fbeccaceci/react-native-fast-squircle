import { StyleSheet, Text, View } from 'react-native';
import SquircleView from 'react-native-fast-squircle';

export default function App() {
  return (
    <View style={styles.container}>
      <View style={styles.textContainer}>
        <Text style={styles.label}>Squircle</Text>
        <SquircleView style={styles.box} cornerSmoothing={1} />
      </View>

      <View style={styles.textContainer}>
        <Text style={styles.label}>Normal View</Text>
        <View style={styles.box} />
      </View>

      <Text style={styles.caption}>
        Notice how the squircle has a smooth, rounded appearance while the
        normal view has sharp corners.
      </Text>
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

  textContainer: {
    alignItems: 'center',
    gap: 10,
  },

  label: {
    fontSize: 16,
    fontWeight: '500',
  },

  caption: {
    fontSize: 14,
    fontWeight: '400',
    textAlign: 'center',
    width: '80%',
  },

  box: {
    height: 160,
    aspectRatio: 1,
    // backgroundColor: '#C1D7EA',
    backgroundColor: 'red',

    padding: 20,

    borderWidth: 8,
    // borderColor: '#3A86FF',
    borderRadius: 30,
    borderTopRightRadius: 20,

    borderRightColor: 'blue',

    // boxShadow: '25px 25px 0px 0px #000000',
  },
});
