package com.example.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import android.content.Context
import android.content.Intent

class TargetWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TargetWidget()
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
    }
}
