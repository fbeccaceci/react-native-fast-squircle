const path = require('path');
const pkg = require('../package.json');

module.exports = {
  dependencies: {
    [pkg.name]: {
      root: path.resolve(__dirname, '..'),
      platforms: {
        ios: {},
        android: {},
      },
    },
  },
};
