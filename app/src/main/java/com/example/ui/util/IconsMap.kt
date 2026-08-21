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
            "Pets" -> Icons.Default.Pets
            "ChildCare" -> Icons.Default.ChildCare
            "Spa" -> Icons.Default.Spa
            "SmokingRooms" -> Icons.Default.SmokingRooms
            "WineBar" -> Icons.Default.WineBar
            "Checkroom" -> Icons.Default.Checkroom
            "LocalMall" -> Icons.Default.LocalMall
            "Wifi" -> Icons.Default.Wifi
            "ElectricMeter" -> Icons.Default.ElectricMeter
            "WaterDrop" -> Icons.Default.WaterDrop
            "Tv" -> Icons.Default.Tv
            "Movie" -> Icons.Default.Movie
            "MusicNote" -> Icons.Default.MusicNote
            "MenuBook" -> Icons.Default.MenuBook
            "Park" -> Icons.Default.Park
            "DirectionsBus" -> Icons.Default.DirectionsBus
            "Train" -> Icons.Default.Train
            "LocalGasStation" -> Icons.Default.LocalGasStation
            "Hotel" -> Icons.Default.Hotel
            "Restaurant" -> Icons.Default.Restaurant
            "LocalPizza" -> Icons.Default.LocalPizza
            "Cake" -> Icons.Default.Cake
            "Icecream" -> Icons.Default.Icecream
            "BusinessCenter" -> Icons.Default.BusinessCenter
            "Savings" -> Icons.Default.Savings
            "CardGiftcard" -> Icons.Default.CardGiftcard
            "Payments" -> Icons.Default.Payments
            "Store" -> Icons.Default.Store
            "Build" -> Icons.Default.Build
            "Warning" -> Icons.Default.Warning
            "Favorite" -> Icons.Default.Favorite
            else -> Icons.Default.Category
        }
    }

    val availableIcons = listOf(
        IconOption("Fastfood", "Makanan"),
        IconOption("LocalPizza", "Pizza"),
        IconOption("Cake", "Kue"),
        IconOption("Icecream", "Es Krim"),
        IconOption("Restaurant", "Restoran"),
        IconOption("LocalCafe", "Kafe"),
        IconOption("WineBar", "Minuman"),
        IconOption("DirectionsCar", "Mobil"),
        IconOption("DirectionsBus", "Bus"),
        IconOption("Train", "Kereta"),
        IconOption("Flight", "Pesawat"),
        IconOption("LocalGasStation", "BBM"),
        IconOption("ShoppingCart", "Keranjang"),
        IconOption("LocalMall", "Mall"),
        IconOption("Checkroom", "Pakaian"),
        IconOption("MedicalServices", "Obat"),
        IconOption("Favorite", "Kesehatan"),
        IconOption("Spa", "Perawatan"),
        IconOption("SportsEsports", "Game"),
        IconOption("Movie", "Film"),
        IconOption("MusicNote", "Musik"),
        IconOption("Tv", "TV"),
        IconOption("Receipt", "Tagihan"),
        IconOption("ElectricMeter", "Listrik"),
        IconOption("WaterDrop", "Air"),
        IconOption("Wifi", "Internet"),
        IconOption("Home", "Rumah"),
        IconOption("House", "Properti"),
        IconOption("Build", "Perbaikan"),
        IconOption("Store", "Toko"),
        IconOption("School", "Sekolah"),
        IconOption("MenuBook", "Buku"),
        IconOption("FitnessCenter", "Gym"),
        IconOption("Park", "Taman"),
        IconOption("Pets", "Hewan Peliharaan"),
        IconOption("ChildCare", "Anak"),
        IconOption("MonetizationOn", "Gaji"),
        IconOption("Payments", "Pembayaran"),
        IconOption("Savings", "Tabungan"),
        IconOption("Redeem", "Voucher"),
        IconOption("CardGiftcard", "Kado"),
        IconOption("TrendingUp", "Investasi"),
        IconOption("BusinessCenter", "Bisnis"),
        IconOption("Work", "Pekerjaan"),
        IconOption("AttachMoney", "Pemasukan")
    )
}

data class IconOption(val name: String, val description: String)
