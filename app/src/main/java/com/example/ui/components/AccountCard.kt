package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.CurrencyBitcoin
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AccountEntity
import com.example.data.model.AccountType
import com.example.data.model.ConnectionStatus
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.ExpenseRed
import java.util.Locale

@Composable
fun AccountCard(
    account: AccountEntity,
    currencySymbol: String,
    onToggleAutoSync: (Boolean) -> Unit,
    onSyncNow: () -> Unit,
    onClick: () -> Unit,
    isPrivacyMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val brandBaseColor = Color(account.institutionBrandColorHex)
    val cardGradient = Brush.linearGradient(
        colors = listOf(
            brandBaseColor.copy(alpha = 0.9f),
            brandBaseColor.copy(alpha = 0.7f),
            Color(0xFF0F172A)
        )
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag("account_card_${account.id}")
    ) {
        Box(
            modifier = Modifier
                .background(cardGradient)
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top header: Bank name + type icon + status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0x33FFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (account.accountType) {
                                    AccountType.CHECKING -> Icons.Default.AccountBalance
                                    AccountType.SAVINGS -> Icons.Default.Savings
                                    AccountType.CREDIT_CARD -> Icons.Default.CreditCard
                                    AccountType.INVESTMENT -> if (account.institutionName.contains("Crypto", true) || account.institutionName.contains("Coinbase", true)) Icons.Default.CurrencyBitcoin else Icons.Default.TrendingUp
                                    AccountType.LOAN -> Icons.Default.CreditCard
                                },
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = account.institutionName,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "${account.accountName} • ${account.accountNumberMask}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }

                    // Sync Indicator
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when (account.connectionStatus) {
                                    ConnectionStatus.CONNECTED -> Color(0x3310B981)
                                    ConnectionStatus.SYNCING -> Color(0x3338BDF8)
                                    ConnectionStatus.ERROR -> Color(0x33EF4444)
                                    ConnectionStatus.REQUIRES_REAUTH -> Color(0x33F59E0B)
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = account.connectionStatus.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = when (account.connectionStatus) {
                                ConnectionStatus.CONNECTED -> EmeraldLight
                                ConnectionStatus.SYNCING -> Color(0xFF38BDF8)
                                ConnectionStatus.ERROR -> ExpenseRed
                                ConnectionStatus.REQUIRES_REAUTH -> Color(0xFFF59E0B)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Balance
                val formattedBalance = String.format(Locale.US, "%,.2f", Math.abs(account.balance))
                Text(
                    text = if (isPrivacyMode) "$currencySymbol••••••" else "${if (account.balance < 0) "-" else ""}$currencySymbol$formattedBalance",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Controls: Auto-Sync Switch and Sync Now button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Switch(
                            checked = account.isAutoSyncEnabled,
                            onCheckedChange = onToggleAutoSync,
                            modifier = Modifier
                                .testTag("switch_autosync_${account.id}"),
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldLight,
                                uncheckedThumbColor = Color(0xFF94A3B8),
                                uncheckedTrackColor = Color(0x44FFFFFF)
                            )
                        )
                        Text(
                            text = if (account.isAutoSyncEnabled) "Auto-Sync" else "Manual",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = Color(0xFFE2E8F0)
                        )
                    }

                    // Sync Now Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33FFFFFF))
                            .clickable(onClick = onSyncNow)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("btn_sync_account_${account.id}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Sync",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Sync Now",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
