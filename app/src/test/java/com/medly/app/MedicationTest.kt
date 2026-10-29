package com.medly.app

import com.medly.app.data.*
import org.junit.Assert.assertEquals
import org.junit.Test

class MedicationTest {
    private val medication = Medication(name = "Vitamina D", dose = "1000 UI", frequencyPerDay = 2, unitsPerPack = 30, looseUnits = 14, packs = 0, monthlyLimit = 60)

    @Test fun remainingDaysUsesDailyFrequency() {
        assertEquals(7, medication.remainingDays())
    }

    @Test fun statusChangesWithRemainingStock() {
        assertEquals(StockStatus.LOW, medication.status())
        assertEquals(StockStatus.CRITICAL, medication.copy(looseUnits = 6).status())
        assertEquals(StockStatus.OUT, medication.copy(looseUnits = 0).status())
    }

    @Test fun consumeOpensPackWhenLooseUnitsAreInsufficient() {
        val consumed = StockLogic.consume(medication.copy(looseUnits = 1, packs = 1))
        assertEquals(29, consumed.looseUnits)
        assertEquals(0, consumed.packs)
        assertEquals(29, consumed.stock)
    }

    @Test fun checkInIsBlockedAtMonthlyLimit() {
        assertEquals(false, medication.copy(dosesTakenThisMonth = 60).canCheckIn())
    }
}
