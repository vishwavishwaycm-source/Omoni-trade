package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Theaters
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionCategory
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionType
import com.example.ui.theme.CatEducation
import com.example.ui.theme.CatEntertainment
import com.example.ui.theme.CatFood
import com.example.ui.theme.CatGroceries
import com.example.ui.theme.CatHealthcare
import com.example.ui.theme.CatHousing
import com.example.ui.theme.CatInvestments
import com.example.ui.theme.CatOther
import com.example.ui.theme.CatSalary
import com.example.ui.theme.CatShopping
import com.example.ui.theme.CatTransport
import com.example.ui.theme.CatTravel
import com.example.ui.theme.CatUtilities
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.PendingGold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionItem(
    transaction: TransactionEntity,
    accountName: String,
    currencySymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = getCategoryColor(transaction.category)
    val categoryIcon = getCategoryIcon(transaction.category)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("transaction_item_${transaction.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Category Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(categoryColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = transaction.category.displayName,
                        tint = categoryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f, fill = false)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = transaction.title,
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1
                        )
                        if (transaction.isPending) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = PendingGold.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Pending",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PendingGold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = accountName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatTxDate(transaction.dateTimestamp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Amount Column
            val isIncome = transaction.type == TransactionType.INCOME
            val prefix = if (isIncome) "+" else "-"
            val formattedAmount = String.format(Locale.US, "%,.2f", Math.abs(transaction.amount))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$prefix$currencySymbol$formattedAmount",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isIncome) EmeraldLight else ExpenseRed
                )
                Text(
                    text = transaction.category.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun getCategoryColor(category: TransactionCategory): Color = when (category) {
    TransactionCategory.FOOD_DINING -> CatFood
    TransactionCategory.GROCERIES -> CatGroceries
    TransactionCategory.SHOPPING -> CatShopping
    TransactionCategory.HOUSING -> CatHousing
    TransactionCategory.TRANSPORTATION -> CatTransport
    TransactionCategory.UTILITIES -> CatUtilities
    TransactionCategory.ENTERTAINMENT -> CatEntertainment
    TransactionCategory.HEALTHCARE -> CatHealthcare
    TransactionCategory.SALARY -> CatSalary
    TransactionCategory.INVESTMENTS -> CatInvestments
    TransactionCategory.TRAVEL -> CatTravel
    TransactionCategory.EDUCATION -> CatEducation
    TransactionCategory.OTHER -> CatOther
}

fun getCategoryIcon(category: TransactionCategory): ImageVector = when (category) {
    TransactionCategory.FOOD_DINING -> Icons.Default.Restaurant
    TransactionCategory.GROCERIES -> Icons.Default.ShoppingCart
    TransactionCategory.SHOPPING -> Icons.Default.Store
    TransactionCategory.HOUSING -> Icons.Default.Home
    TransactionCategory.TRANSPORTATION -> Icons.Default.DirectionsCar
    TransactionCategory.UTILITIES -> Icons.Default.Bolt
    TransactionCategory.ENTERTAINMENT -> Icons.Default.Theaters
    TransactionCategory.HEALTHCARE -> Icons.Default.LocalHospital
    TransactionCategory.SALARY -> Icons.Default.AttachMoney
    TransactionCategory.INVESTMENTS -> Icons.Default.TrendingUp
    TransactionCategory.TRAVEL -> Icons.Default.Flight
    TransactionCategory.EDUCATION -> Icons.Default.School
    TransactionCategory.OTHER -> Icons.Default.Category
}

private fun formatTxDate(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Date()
    val sameDay = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(date) ==
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(now)
    return if (sameDay) {
        "Today, " + SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
    } else {
        SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
    }
}
