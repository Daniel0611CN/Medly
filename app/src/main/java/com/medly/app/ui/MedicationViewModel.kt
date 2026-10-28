package com.medly.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medly.app.data.*
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedicationViewModel(private val repository: MedicationRepository) : ViewModel() {
    val medications: StateFlow<List<Medication>> = repository.medications.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList()
    )
    fun add(medication: Medication) = viewModelScope.launch { repository.add(medication) }
    fun update(medication: Medication) = viewModelScope.launch { repository.update(medication) }
    fun delete(medication: Medication) = viewModelScope.launch { repository.delete(medication) }
    fun checkIn(medication: Medication) = viewModelScope.launch { repository.takeDailyDose(medication) }
}

class MedicationViewModelFactory(private val repository: MedicationRepository) :
    androidx.lifecycle.ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MedicationViewModel(repository) as T
}
