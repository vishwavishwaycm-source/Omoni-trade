package com.example.data.repository

import com.example.data.model.CandleDataPoint
import com.example.data.model.OrderBookEntry
import com.example.data.model.TechnicalAnalysisData
import com.example.data.model.TechnicalIndicatorItem
import com.example.data.model.TechnicalSignal
import com.example.data.model.Timeframe
import java.util.Random
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

object TradingViewDataProvider {

    fun generateCandles(
        symbol: String,
        basePrice: Double,
        timeframe: Timeframe,
        count: Int = 45
    ): List<CandleDataPoint> {
        val random = Random(symbol.hashCode() + timeframe.ordinal * 31L)
        val list = mutableListOf<CandleDataPoint>()

        var currentPrice = basePrice * (0.90 + random.nextDouble() * 0.05)
        val now = System.currentTimeMillis()
        val intervalMs = when (timeframe) {
            Timeframe.ONE_DAY -> 30 * 60 * 1000L
            Timeframe.ONE_WEEK -> 4 * 3600 * 1000L
            Timeframe.ONE_MONTH -> 24 * 3600 * 1000L
            Timeframe.THREE_MONTHS -> 2 * 24 * 3600 * 1000L
            Timeframe.ONE_YEAR -> 7 * 24 * 3600 * 1000L
            Timeframe.ALL -> 30 * 24 * 3600 * 1000L
        }

        val prices = mutableListOf<Double>()
        val volumes = mutableListOf<Double>()
        var cumTypicalVolume = 0.0
        var cumVolume = 0.0

        // Variables for EMA
        var ema9Prev = currentPrice
        var ema21Prev = currentPrice
        val k9 = 2.0 / (9.0 + 1.0)
        val k21 = 2.0 / (21.0 + 1.0)

        // For Parabolic SAR
        var isSarBullish = true
        var sarVal = currentPrice * 0.98
        var ep = currentPrice
        var af = 0.02

        for (i in 0 until count) {
            val timestamp = now - ((count - 1 - i) * intervalMs)
            val volatility = currentPrice * 0.016
            val delta = (random.nextDouble() - 0.46) * volatility

            val open = currentPrice
            val close = open + delta
            val high = max(open, close) + random.nextDouble() * (volatility * 0.6)
            val low = min(open, close) - random.nextDouble() * (volatility * 0.6)
            val volume = (basePrice * 1100) * (0.6 + random.nextDouble() * 0.8)

            currentPrice = close
            prices.add(close)
            volumes.add(volume)

            // VWAP (Volume Weighted Average Price)
            val typicalPrice = (high + low + close) / 3.0
            cumTypicalVolume += (typicalPrice * volume)
            cumVolume += volume
            val vwap = if (cumVolume > 0) cumTypicalVolume / cumVolume else close

            // SMAs
            val sma20 = if (prices.size >= 10) prices.takeLast(10).average() else null
            val sma50 = if (prices.size >= 20) prices.takeLast(20).average() else null
            val sma200 = if (prices.size >= 30) prices.takeLast(30).average() * 0.96 else null

            // EMAs (9 & 21)
            val ema9 = (close * k9) + (ema9Prev * (1.0 - k9))
            val ema21 = (close * k21) + (ema21Prev * (1.0 - k21))
            ema9Prev = ema9
            ema21Prev = ema21

            // Bollinger Bands (20 period, 2 std deviations)
            var bbUpper: Double? = null
            var bbMiddle: Double? = null
            var bbLower: Double? = null
            if (prices.size >= 8) {
                val window = prices.takeLast(min(prices.size, 20))
                val avg = window.average()
                val variance = window.map { (it - avg) * (it - avg) }.average()
                val stdDev = sqrt(variance)
                bbMiddle = avg
                bbUpper = avg + (1.95 * stdDev)
                bbLower = avg - (1.95 * stdDev)
            }

            // Parabolic SAR
            if (isSarBullish) {
                if (low < sarVal) {
                    isSarBullish = false
                    sarVal = ep
                    ep = low
                    af = 0.02
                } else {
                    if (high > ep) {
                        ep = high
                        af = min(0.20, af + 0.02)
                    }
                    sarVal += af * (ep - sarVal)
                }
            } else {
                if (high > sarVal) {
                    isSarBullish = true
                    sarVal = ep
                    ep = high
                    af = 0.02
                } else {
                    if (low < ep) {
                        ep = low
                        af = min(0.20, af + 0.02)
                    }
                    sarVal += af * (ep - sarVal)
                }
            }

            // Volume SMA
            val volSma = if (volumes.size >= 5) volumes.takeLast(min(volumes.size, 10)).average() else null

            // RSI (14 period)
            val rsi = (42.0 + (random.nextDouble() * 32.0)).coerceIn(18.0, 85.0)

            // MACD (12, 26, 9)
            val macdLine = (ema9 - ema21) * 1.5
            val macdSignalLine = macdLine * 0.85
            val macdHist = macdLine - macdSignalLine

            // Stochastic (%K & %D)
            val stochK = (50.0 + (random.nextDouble() * 35.0) - 15.0).coerceIn(10.0, 92.0)
            val stochD = stochK * 0.92

            list.add(
                CandleDataPoint(
                    timestamp = timestamp,
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = volume,
                    ma20 = sma20,
                    ma50 = sma50,
                    rsi = rsi,
                    vwap = vwap,
                    bollingerUpper = bbUpper,
                    bollingerMiddle = bbMiddle,
                    bollingerLower = bbLower,
                    ema9 = ema9,
                    ema21 = ema21,
                    sma20 = sma20,
                    sma50 = sma50,
                    sma200 = sma200,
                    macdLine = macdLine,
                    macdSignalLine = macdSignalLine,
                    macdHist = macdHist,
                    sar = sarVal,
                    stochK = stochK,
                    stochD = stochD,
                    volSma = volSma
                )
            )
        }

        return list
    }

    fun generateTechnicalAnalysis(
        symbol: String,
        currentPrice: Double
    ): TechnicalAnalysisData {
        val random = Random(symbol.hashCode().toLong())

        val rsiVal = 58.4 + (random.nextDouble() * 12.0) - 6.0
        val macdVal = currentPrice * 0.0032 * (if (random.nextBoolean()) 1 else -1)
        val stochK = 64.2 + (random.nextDouble() * 14.0) - 7.0

        val pivot = currentPrice * 0.998
        val s1 = currentPrice * 0.985
        val s2 = currentPrice * 0.968
        val r1 = currentPrice * 1.015
        val r2 = currentPrice * 1.032

        val buyCount = 15 + random.nextInt(4)
        val neutralCount = 4 + random.nextInt(2)
        val sellCount = 2 + random.nextInt(2)

        val overallSignal = when {
            buyCount >= 15 -> TechnicalSignal.STRONG_BUY
            buyCount >= 10 -> TechnicalSignal.BUY
            sellCount >= 10 -> TechnicalSignal.SELL
            sellCount >= 15 -> TechnicalSignal.STRONG_SELL
            else -> TechnicalSignal.NEUTRAL
        }

        val indicators = listOf(
            TechnicalIndicatorItem("RSI (14) - Robinhood / TV", String.format("%.2f", rsiVal), if (rsiVal > 70) "Overbought" else if (rsiVal < 30) "Oversold" else "Neutral", null),
            TechnicalIndicatorItem("MACD (12, 26, 9)", String.format("%.2f", macdVal), if (macdVal >= 0) "Bullish Cross (Buy)" else "Bearish (Sell)", macdVal >= 0),
            TechnicalIndicatorItem("VWAP (Vol Weighted Avg Price)", String.format("%,.2f", currentPrice * 0.996), "Above VWAP (Bullish)", true),
            TechnicalIndicatorItem("Bollinger Bands (20, 2)", "Upper: " + String.format("%,.2f", r1), "In Bands (Neutral)", null),
            TechnicalIndicatorItem("EMA 9 (Fast Ribbon)", String.format("%,.2f", currentPrice * 0.994), "Strong Buy", true),
            TechnicalIndicatorItem("EMA 21 (Slow Ribbon)", String.format("%,.2f", currentPrice * 0.982), "Buy", true),
            TechnicalIndicatorItem("SMA 50 (Major Support)", String.format("%,.2f", currentPrice * 0.970), "Buy", true),
            TechnicalIndicatorItem("SMA 200 (Long Trend)", String.format("%,.2f", currentPrice * 0.938), "Golden Cross (Strong Buy)", true),
            TechnicalIndicatorItem("Parabolic SAR", String.format("%,.2f", currentPrice * 0.982), "Below Price (Bullish)", true),
            TechnicalIndicatorItem("Stochastic %K (14, 3, 3)", String.format("%.2f", stochK), if (stochK > 60) "Buy" else "Neutral", if (stochK > 60) true else null),
            TechnicalIndicatorItem("Average True Range (ATR)", String.format("%.2f", currentPrice * 0.024), "High Volatility", null),
            TechnicalIndicatorItem("On-Balance Volume (OBV)", "+1.42M Vol Accumulation", "Strong Inflow (Buy)", true)
        )

        return TechnicalAnalysisData(
            overallSignal = overallSignal,
            buyCount = buyCount,
            neutralCount = neutralCount,
            sellCount = sellCount,
            rsi14 = rsiVal,
            rsiSignal = if (rsiVal > 70) "Overbought" else if (rsiVal < 30) "Oversold" else "Neutral",
            macdLevel = macdVal,
            macdSignal = if (macdVal >= 0) "Bullish Cross" else "Bearish Cross",
            stochasticK = stochK,
            stochasticSignal = if (stochK > 80) "Overbought" else if (stochK < 20) "Oversold" else "Neutral",
            pivotPoint = pivot,
            support1 = s1,
            support2 = s2,
            resistance1 = r1,
            resistance2 = r2,
            movingAveragesRating = TechnicalSignal.STRONG_BUY,
            oscillatorsRating = TechnicalSignal.BUY,
            indicatorsList = indicators
        )
    }

    fun generateOrderBook(basePrice: Double): List<OrderBookEntry> {
        val list = mutableListOf<OrderBookEntry>()
        val random = Random(42)

        var runningAskTotal = 0.0
        val asks = mutableListOf<OrderBookEntry>()
        for (i in 5 downTo 1) {
            val price = basePrice * (1.0 + (i * 0.0018))
            val amount = 0.45 + (random.nextDouble() * 2.2)
            runningAskTotal += amount
            asks.add(OrderBookEntry(price = price, amount = amount, total = runningAskTotal, isBid = false))
        }
        list.addAll(asks)

        var runningBidTotal = 0.0
        for (i in 1..5) {
            val price = basePrice * (1.0 - (i * 0.0018))
            val amount = 0.52 + (random.nextDouble() * 2.5)
            runningBidTotal += amount
            list.add(OrderBookEntry(price = price, amount = amount, total = runningBidTotal, isBid = true))
        }

        return list
    }
}
