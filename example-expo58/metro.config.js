const path = require('path');
const { getDefaultConfig } = require('expo/metro-config');

const config = getDefaultConfig(__dirname);

config.resolver.resolveRequest = (context, moduleName, platform) => {
  if (moduleName === 'react-native-fast-squircle') {
    return context.resolveRequest(
      context,
      path.resolve(__dirname, '../src/index.tsx'),
      platform
    );
  }

  // The library is outside this app. Use this app's React and React Native.
  if (/^(react|react-native)(\/|$)/.test(moduleName)) {
    return context.resolveRequest(
      { ...context, originModulePath: path.join(__dirname, 'index.ts') },
      moduleName,
      platform
    );
  }

  return context.resolveRequest(context, moduleName, platform);
};

module.exports = config;
