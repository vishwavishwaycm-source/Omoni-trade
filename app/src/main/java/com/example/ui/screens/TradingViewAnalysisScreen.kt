package com.example.ui.screens

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.ActiveIndicatorsConfig
import com.example.data.model.ChartStyle
import com.example.data.model.CryptoAsset
import com.example.data.model.StockIndex
import com.example.data.model.Timeframe
import com.example.data.repository.TradingViewDataProvider
import com.example.ui.components.TechnicalGauge
import com.example.ui.components.TradingViewChart
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import com.example.data.model.FinancialAnalysisModel
import com.example.ui.dialogs.IndicatorsControlDialog
import com.example.ui.dialogs.ModelSelectorDialog
import com.example.ui.dialogs.TradingCalculatorDialog
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.ExpenseRed
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TradingViewAnalysisScreen(
    stockIndices: List<StockIndex>,
    cryptoAssets: List<CryptoAsset>,
    currencySymbol: String,
    initialSymbol: String? = null,
    currentModel: FinancialAnalysisModel = FinancialAnalysisModel.QUANT_MOMENTUM,
    onSelectModel: (FinancialAnalysisModel) -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val allAssetSymbols = remember(stockIndices, cryptoAssets) {
        val list = mutableListOf<Triple<String, String, Double>>()
        stockIndices.forEach { list.add(Triple(it.symbol, "${it.flag} ${it.name}", it.price)) }
        cryptoAssets.forEach { list.add(Triple(it.symbol, it.name, it.priceUsd)) }
        list
    }

    var selectedSymbol by remember {
        mutableStateOf(initialSymbol ?: allAssetSymbols.firstOrNull()?.first ?: "BTC")
    }
    var assetDropdownExpanded by remember { mutableStateOf(false) }

    var selectedTimeframe by remember { mutableStateOf(Timeframe.ONE_MONTH) }
    var selectedChartStyle by remember { mutableStateOf(ChartStyle.CANDLESTICK) }

    // Robinhood & TradingView Indicators state
    var indicatorsConfig by remember {
        mutableStateOf(
            ActiveIndicatorsConfig(
                showVwap = true,
                showBollinger = true,
                showEma = true,
                showSma = false,
                showParabolicSar = false,
                showVolume = true,
                showRsi = true,
                showMacd = true,
                showStochastic = false
            )
        )
    }
    var showIndicatorsDialog by remember { mutableStateOf(false) }
    var showCalculatorDialog by remember { mutableStateOf(false) }
    var showModelDialog by remember { mutableStateOf(false) }

    val currentAssetInfo = allAssetSymbols.find { it.first == selectedSymbol } ?: allAssetSymbols.firstOrNull()
    val basePrice = currentAssetInfo?.third ?: 65000.0

    val candles = remember(selectedSymbol, selectedTimeframe, basePrice) {
        TradingViewDataProvider.generateCandles(selectedSymbol, basePrice, selectedTimeframe)
    }

    val technicalData = remember(selectedSymbol, basePrice) {
        TradingViewDataProvider.generateTechnicalAnalysis(selectedSymbol, basePrice)
    }

    val orderBook = remember(selectedSymbol, basePrice) {
        TradingViewDataProvider.generateOrderBook(basePrice)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "OmniTrade Analysis",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("btn_back_from_tradingview")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Active Model Switcher Chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showModelDialog = true }
                            .testTag("btn_select_model")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = "Model",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = currentModel.badge,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Risk & Position Calculator Button
                    IconButton(
                        onClick = { showCalculatorDialog = true },
                        modifier = Modifier.testTag("btn_open_trading_calculator")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = "Risk Calculator",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Quick Indicator Settings Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showIndicatorsDialog = true }
                            .testTag("btn_open_indicators_menu")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Tune,
                                contentDescription = "Indicators",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Indicators",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("trading_view_analysis_screen"),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Asset Selector & Current Price Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = assetDropdownExpanded,
                            onExpandedChange = { assetDropdownExpanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = "${currentAssetInfo?.second ?: selectedSymbol} (${currentAssetInfo?.first ?: ""})",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Selected Market Instrument") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = assetDropdownExpanded) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = assetDropdownExpanded,
                                onDismissRequest = { assetDropdownExpanded = false }
                            ) {
                                allAssetSymbols.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text("${item.second} (${item.first})") },
                                        onClick = {
                                            selectedSymbol = item.first
                                            assetDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "$currencySymbol${String.format(Locale.US, "%,.2f", basePrice)}",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val isBullish = (candles.lastOrNull()?.close ?: 0.0) >= (candles.firstOrNull()?.open ?: 0.0)
                                Text(
                                    text = if (isBullish) "+2.84% (Period)" else "-1.45% (Period)",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isBullish) EmeraldLight else ExpenseRed
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(technicalData.overallSignal.colorHex).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = technicalData.overallSignal.label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.ExtraBold),
                                    color = Color(technicalData.overallSignal.colorHex),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Timeframe and Chart Style Selection
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Timeframe Chips (1D, 1W, 1M, 3M, 1Y, ALL)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(Timeframe.values()) { tf ->
                            val isSelected = selectedTimeframe == tf
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedTimeframe = tf }
                                    .testTag("tf_${tf.name.lowercase()}")
                            ) {
                                Text(
                                    text = tf.label,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }

                    // Chart Style Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(ChartStyle.CANDLESTICK, ChartStyle.LINE, ChartStyle.LIVE_WEB).forEach { style ->
                                val isSelected = selectedChartStyle == style
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedChartStyle = style }
                                ) {
                                    Text(
                                        text = style.label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        // Indicator Settings Shortcut Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.clickable { showIndicatorsDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(14.dp))
                                Text(
                                    text = "All Indicators",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }
                    }

                    // ROBINHOOD & TRADINGVIEW QUICK INDICATOR CHIPS
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            QuickChip(
                                label = "VWAP",
                                activeColor = Color(0xFFEC4899),
                                isActive = indicatorsConfig.showVwap,
                                onClick = { indicatorsConfig = indicatorsConfig.copy(showVwap = !indicatorsConfig.showVwap) }
                            )
                        }
                        item {
                            QuickChip(
                                label = "Bands (20,2)",
                                activeColor = Color(0xFF38BDF8),
                                isActive = indicatorsConfig.showBollinger,
                                onClick = { indicatorsConfig = indicatorsConfig.copy(showBollinger = !indicatorsConfig.showBollinger) }
                            )
                        }
                        item {
                            QuickChip(
                                label = "EMA (9/21)",
                                activeColor = Color(0xFF10B981),
                                isActive = indicatorsConfig.showEma,
                                onClick = { indicatorsConfig = indicatorsConfig.copy(showEma = !indicatorsConfig.showEma) }
                            )
                        }
                        item {
                            QuickChip(
                                label = "SMA (20/50)",
                                activeColor = Color(0xFFF59E0B),
                                isActive = indicatorsConfig.showSma,
                                onClick = { indicatorsConfig = indicatorsConfig.copy(showSma = !indicatorsConfig.showSma) }
                            )
                        }
                        item {
                            QuickChip(
                                label = "RSI (14)",
                                activeColor = Color(0xFFA855F7),
                                isActive = indicatorsConfig.showRsi,
                                onClick = { indicatorsConfig = indicatorsConfig.copy(showRsi = !indicatorsConfig.showRsi) }
                            )
                        }
                        item {
                            QuickChip(
                                label = "MACD (12,26,9)",
                                activeColor = Color(0xFF06B6D4),
                                isActive = indicatorsConfig.showMacd,
                                onClick = { indicatorsConfig = indicatorsConfig.copy(showMacd = !indicatorsConfig.showMacd) }
                            )
                        }
                        item {
                            QuickChip(
                                label = "SAR",
                                activeColor = Color(0xFFC084FC),
                                isActive = indicatorsConfig.showParabolicSar,
                                onClick = { indicatorsConfig = indicatorsConfig.copy(showParabolicSar = !indicatorsConfig.showParabolicSar) }
                            )
                        }
                        item {
                            QuickChip(
                                label = "Volume",
                                activeColor = Color(0xFF94A3B8),
                                isActive = indicatorsConfig.showVolume,
                                onClick = { indicatorsConfig = indicatorsConfig.copy(showVolume = !indicatorsConfig.showVolume) }
                            )
                        }
                    }
                }
            }

            // PRIMARY CHART WITH MULTI-PANE INDICATORS
            item {
                if (selectedChartStyle == ChartStyle.LIVE_WEB) {
                    LiveTradingViewWebWidget(
                        symbol = when (selectedSymbol) {
                            "BTC" -> "BINANCE:BTCUSDT"
                            "ETH" -> "BINANCE:ETHUSDT"
                            "SOL" -> "BINANCE:SOLUSDT"
                            "BNB" -> "BINANCE:BNBUSDT"
                            "SPX" -> "FOREXCOM:SPXUSD"
                            "NDX" -> "NASDAQ:NDX"
                            "NKY" -> "INDEX:NKY"
                            "UK100" -> "INDEX:UK100"
                            "DAX" -> "INDEX:DAX"
                            else -> "BINANCE:BTCUSDT"
                        }
                    )
                } else {
                    TradingViewChart(
                        candles = candles,
                        currencySymbol = currencySymbol,
                        chartStyle = selectedChartStyle,
                        indicators = indicatorsConfig
                    )
                }
            }

            // Speedometer Technical Rating
            item {
                TechnicalGauge(analysis = technicalData)
            }

            // Support & Resistance Pivot Points
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "SUPPORT & RESISTANCE LEVELS (CLASSIC)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        val levels = listOf(
                            Triple("Resistance 2 (R2)", technicalData.resistance2, ExpenseRed),
                            Triple("Resistance 1 (R1)", technicalData.resistance1, ExpenseRed.copy(alpha = 0.8f)),
                            Triple("Pivot Point (P)", technicalData.pivotPoint, Color(0xFFF59E0B)),
                            Triple("Support 1 (S1)", technicalData.support1, EmeraldLight.copy(alpha = 0.8f)),
                            Triple("Support 2 (S2)", technicalData.support2, EmeraldLight)
                        )

                        levels.forEach { (name, value, color) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(name, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    "$currencySymbol${String.format(Locale.US, "%,.2f", value)}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = color
                                )
                            }
                        }
                    }
                }
            }

            // Technical Indicators & Oscillators Breakdown
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "TECHNICAL INDICATORS & OSCILLATORS",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        technicalData.indicatorsList.forEach { ind ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(ind.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold))
                                    Text(ind.value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }

                                val badgeColor = when (ind.isBullish) {
                                    true -> EmeraldLight
                                    false -> ExpenseRed
                                    null -> Color(0xFF94A3B8)
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = badgeColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = ind.action,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = badgeColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Order Book / Market Depth
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ORDER BOOK DEPTH",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("Size", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        orderBook.filter { !it.isBid }.forEach { ask ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "$currencySymbol${String.format(Locale.US, "%,.2f", ask.price)}",
                                    color = ExpenseRed,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    String.format(Locale.US, "%.3f", ask.amount),
                                    color = Color(0xFF94A3B8),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(vertical = 4.dp, horizontal = 8.dp)
                        ) {
                            Text(
                                text = "Spread: $currencySymbol${String.format(Locale.US, "%,.2f", basePrice)} (Mark Price)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        orderBook.filter { it.isBid }.forEach { bid ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "$currencySymbol${String.format(Locale.US, "%,.2f", bid.price)}",
                                    color = EmeraldLight,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    String.format(Locale.US, "%.3f", bid.amount),
                                    color = Color(0xFF94A3B8),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showIndicatorsDialog) {
        IndicatorsControlDialog(
            currentConfig = indicatorsConfig,
            onDismiss = { showIndicatorsDialog = false },
            onApplyConfig = { newConfig ->
                indicatorsConfig = newConfig
                showIndicatorsDialog = false
            }
        )
    }

    if (showCalculatorDialog) {
        TradingCalculatorDialog(
            symbol = selectedSymbol,
            currentPrice = basePrice,
            currencySymbol = currencySymbol,
            onDismiss = { showCalculatorDialog = false }
        )
    }

    if (showModelDialog) {
        ModelSelectorDialog(
            currentModel = currentModel,
            onDismiss = { showModelDialog = false },
            onSelectModel = { newModel ->
                onSelectModel(newModel)
                showModelDialog = false
            }
        )
    }
}

@Composable
private fun QuickChip(
    label: String,
    activeColor: Color,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = if (isActive) activeColor.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(3.dp),
                color = if (isActive) activeColor else Color.Gray,
                modifier = Modifier.size(8.dp)
            ) {}
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LiveTradingViewWebWidget(
    symbol: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A),
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(16.dp))
    ) {
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = WebViewClient()
                    setBackgroundColor(0xFF0F172A.toInt())

                    val html = """
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <meta name="viewport" content="width=device-width, initial-scale=1.0">
                            <style>body { margin: 0; background-color: #0f172a; overflow: hidden; }</style>
                        </head>
                        <body>
                            <div class="tradingview-widget-container" style="height:100%;width:100%">
                              <div id="tradingview_widget" style="height:calc(100% - 32px);width:100%"></div>
                              <script type="text/javascript" src="https://s3.tradingview.com/tv.js"></script>
                              <script type="text/javascript">
                              new TradingView.widget(
                              {
                                "autosize": true,
                                "symbol": "$symbol",
                                "interval": "D",
                                "timezone": "Etc/UTC",
                                "theme": "dark",
                                "style": "1",
                                "locale": "en",
                                "toolbar_bg": "#0f172a",
                                "enable_publishing": false,
                                "allow_symbol_change": false,
                                "container_id": "tradingview_widget"
                              }
                              );
                              </script>
                            </div>
                        </body>
                        </html>
                    """.trimIndent()

                    loadDataWithBaseURL("https://www.tradingview.com", html, "text/html", "UTF-8", null)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
