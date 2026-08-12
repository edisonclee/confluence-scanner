# Confluence Scanner Architecture

## Current Architecture

```
React (Future)
        │
        ▼
Spring Boot REST API
        │
        ▼
ScannerController
        │
        ▼
ScannerService
        │
        ▼
ScannerEngine
        │
        ▼
MarketDataLoader
        │
        ▼
Binance Futures API
```

---

## Current Scanner

- Bollinger Band Touch Scanner

Current strategy:

- Heiken Ashi candles
- Higher timeframe Bollinger Bands
- BB Width sorting

---

## Future Scanners

- RSI
- MACD
- Volume
- Open Interest
- Funding
- EMA