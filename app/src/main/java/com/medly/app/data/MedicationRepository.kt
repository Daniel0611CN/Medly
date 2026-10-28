package com.medly.app.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class MedicationRepository(private val dao: MedicationDao) {
    val medications: Flow<List<Medication>> = dao.observeAll()
    suspend fun add(medication: Medication) = dao.insert(medication)
    suspend fun update(medication: Medication) = dao.update(medication)
    suspend fun delete(medication: Medication) = dao.delete(medication)

    suspend fun takeDailyDose(medication: Medication) {
        if (medication.canCheckIn()) dao.update(StockLogic.consume(medication))
    }
}
