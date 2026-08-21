package com.example.ui.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {
    fun formatRupiah(amount: Long): String {
        val localeID = Locale("id", "ID")
        val formatter = NumberFormat.getCurrencyInstance(localeID).apply {
            minimumFractionDigits = 2
            maximumFractionDigits = 2
        }
        return formatter.format(amount)
    }

    fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale("id", "ID"))
        return dateFormat.format(Date(timestamp))
    }

    fun formatDateShort(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd/MM/yy", Locale("id", "ID"))
        return dateFormat.format(Date(timestamp))
    }
}
