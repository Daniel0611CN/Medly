package com.medly.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "medications")
data class Medication(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val format: String = "Comprimidos",
    val dose: String,
    val frequencyPerDay: Int,
    val unitsPerPack: Int,
    val looseUnits: Int,
    val packs: Int,
    val monthlyLimit: Int,
    val dosesTakenThisMonth: Int = 0,
    val lastCheckIn: String? = null,
    val createdAt: String = LocalDate.now().toString()
) {
    val stock: Int get() = looseUnits + (packs * unitsPerPack)
    val monthlyQuota: Int get() = monthlyLimit
    fun dailyUnits() = frequencyPerDay.coerceAtLeast(1)
    fun remainingDays() = stock / dailyUnits()
    fun canCheckIn() = stock >= dailyUnits() && dosesTakenThisMonth < monthlyLimit
    fun status(): StockStatus = when {
        stock <= 0 -> StockStatus.OUT
        remainingDays() <= 3 -> StockStatus.CRITICAL
        remainingDays() <= 7 -> StockStatus.LOW
        else -> StockStatus.OK
    }
}

enum class StockStatus { OK, LOW, CRITICAL, OUT }

data class MonthlyUsage(val medicationId: Long, val units: Int)

object StockLogic {
    fun consume(medication: Medication, units: Int = medication.dailyUnits()): Medication {
        require(units > 0) { "units must be positive" }
        require(medication.stock >= units) { "not enough stock" }
        var loose = medication.looseUnits
        var packs = medication.packs
        if (loose < units && packs > 0) {
            packs -= 1
            loose += medication.unitsPerPack
        }
        return medication.copy(
            looseUnits = loose - units,
            packs = packs,
            dosesTakenThisMonth = medication.dosesTakenThisMonth + units,
            lastCheckIn = LocalDate.now().toString()
        )
    }
}
