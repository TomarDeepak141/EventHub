import { describe, expect, it } from 'vitest';
import { resolveApiBaseUrl } from './http';

describe('resolveApiBaseUrl', () => {
  it('keeps the local same-origin API proxy prefix', () => {
    expect(resolveApiBaseUrl('/api')).toBe('/api');
  });

  it('removes the local proxy prefix from an absolute backend URL', () => {
    expect(resolveApiBaseUrl('https://backend.example.test/api')).toBe('https://backend.example.test');
    expect(resolveApiBaseUrl('https://backend.example.test/api/')).toBe('https://backend.example.test');
  });

  it('preserves an absolute backend URL that has no proxy prefix', () => {
    expect(resolveApiBaseUrl('https://backend.example.test')).toBe('https://backend.example.test');
  });
});
