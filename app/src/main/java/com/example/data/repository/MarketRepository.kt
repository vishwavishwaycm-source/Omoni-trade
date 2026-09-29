package com.example.data.repository

import com.example.data.model.CryptoAsset
import com.example.data.model.GlobalMarketOverview
import com.example.data.model.StockIndex
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class MarketRepository {

    private val _stockIndices = MutableStateFlow(getInitialStockIndices())
    val stockIndices: Flow<List<StockIndex>> = _stockIndices.asStateFlow()

    private val _cryptoAssets = MutableStateFlow(getInitialCryptoAssets())
    val cryptoAssets: Flow<List<CryptoAsset>> = _cryptoAssets.asStateFlow()

    private val _marketOverview = MutableStateFlow(GlobalMarketOverview())
    val marketOverview: Flow<GlobalMarketOverview> = _marketOverview.asStateFlow()

    suspend fun refreshMarkets() {
        // Slightly simulate real-time price fluctuation
        _stockIndices.value = _stockIndices.value.map { index ->
            val deltaPercent = (Random.nextDouble(-0.35, 0.45))
            val newChange = (index.changePercent + deltaPercent).coerceIn(-4.0, 5.0)
            val newPrice = index.price * (1.0 + (deltaPercent / 100.0))
            val newPoints = newPrice - (newPrice / (1.0 + (newChange / 100.0)))
            val updatedSparkline = index.sparkline.drop(1) + (newPrice.toFloat())
            index.copy(
                price = (newPrice * 100).toLong() / 100.0,
                changePercent = (newChange * 100).toLong() / 100.0,
                changePoints = (newPoints * 100).toLong() / 100.0,
                sparkline = updatedSparkline
            )
        }

        _cryptoAssets.value = _cryptoAssets.value.map { crypto ->
            val deltaPercent = (Random.nextDouble(-0.8, 1.1))
            val newChange = crypto.change24hPercent + deltaPercent
            val newPrice = crypto.priceUsd * (1.0 + (deltaPercent / 100.0))
            val updatedSparkline = crypto.sparkline.drop(1) + (newPrice.toFloat())
            crypto.copy(
                priceUsd = if (newPrice > 10) (newPrice * 100).toLong() / 100.0 else (newPrice * 1000).toLong() / 1000.0,
                change24hPercent = (newChange * 100).toLong() / 100.0,
                sparkline = updatedSparkline
            )
        }

        _marketOverview.value = _marketOverview.value.copy(
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun getInitialStockIndices(): List<StockIndex> {
        return listOf(
            StockIndex(
                symbol = "S&P 500",
                name = "S&P 500 Index",
                region = "Americas",
                country = "United States",
                flag = "🇺🇸",
                price = 5815.20,
                changePoints = 28.45,
                changePercent = 0.49,
                isOpen = true,
                currency = "USD",
                sparkline = listOf(5760f, 5775f, 5780f, 5792f, 5805f, 5810f, 5815.2f)
            ),
            StockIndex(
                symbol = "NASDAQ",
                name = "Nasdaq Composite",
                region = "Americas",
                country = "United States",
                flag = "🇺🇸",
                price = 18190.50,
                changePoints = 142.30,
                changePercent = 0.79,
                isOpen = true,
                currency = "USD",
                sparkline = listOf(17980f, 18020f, 18090f, 18120f, 18150f, 18190.5f)
            ),
            StockIndex(
                symbol = "DOW",
                name = "Dow Jones Industrial",
                region = "Americas",
                country = "United States",
                flag = "🇺🇸",
                price = 42310.80,
                changePoints = 94.20,
                changePercent = 0.22,
                isOpen = true,
                currency = "USD",
                sparkline = listOf(42150f, 42200f, 42180f, 42250f, 42290f, 42310.8f)
            ),
            StockIndex(
                symbol = "FTSE 100",
                name = "FTSE 100 Index",
                region = "Europe",
                country = "United Kingdom",
                flag = "🇬🇧",
                price = 8280.90,
                changePoints = 15.60,
                changePercent = 0.19,
                isOpen = false,
                currency = "GBP",
                sparkline = listOf(8240f, 8255f, 8260f, 8275f, 8270f, 8280.9f)
            ),
            StockIndex(
                symbol = "DAX 40",
                name = "Deutscher Aktienindex",
                region = "Europe",
                country = "Germany",
                flag = "🇩🇪",
                price = 19250.30,
                changePoints = 124.50,
                changePercent = 0.65,
                isOpen = false,
                currency = "EUR",
                sparkline = listOf(19100f, 19140f, 19180f, 19210f, 19240f, 19250.3f)
            ),
            StockIndex(
                symbol = "CAC 40",
                name = "CAC 40 Index",
                region = "Europe",
                country = "France",
                flag = "🇫🇷",
                price = 7640.10,
                changePoints = 26.80,
                changePercent = 0.35,
                isOpen = false,
                currency = "EUR",
                sparkline = listOf(7590f, 7610f, 7625f, 7630f, 7640.1f)
            ),
            StockIndex(
                symbol = "NIKKEI 225",
                name = "Nikkei 225",
                region = "Asia-Pacific",
                country = "Japan",
                flag = "🇯🇵",
                price = 38920.00,
                changePoints = 485.00,
                changePercent = 1.26,
                isOpen = false,
                currency = "JPY",
                sparkline = listOf(38200f, 38400f, 38550f, 38700f, 38850f, 38920f)
            ),
            StockIndex(
                symbol = "HANG SENG",
                name = "Hang Seng Index",
                region = "Asia-Pacific",
                country = "Hong Kong",
                flag = "🇭🇰",
                price = 20630.50,
                changePoints = 428.20,
                changePercent = 2.12,
                isOpen = false,
                currency = "HKD",
                sparkline = listOf(19900f, 20150f, 20300f, 20450f, 20580f, 20630.5f)
            ),
            StockIndex(
                symbol = "NIFTY 50",
                name = "Nifty 50 Index",
                region = "Asia-Pacific",
                country = "India",
                flag = "🇮🇳",
                price = 25810.00,
                changePoints = 142.00,
                changePercent = 0.55,
                isOpen = false,
                currency = "INR",
                sparkline = listOf(25600f, 25680f, 25720f, 25770f, 25810f)
            ),
            StockIndex(
                symbol = "ASX 200",
                name = "S&P/ASX 200",
                region = "Asia-Pacific",
                country = "Australia",
                flag = "🇦🇺",
                price = 8210.30,
                changePoints = 32.80,
                changePercent = 0.40,
                isOpen = false,
                currency = "AUD",
                sparkline = listOf(8160f, 8180f, 8195f, 8205f, 8210.3f)
            )
        )
    }

    private fun getInitialCryptoAssets(): List<CryptoAsset> {
        return listOf(
            CryptoAsset(
                symbol = "BTC",
                name = "Bitcoin",
                priceUsd = 65420.00,
                change24hPercent = 2.84,
                marketCapFormatted = "$1.29T",
                volume24hFormatted = "$34.2B",
                high24h = 65850.00,
                low24h = 63900.00,
                sparkline = listOf(63900f, 64200f, 64550f, 64900f, 65200f, 65420f)
            ),
            CryptoAsset(
                symbol = "ETH",
                name = "Ethereum",
                priceUsd = 2640.50,
                change24hPercent = 3.42,
                marketCapFormatted = "$317.8B",
                volume24hFormatted = "$18.6B",
                high24h = 2675.00,
                low24h = 2550.00,
                sparkline = listOf(2550f, 2580f, 2600f, 2620f, 2635f, 2640.5f)
            ),
            CryptoAsset(
                symbol = "SOL",
                name = "Solana",
                priceUsd = 154.20,
                change24hPercent = 5.15,
                marketCapFormatted = "$72.1B",
                volume24hFormatted = "$4.2B",
                high24h = 156.40,
                low24h = 146.10,
                sparkline = listOf(146f, 148f, 150f, 152f, 153.5f, 154.2f)
            ),
            CryptoAsset(
                symbol = "BNB",
                name = "Binance Coin",
                priceUsd = 595.00,
                change24hPercent = 1.25,
                marketCapFormatted = "$86.5B",
                volume24hFormatted = "$1.1B",
                high24h = 598.00,
                low24h = 588.00,
                sparkline = listOf(588f, 590f, 592f, 594f, 595f)
            ),
            CryptoAsset(
                symbol = "XRP",
                name = "Ripple",
                priceUsd = 0.585,
                change24hPercent = 4.22,
                marketCapFormatted = "$33.2B",
                volume24hFormatted = "$1.8B",
                high24h = 0.592,
                low24h = 0.560,
                sparkline = listOf(0.56f, 0.565f, 0.572f, 0.580f, 0.585f)
            ),
            CryptoAsset(
                symbol = "ADA",
                name = "Cardano",
                priceUsd = 0.382,
                change24hPercent = 1.88,
                marketCapFormatted = "$13.7B",
                volume24hFormatted = "$320M",
                high24h = 0.389,
                low24h = 0.374,
                sparkline = listOf(0.374f, 0.377f, 0.380f, 0.382f)
            ),
            CryptoAsset(
                symbol = "AVAX",
                name = "Avalanche",
                priceUsd = 28.40,
                change24hPercent = 3.85,
                marketCapFormatted = "$11.4B",
                volume24hFormatted = "$480M",
                high24h = 28.90,
                low24h = 27.20,
                sparkline = listOf(27.2f, 27.5f, 27.9f, 28.2f, 28.4f)
            ),
            CryptoAsset(
                symbol = "NEAR",
                name = "NEAR Protocol",
                priceUsd = 5.24,
                change24hPercent = 6.30,
                marketCapFormatted = "$6.4B",
                volume24hFormatted = "$390M",
                high24h = 5.35,
                low24h = 4.90,
                sparkline = listOf(4.90f, 5.05f, 5.15f, 5.24f)
            )
        )
    }
}
