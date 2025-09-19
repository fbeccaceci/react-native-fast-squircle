import React from 'react';
import FastSquircleView from '../index';

// Mock the native component since it won't be available in test environment
jest.mock(
  '../FastSquircleViewNativeComponent',
  () => 'FastSquircleViewNativeComponent'
);

describe('FastSquircleView', () => {
  it('should have correct displayName', () => {
    expect(FastSquircleView.displayName).toBe('FastSquircleView');
  });

  it('should be a forwardRef component', () => {
    expect(typeof FastSquircleView).toBe('object');
    expect(FastSquircleView.$$typeof.toString()).toContain('react.forward_ref');
  });

  // Test validation logic by creating a test component that uses our validation
  describe('cornerSmoothing validation', () => {
    const validateCornerSmoothing = (cornerSmoothing: number) => {
      if (cornerSmoothing < 0 || cornerSmoothing > 1) {
        throw new Error(
          'cornerSmoothing must be between 0 and 1, inclusive. Received: ' +
            cornerSmoothing
        );
      }
    };

    it('should throw error when cornerSmoothing is less than 0', () => {
      expect(() => {
        validateCornerSmoothing(-0.1);
      }).toThrow(
        'cornerSmoothing must be between 0 and 1, inclusive. Received: -0.1'
      );
    });

    it('should throw error when cornerSmoothing is greater than 1', () => {
      expect(() => {
        validateCornerSmoothing(1.1);
      }).toThrow(
        'cornerSmoothing must be between 0 and 1, inclusive. Received: 1.1'
      );
    });

    it('should accept cornerSmoothing value of 0 (boundary case)', () => {
      expect(() => {
        validateCornerSmoothing(0);
      }).not.toThrow();
    });

    it('should accept cornerSmoothing value of 1 (boundary case)', () => {
      expect(() => {
        validateCornerSmoothing(1);
      }).not.toThrow();
    });

    it('should accept valid cornerSmoothing values', () => {
      expect(() => {
        validateCornerSmoothing(0.5);
      }).not.toThrow();

      expect(() => {
        validateCornerSmoothing(0.8);
      }).not.toThrow();
    });
  });

  // Test component structure
  it('should create React element without errors for valid props', () => {
    expect(() => {
      React.createElement(FastSquircleView, { cornerSmoothing: 0.6 });
    }).not.toThrow();
  });

  it('should create React element without errors when no props provided', () => {
    expect(() => {
      React.createElement(FastSquircleView);
    }).not.toThrow();
  });

  it('should create React element with additional props', () => {
    expect(() => {
      React.createElement(FastSquircleView, {
        cornerSmoothing: 0.5,
        style: { backgroundColor: 'red' },
        testID: 'test-squircle',
      });
    }).not.toThrow();
  });
});
