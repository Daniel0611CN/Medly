package com.medly.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import android.content.Context
import com.medly.app.widget.MedlyWidget
import androidx.glance.appwidget.updateAll

class MedicationRepository(
    private val dao: MedicationDao,
    private val context: Context
) {
    val medications: Flow<List<Medication>> = dao.observeAll()
    suspend fun add(medication: Medication) {
        dao.insert(medication)
        MedlyWidget().updateAll(context)
    }

    suspend fun update(medication: Medication) {
        dao.update(medication)
        MedlyWidget().updateAll(context)
    }

    suspend fun delete(medication: Medication) {
        dao.delete(medication)
        MedlyWidget().updateAll(context)
    }

    suspend fun takeDailyDose(medication: Medication) {
        if (medication.canCheckIn()) {
            dao.update(StockLogic.consume(medication))
            MedlyWidget().updateAll(context)
        }
    }
}
