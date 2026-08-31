import { describe, it, expect } from 'vitest'
import { formatCurrency, formatCurrencyCompact, formatPercent } from './format'

describe('formatCurrency', () => {
  it('formats a positive amount as INR with two decimal places', () => {
    expect(formatCurrency(1234.5)).toBe('₹1,234.50')
  })

  it('formats zero correctly', () => {
    expect(formatCurrency(0)).toBe('₹0.00')
  })
})

describe('formatCurrencyCompact', () => {
  it('rounds to whole rupees with no decimal places', () => {
    expect(formatCurrencyCompact(1234.5)).toBe('₹1,235')
  })
})

describe('formatPercent', () => {
  it('prefixes a positive value with a plus sign', () => {
    expect(formatPercent(12.34)).toBe('+12.3%')
  })

  it('does not double up the minus sign on negative values', () => {
    expect(formatPercent(-5.6)).toBe('-5.6%')
  })

  it('treats zero as non-negative (prefixes with +)', () => {
    expect(formatPercent(0)).toBe('+0.0%')
  })
})
