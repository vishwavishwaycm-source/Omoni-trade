package com.example.data.model

enum class FinancialAnalysisModel(
    val title: String,
    val subtitle: String,
    val badge: String,
    val riskProfile: String,
    val targetHorizon: String,
    val description: String
) {
    QUANT_MOMENTUM(
        title = "Quantitative Momentum Model",
        subtitle = "VWAP, MACD & EMA Ribbon Trend Following",
        badge = "FAST",
        riskProfile = "High Growth / Active",
        targetHorizon = "1 - 14 Days",
        description = "Detects aggressive institutional liquidity velocity, EMA 9/21 ribbon expansions, and positive MACD histogram divergence for high-momentum swing opportunities."
    ),
    MEAN_REVERSION(
        title = "Bollinger Volatility Reversion Model",
        subtitle = "Statistical Standard Deviation Bounce",
        badge = "STAT-ARB",
        riskProfile = "Balanced / Moderate",
        targetHorizon = "3 - 30 Days",
        description = "Capitalizes on 2-standard-deviation Bollinger Band touches, RSI extremes (<30 oversold, >70 overbought), and candlestick wick rejections back toward the 20-period mean."
    ),
    MACRO_VALUE(
        title = "Macro Wealth Preservation Model",
        subtitle = "SMA 200 & Cash Flow Safeguard",
        badge = "LONG TERM",
        riskProfile = "Low Risk / Conservative",
        targetHorizon = "3 - 12 Months",
        description = "Prioritizes capital defense by aligning portfolio holdings above the 200-day simple moving average, optimizing debt-to-asset ratios, and minimizing drawdown."
    ),
    AI_NEURAL(
        title = "AI Neural Market Intelligence",
        subtitle = "Multi-Indicator Confluence Synthesizer",
        badge = "AI PRO",
        riskProfile = "Adaptive Dynamic",
        targetHorizon = "Intraday to Multi-Week",
        description = "Synthesizes multi-pane technical indicators, global market sentiment, and order book depth into an actionable confluence score with algorithmic confidence."
    )
}

data class ModelInsight(
    val model: FinancialAnalysisModel,
    val confidenceScore: Int, // 0 - 100%
    val recommendation: String,
    val entryTarget: String,
    val stopLossLevel: String,
    val targetProfit: String,
    val keySignals: List<String>,
    val riskRewardSummary: String
)
