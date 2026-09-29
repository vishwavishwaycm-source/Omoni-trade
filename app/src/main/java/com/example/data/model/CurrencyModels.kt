package com.example.data.model

data class CurrencyInfo(
    val code: String,
    val name: String,
    val symbol: String,
    val flag: String,
    val rateToUsd: Double // How many units of this currency per 1 USD
)

object WorldCurrencies {
    val allCurrencies: List<CurrencyInfo> = listOf(
        CurrencyInfo("USD", "US Dollar", "$", "🇺🇸", 1.0),
        CurrencyInfo("EUR", "Euro", "€", "🇪🇺", 0.92),
        CurrencyInfo("GBP", "British Pound", "£", "🇬🇧", 0.77),
        CurrencyInfo("JPY", "Japanese Yen", "¥", "🇯🇵", 148.50),
        CurrencyInfo("CAD", "Canadian Dollar", "CA$", "🇨🇦", 1.36),
        CurrencyInfo("AUD", "Australian Dollar", "A$", "🇦🇺", 1.52),
        CurrencyInfo("INR", "Indian Rupee", "₹", "🇮🇳", 83.95),
        CurrencyInfo("CHF", "Swiss Franc", "CHF", "🇨🇭", 0.88),
        CurrencyInfo("CNY", "Chinese Yuan", "¥", "🇨🇳", 7.12),
        CurrencyInfo("SGD", "Singapore Dollar", "S$", "🇸🇬", 1.32),
        CurrencyInfo("AED", "UAE Dirham", "AED", "🇦🇪", 3.67),
        CurrencyInfo("SAR", "Saudi Riyal", "SAR", "🇸🇦", 3.75),
        CurrencyInfo("BRL", "Brazilian Real", "R$", "🇧🇷", 5.48),
        CurrencyInfo("MXN", "Mexican Peso", "Mex$", "🇲🇽", 19.30),
        CurrencyInfo("KRW", "South Korean Won", "₩", "🇰🇷", 1342.0),
        CurrencyInfo("ZAR", "South African Rand", "R", "🇿🇦", 17.55),
        CurrencyInfo("HKD", "Hong Kong Dollar", "HK$", "🇭🇰", 7.78),
        CurrencyInfo("NZD", "New Zealand Dollar", "NZ$", "🇳🇿", 1.63),
        CurrencyInfo("SEK", "Swedish Krona", "kr", "🇸🇪", 10.42),
        CurrencyInfo("NOK", "Norwegian Krone", "kr", "🇳🇴", 10.65),
        CurrencyInfo("DKK", "Danish Krone", "kr", "🇩🇰", 6.87),
        CurrencyInfo("TRY", "Turkish Lira", "₺", "🇹🇷", 34.20),
        CurrencyInfo("THB", "Thai Baht", "฿", "🇹🇭", 33.15),
        CurrencyInfo("IDR", "Indonesian Rupiah", "Rp", "🇮🇩", 15420.0),
        CurrencyInfo("MYR", "Malaysian Ringgit", "RM", "🇲🇾", 4.32),
        CurrencyInfo("PHP", "Philippine Peso", "₱", "🇵🇭", 56.40),
        CurrencyInfo("VND", "Vietnamese Dong", "₫", "🇻🇳", 24850.0),
        CurrencyInfo("PLN", "Polish Zloty", "zł", "🇵🇱", 3.95),
        CurrencyInfo("CZK", "Czech Koruna", "Kč", "🇨🇿", 23.10),
        CurrencyInfo("HUF", "Hungarian Forint", "Ft", "🇭🇺", 365.0),
        CurrencyInfo("ILS", "Israeli Shekel", "₪", "🇮🇱", 3.74),
        CurrencyInfo("CLP", "Chilean Peso", "CLP$", "🇨🇱", 925.0),
        CurrencyInfo("COP", "Colombian Peso", "COL$", "🇨🇴", 4180.0),
        CurrencyInfo("ARS", "Argentine Peso", "ARS$", "🇦🇷", 975.0),
        CurrencyInfo("EGP", "Egyptian Pound", "E£", "🇪🇬", 48.60),
        CurrencyInfo("PKR", "Pakistani Rupee", "₨", "🇵🇰", 278.0),
        CurrencyInfo("BDT", "Bangladeshi Taka", "৳", "🇧🇩", 119.5),
        CurrencyInfo("NGN", "Nigerian Naira", "₦", "🇳🇬", 1630.0),
        CurrencyInfo("KES", "Kenyan Shilling", "KSh", "🇰🇪", 129.5),
        CurrencyInfo("QAR", "Qatari Riyal", "QR", "🇶🇦", 3.64),
        CurrencyInfo("KWD", "Kuwaiti Dinar", "KD", "🇰🇼", 0.306),
        CurrencyInfo("BHD", "Bahraini Dinar", "BD", "🇧🇭", 0.377),
        CurrencyInfo("OMR", "Omani Rial", "RO", "🇴🇲", 0.385),
        CurrencyInfo("TWD", "Taiwan New Dollar", "NT$", "🇹🇼", 32.10)
    )

    val defaultCurrency = allCurrencies.first() // USD
}
