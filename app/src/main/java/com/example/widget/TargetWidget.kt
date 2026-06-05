package com.example.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.text.Text
import androidx.glance.layout.*
import androidx.glance.Button
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.action.actionStartActivity
import com.example.ui.util.FormatUtils

import androidx.glance.action.clickable
import androidx.glance.text.FontWeight
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.data.db.AppDatabase
import kotlinx.coroutines.flow.first
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.components.TitleBar
import androidx.glance.GlanceTheme
import androidx.glance.ImageProvider
import androidx.glance.appwidget.action.actionRunCallback
import android.content.Intent
import androidx.glance.background
import androidx.glance.appwidget.cornerRadius

class TargetWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val database = AppDatabase.getDatabase(context)
        val savingTargets = database.savingTargetDao().getAllSavingTargets().first()
        
        provideContent {
            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .background(ColorProvider(Color(0xFFE8DEF8)))
                        .cornerRadius(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (savingTargets.isNotEmpty()) {
                        val firstTarget = savingTargets.first()
                        val progress = if (firstTarget.targetAmount > 0) (firstTarget.currentAmount / firstTarget.targetAmount) * 100 else 0.0
                        Text(
                            text = "Target Impian: ${firstTarget.name}",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color(0xFF4A4458)))
                        )
                        Spacer(modifier = GlanceModifier.height(8.dp))
                        Text(
                            text = "${FormatUtils.formatRupiah(firstTarget.currentAmount)} / ${FormatUtils.formatRupiah(firstTarget.targetAmount)} (${progress.toInt()}%)",
                            style = TextStyle(fontSize = 12.sp, color = ColorProvider(Color(0xFF4A4458)))
                        )
                    } else {
                        Text(
                            text = "Belum Ada Target",
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = ColorProvider(Color(0xFF4A4458)))
                        )
                        Spacer(modifier = GlanceModifier.height(8.dp))
                        Text(
                            text = "Ayo mulai menabung!",
                            style = TextStyle(fontSize = 12.sp, color = ColorProvider(Color(0xFF4A4458)))
                        )
                    }
                    
                    Spacer(modifier = GlanceModifier.height(14.dp))
                    
                    Button(
                        text = "+ Target Impian",
                        onClick = actionStartActivity(
                            Intent(context, MainActivity::class.java).apply {
                                action = "OPEN_ADD_TARGET"
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            }
                        ),
                        modifier = GlanceModifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
