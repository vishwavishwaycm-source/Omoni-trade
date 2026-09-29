package com.example.data.model

data class CandleDataPoint(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double,
    val ma20: Double? = null,
    val ma50: Double? = null,
    val rsi: Double? = null,
    // Robinhood & TradingView Indicators
    val vwap: Double? = null,
    val bollingerUpper: Double? = null,
    val bollingerMiddle: Double? = null,
    val bollingerLower: Double? = null,
    val ema9: Double? = null,
    val ema21: Double? = null,
    val sma20: Double? = null,
    val sma50: Double? = null,
    val sma200: Double? = null,
    val macdLine: Double? = null,
    val macdSignalLine: Double? = null,
    val macdHist: Double? = null,
    val sar: Double? = null, // Parabolic SAR
    val stochK: Double? = null,
    val stochD: Double? = null,
    val volSma: Double? = null
)

data class ActiveIndicatorsConfig(
    val showVwap: Boolean = false,
    val showBollinger: Boolean = true,
    val showEma: Boolean = true, // EMA 9 & EMA 21
    val showSma: Boolean = false, // SMA 20 & SMA 50
    val showParabolicSar: Boolean = false,
    val showVolume: Boolean = true,
    val showRsi: Boolean = true,
    val showMacd: Boolean = true,
    val showStochastic: Boolean = false
)

enum class Timeframe(val label: String, val days: Int) {
    ONE_DAY("1D", 1),
    ONE_WEEK("1W", 7),
    ONE_MONTH("1M", 30),
    THREE_MONTHS("3M", 90),
    ONE_YEAR("1Y", 365),
    ALL("ALL", 1000)
}

enum class ChartStyle(val label: String) {
    CANDLESTICK("Candles"),
    LINE("Line"),
    AREA("Area"),
    LIVE_WEB("Live Web")
}

enum class TechnicalSignal(val label: String, val colorHex: Long) {
    STRONG_BUY("STRONG BUY", 0xFF059669),
    BUY("BUY", 0xFF10B981),
    NEUTRAL("NEUTRAL", 0xFF94A3B8),
    SELL("SELL", 0xFFEF4444),
    STRONG_SELL("STRONG SELL", 0xFFDC2626)
}

data class TechnicalIndicatorItem(
    val name: String,
    val value: String,
    val action: String, // Buy, Sell, Neutral
    val isBullish: Boolean? // true = green, false = red, null = gray
)

data class TechnicalAnalysisData(
    val overallSignal: TechnicalSignal,
    val buyCount: Int,
    val neutralCount: Int,
    val sellCount: Int,
    val rsi14: Double,
    val rsiSignal: String,
    val macdLevel: Double,
    val macdSignal: String,
    val stochasticK: Double,
    val stochasticSignal: String,
    val pivotPoint: Double,
    val support1: Double,
    val support2: Double,
    val resistance1: Double,
    val resistance2: Double,
    val movingAveragesRating: TechnicalSignal,
    val oscillatorsRating: TechnicalSignal,
    val indicatorsList: List<TechnicalIndicatorItem>
)

data class OrderBookEntry(
    val price: Double,
    val amount: Double,
    val total: Double,
    val isBid: Boolean // true = green bid, false = red ask
)
