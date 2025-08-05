import { StyleSheet, ScrollView, View } from 'react-native';
import { FastSquircleView } from 'react-native-fast-squircle';
import { SquircleView } from 'react-native-figma-squircle';

const data = new Array(1000).fill(0).map((_, i) => ({
  id: i,
  title: `Item ${i + 1}`,
}));

export default function App() {
  return (
    <ScrollView
      style={styles.container}
      contentContainerStyle={styles.scrollableContainer}
    >
      {/* <FastSquircleViewRenderer /> */}
      <SVGRenderer />
    </ScrollView>
  );
}

function FastSquircleViewRenderer() {
  return (
    <View style={styles.boxesContainer}>
      {data.map((item) => (
        <FastSquircleView
          key={item.id}
          style={styles.box}
          cornerSmoothing={1}
        />
      ))}
    </View>
  );
}

function SVGRenderer() {
  return (
    <View style={styles.boxesContainer}>
      {data.map((item) => (
        <SquircleView
          key={item.id}
          style={styles.box}
          squircleParams={{
            cornerSmoothing: 1,
            cornerRadius: 10,
            fillColor: '#DDDAD0',
          }}
        />
      ))}
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },

  scrollableContainer: {
    paddingVertical: 50,
  },

  boxesContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'center',
  },

  box: {
    width: 40,
    height: 40,
    margin: 10,

    backgroundColor: '#DDDAD0',

    borderRadius: 10,
  },
});
