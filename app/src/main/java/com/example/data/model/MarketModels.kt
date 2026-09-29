package com.example.data.model

data class StockIndex(
    val symbol: String,
    val name: String,
    val region: String,
    val country: String,
    val flag: String,
    val price: Double,
    val changePoints: Double,
    val changePercent: Double,
    val isOpen: Boolean,
    val currency: String,
    val sparkline: List<Float>
)

data class CryptoAsset(
    val symbol: String,
    val name: String,
    val priceUsd: Double,
    val change24hPercent: Double,
    val marketCapFormatted: String,
    val volume24hFormatted: String,
    val high24h: Double,
    val low24h: Double,
    val sparkline: List<Float>
)

data class GlobalMarketOverview(
    val globalCryptoMarketCap: String = "$2.68T",
    val cryptoMarketCapChange24h: Double = 2.45,
    val btcDominance: Double = 56.2,
    val fearGreedIndex: Int = 68,
    val fearGreedLabel: String = "Greed",
    val lastUpdated: Long = System.currentTimeMillis()
)
