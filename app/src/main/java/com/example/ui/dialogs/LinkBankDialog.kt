package com.example.ui.dialogs

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.AccountType
import com.example.data.repository.BankInstitution
import com.example.ui.theme.EmeraldLight

private enum class WizardStep {
    SELECT_BANK,
    CREDENTIALS,
    MFA_VERIFY,
    ACCOUNT_SETUP,
    CONNECTING
}

@Composable
fun LinkBankDialog(
    institutions: List<BankInstitution>,
    onDismiss: () -> Unit,
    onLinkAccount: (BankInstitution, String, AccountType, Double) -> Unit
) {
    var currentStep by remember { mutableStateOf(WizardStep.SELECT_BANK) }
    var selectedInstitution by remember { mutableStateOf<BankInstitution?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedRegionFilter by remember { mutableStateOf<String?>(null) }

    // Credentials
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // MFA
    var mfaCode by remember { mutableStateOf("849201") }

    // Account setup
    var customAccountName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(AccountType.CHECKING) }
    var initialBalanceStr by remember { mutableStateOf("5000.00") }

    val regionsList = listOf(
        null to "🌐 All Nations",
        "North America" to "🇺🇸/🇨🇦 N. America",
        "Europe" to "🇪🇺/🇬🇧 Europe",
        "Asia-Pacific" to "🌏 Asia-Pac",
        "Middle East" to "🇦🇪/🇸🇦 Middle East",
        "Latin America" to "🇧🇷/🇲🇽 LatAm",
        "Africa" to "🇿🇦/🇳🇬 Africa",
        "Crypto" to "🪙 Web3"
    )

    val filteredInstitutions = institutions.filter { inst ->
        val matchesRegion = selectedRegionFilter == null || inst.region == selectedRegionFilter
        val matchesSearch = searchQuery.isBlank() ||
                inst.name.contains(searchQuery, ignoreCase = true) ||
                inst.country.contains(searchQuery, ignoreCase = true) ||
                inst.protocol.contains(searchQuery, ignoreCase = true)
        matchesRegion && matchesSearch
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("link_bank_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (currentStep != WizardStep.SELECT_BANK && currentStep != WizardStep.CONNECTING) {
                            IconButton(
                                onClick = {
                                    currentStep = when (currentStep) {
                                        WizardStep.CREDENTIALS -> WizardStep.SELECT_BANK
                                        WizardStep.MFA_VERIFY -> WizardStep.CREDENTIALS
                                        WizardStep.ACCOUNT_SETUP -> WizardStep.MFA_VERIFY
                                        else -> WizardStep.SELECT_BANK
                                    }
                                }
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = EmeraldLight,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = when (currentStep) {
                                WizardStep.SELECT_BANK -> "Worldwide Bank Sync"
                                WizardStep.CREDENTIALS -> "Secure Open Banking"
                                WizardStep.MFA_VERIFY -> "Two-Factor Auth"
                                WizardStep.ACCOUNT_SETUP -> "Account Feed Setup"
                                WizardStep.CONNECTING -> "Synchronizing Live Feed"
                            },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (currentStep != WizardStep.CONNECTING) {
                        IconButton(onClick = onDismiss, modifier = Modifier.testTag("btn_close_link_dialog")) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "WizardStepTransition"
                ) { step ->
                    when (step) {
                        WizardStep.SELECT_BANK -> {
                            Column {
                                Text(
                                    text = "Connect financial accounts across 15+ nations via Open Banking, SEPA, UPI, and ISO-20022 protocols.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Search input
                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    placeholder = { Text("Search 65+ worldwide banks, nations...") },
                                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_search_banks")
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Region / Nation filter pills
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(regionsList) { (reg, label) ->
                                        val isSelected = selectedRegionFilter == reg
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                            modifier = Modifier.clickable { selectedRegionFilter = reg }
                                        ) {
                                            Text(
                                                text = label,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = "Supported Institutions (${filteredInstitutions.size})",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Banks list
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(290.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(filteredInstitutions) { inst ->
                                        Surface(
                                            shape = RoundedCornerShape(14.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(14.dp))
                                                .clickable {
                                                    selectedInstitution = inst
                                                    currentStep = WizardStep.CREDENTIALS
                                                }
                                                .testTag("inst_${inst.id}")
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(40.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(inst.brandColorHex)),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = inst.countryFlag,
                                                            fontSize = 20.sp
                                                        )
                                                    }

                                                    Column {
                                                        Text(
                                                            text = inst.name,
                                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                                        )
                                                        Row(
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            Text(
                                                                text = inst.country,
                                                                style = MaterialTheme.typography.labelSmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                            Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                                            Text(
                                                                text = inst.protocol,
                                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                                color = EmeraldLight
                                                            )
                                                        }
                                                    }
                                                }

                                                Icon(
                                                    imageVector = Icons.Default.Lock,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        WizardStep.CREDENTIALS -> {
                            selectedInstitution?.let { inst ->
                                Column {
                                    Surface(
                                        shape = RoundedCornerShape(16.dp),
                                        color = Color(inst.brandColorHex).copy(alpha = 0.15f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(inst.countryFlag, fontSize = 24.sp)
                                            Column {
                                                Text(
                                                    text = inst.name,
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                                )
                                                Text(
                                                    text = "${inst.country} • Protocol: ${inst.protocol}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = EmeraldLight
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Online Banking Identification",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    OutlinedTextField(
                                        value = username,
                                        onValueChange = { username = it },
                                        label = { Text("User ID / Customer Number") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_bank_username")
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = password,
                                        onValueChange = { password = it },
                                        label = { Text("Password / PIN") },
                                        singleLine = true,
                                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                        trailingIcon = {
                                            IconButton(onClick = { showPassword = !showPassword }) {
                                                Icon(
                                                    if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                    contentDescription = null
                                                )
                                            }
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_bank_password")
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldLight, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "256-bit AES encryption via ${inst.protocol}. Credentials never stored.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Button(
                                        onClick = { currentStep = WizardStep.MFA_VERIFY },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("btn_continue_to_mfa")
                                    ) {
                                        Text("Authenticate with ${inst.name}", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        WizardStep.MFA_VERIFY -> {
                            selectedInstitution?.let { inst ->
                                Column {
                                    Text(
                                        text = "Bank Multi-Factor Security (2FA)",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "A one-time security passkey has been transmitted to your verified mobile number.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    OutlinedTextField(
                                        value = mfaCode,
                                        onValueChange = { mfaCode = it },
                                        label = { Text("Enter 6-Digit OTP Code") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_mfa_code")
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Auto-filled demo code for instant test.",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = EmeraldLight
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Button(
                                        onClick = { currentStep = WizardStep.ACCOUNT_SETUP },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("btn_verify_mfa")
                                    ) {
                                        Text("Confirm Security Authorization", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        WizardStep.ACCOUNT_SETUP -> {
                            selectedInstitution?.let { inst ->
                                Column {
                                    Text(
                                        text = "Account Configuration",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Choose which account feed from ${inst.name} you would like to synchronize.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Account Type",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))

                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        items(inst.supportedTypes) { type ->
                                            val isSelected = selectedType == type
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier.clickable { selectedType = type }
                                            ) {
                                                Text(
                                                    text = type.displayName,
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    OutlinedTextField(
                                        value = customAccountName,
                                        onValueChange = { customAccountName = it },
                                        placeholder = { Text("${inst.name} ${selectedType.displayName}") },
                                        label = { Text("Account Nickname (Optional)") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    OutlinedTextField(
                                        value = initialBalanceStr,
                                        onValueChange = { initialBalanceStr = it },
                                        label = { Text("Initial Verified Balance") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Button(
                                        onClick = {
                                            currentStep = WizardStep.CONNECTING
                                            val balance = initialBalanceStr.toDoubleOrNull() ?: 5000.00
                                            onLinkAccount(inst, customAccountName, selectedType, balance)
                                            onDismiss()
                                        },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("btn_complete_bank_link")
                                    ) {
                                        Text("Authorize & Start Automatic Sync", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        WizardStep.CONNECTING -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    color = EmeraldLight,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Establishing Live Bank Feed...",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Negotiating Open Banking token exchange",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
