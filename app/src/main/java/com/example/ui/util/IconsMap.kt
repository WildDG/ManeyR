package com.example.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object IconsMap {
    fun getIcon(name: String): ImageVector {
        return when (name) {
            "Fastfood" -> Icons.Default.Fastfood
            "DirectionsCar" -> Icons.Default.DirectionsCar
            "ShoppingCart" -> Icons.Default.ShoppingCart
            "MedicalServices" -> Icons.Default.MedicalServices
            "SportsEsports" -> Icons.Default.SportsEsports
            "Receipt" -> Icons.Default.Receipt
            "MonetizationOn" -> Icons.Default.MonetizationOn
            "Redeem" -> Icons.Default.Redeem
            "TrendingUp" -> Icons.Default.TrendingUp
            "AttachMoney" -> Icons.Default.AttachMoney
            "AccountBalanceWallet" -> Icons.Default.AccountBalanceWallet
            "AccountBalance" -> Icons.Default.AccountBalance
            "PhoneAndroid" -> Icons.Default.PhoneAndroid
            "Paid" -> Icons.Default.Paid
            "SwapHoriz" -> Icons.Default.SwapHoriz
            "Home" -> Icons.Default.Home
            "Star" -> Icons.Default.Star
            "School" -> Icons.Default.School
            "Flight" -> Icons.Default.Flight
            "FitnessCenter" -> Icons.Default.FitnessCenter
            "LocalCafe" -> Icons.Default.LocalCafe
            "Work" -> Icons.Default.Work
            "House" -> Icons.Default.House
            "Settings" -> Icons.Default.Settings
            "Analytics" -> Icons.Default.Analytics
            "Add" -> Icons.Default.Add
            else -> Icons.Default.Category
        }
    }

    val availableIcons = listOf(
        IconOption("Fastfood", "Makanan"),
        IconOption("DirectionsCar", "Transportasi"),
        IconOption("ShoppingCart", "Belanja"),
        IconOption("MedicalServices", "Kesehatan"),
        IconOption("SportsEsports", "Hiburan"),
        IconOption("Receipt", "Tagihan"),
        IconOption("Home", "Rumah"),
        IconOption("School", "Pendidikan"),
        IconOption("Flight", "Travel"),
        IconOption("FitnessCenter", "Olahraga"),
        IconOption("LocalCafe", "Kopi & Kafe"),
        IconOption("MonetizationOn", "Gaji"),
        IconOption("Redeem", "Hadiah"),
        IconOption("TrendingUp", "Investasi"),
        IconOption("AttachMoney", "Pemasukan Lain")
    )
}

data class IconOption(val name: String, val description: String)
