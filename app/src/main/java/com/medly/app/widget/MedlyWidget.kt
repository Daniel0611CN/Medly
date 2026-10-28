package com.medly.app.widget

import android.content.Context
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.medly.app.MainActivity
import com.medly.app.data.StockStatus
import kotlinx.coroutines.flow.first

class MedlyWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val medications = DatabaseProvider.get(context).medicationDao().observeAll().first()
        provideContent {
            val top = medications.sortedBy { it.remainingDays() }.take(3)
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(androidx.compose.ui.graphics.Color(0xFFF3F7F5)))
                    .padding(16.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                verticalAlignment = Alignment.Vertical.Top
            ) {
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Text(
                        text = "Medly",
                        style = TextStyle(
                            color = ColorProvider(androidx.compose.ui.graphics.Color(0xFF0F2E2A)),
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(GlanceModifier.width(8.dp))
                    Text(
                        text = if (top.isEmpty()) "Sin tratamientos" else "Tomas de hoy",
                        style = TextStyle(color = ColorProvider(androidx.compose.ui.graphics.Color(0xFF0F766E)))
                    )
                }
                Spacer(GlanceModifier.size(8.dp))
                if (top.isEmpty()) {
                    Text(
                        "Añade medicamentos en Medly para verlos aquí.",
                        style = TextStyle(color = ColorProvider(androidx.compose.ui.graphics.Color(0xFF0F2E2A)))
                    )
                } else {
                    top.forEach { medication ->
                        val status = when (medication.status()) {
                            StockStatus.OK -> "OK"
                            StockStatus.LOW -> "Quedan ${medication.remainingDays()} días"
                            StockStatus.CRITICAL -> "Stock crítico"
                            StockStatus.OUT -> "Agotado"
                        }
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Vertical.CenterVertically
                        ) {
                            Text(
                                text = "○",
                                style = TextStyle(color = ColorProvider(androidx.compose.ui.graphics.Color(0xFF0F766E)))
                            )
                            Spacer(GlanceModifier.width(8.dp))
                            Column(modifier = GlanceModifier.defaultWeight()) {
                                Text(
                                    text = medication.name,
                                    style = TextStyle(
                                        color = ColorProvider(androidx.compose.ui.graphics.Color(0xFF0F2E2A)),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "${medication.dose} · $status",
                                    style = TextStyle(color = ColorProvider(androidx.compose.ui.graphics.Color(0xFF42635D)))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

class MedlyWidgetReceiver : androidx.glance.appwidget.GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MedlyWidget()
}

private object DatabaseProvider {
    fun get(context: Context): com.medly.app.data.MedicationDatabase =
        androidx.room.Room.databaseBuilder(
            context,
            com.medly.app.data.MedicationDatabase::class.java,
            "medly.db"
        ).fallbackToDestructiveMigration().build()
}
