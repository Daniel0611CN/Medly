package com.medly.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.*
import androidx.room.Room
import com.medly.app.data.*
import com.medly.app.ui.*
import com.medly.app.ui.theme.MedlyTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MedicationViewModel by viewModels {
        MedicationViewModelFactory(
            MedicationRepository(
                Room.databaseBuilder(this, MedicationDatabase::class.java, "medly.db")
                    .fallbackToDestructiveMigration()
                    .build()
                    .medicationDao(),
                applicationContext
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferences = getSharedPreferences("medly_preferences", MODE_PRIVATE)
        setContent {
            val systemDark = isSystemInDarkTheme()
            var darkTheme by remember {
                mutableStateOf(preferences.getBoolean("dark_theme", systemDark))
            }
            MedlyTheme(darkTheme = darkTheme) {
                MedlyApp(
                    viewModel = viewModel,
                    darkTheme = darkTheme,
                    onToggleTheme = {
                        darkTheme = !darkTheme
                        preferences.edit().putBoolean("dark_theme", darkTheme).apply()
                    }
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun MedlyApp(
    viewModel: MedicationViewModel,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    val navController = rememberNavController()
    val medications by viewModel.medications.collectAsState()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Medly") },
                actions = {
                    IconButton(onClick = onToggleTheme) {
                        Icon(
                            imageVector = if (darkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (darkTheme) "Activar modo claro" else "Activar modo oscuro"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = navController.currentBackStackEntryAsState().value?.destination?.route == "today",
                    onClick = { navController.navigate("today") { popUpTo("today") { inclusive = true } } },
                    icon = { Icon(Icons.Default.Today, null) }, label = { Text("Hoy") }
                )
                NavigationBarItem(
                    selected = navController.currentBackStackEntryAsState().value?.destination?.route == "inventory",
                    onClick = { navController.navigate("inventory") },
                    icon = { Icon(Icons.Default.Inventory2, null) }, label = { Text("Inventario") }
                )
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = "today", modifier = Modifier.padding(padding)) {
            composable("today") {
                TodayScreen(medications, viewModel, onInventory = { navController.navigate("inventory") })
            }
            composable("inventory") {
                InventoryScreen(medications, viewModel)
            }
        }
    }
}

@Composable
private fun TodayScreen(
    medications: List<Medication>,
    viewModel: MedicationViewModel,
    onInventory: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Buenos días", style = MaterialTheme.typography.headlineMedium)
            Text("Registra tu toma diaria y mantén tu tratamiento bajo control.")
        }
        if (medications.isEmpty()) {
            item {
                EmptyState("Aún no tienes medicamentos", "Añade tu primer medicamento desde Inventario.", onInventory)
            }
        } else {
            items(medications, key = { it.id }) { medication ->
                MedicationCard(medication, onCheckIn = { viewModel.checkIn(medication) })
            }
        }
    }
}

@Composable
private fun InventoryScreen(medications: List<Medication>, viewModel: MedicationViewModel) {
    var showForm by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Medication?>(null) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Column { Text("Inventario", style = MaterialTheme.typography.headlineMedium); Text("${medications.size} medicamentos") }
            FilledTonalButton(onClick = { editing = null; showForm = true }) { Icon(Icons.Default.Add, null); Spacer(Modifier.width(4.dp)); Text("Añadir") }
        }
        Spacer(Modifier.height(12.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(medications, key = { it.id }) { medication ->
                MedicationCard(medication, onCheckIn = { viewModel.checkIn(medication) }, onEdit = { editing = medication; showForm = true }, onDelete = { viewModel.delete(medication) })
            }
        }
    }
    if (showForm) {
        MedicationFormDialog(editing, onDismiss = { showForm = false }, onSave = {
            if (editing == null) viewModel.add(it) else viewModel.update(it)
            showForm = false
        })
    }
}

@Composable
private fun MedicationCard(
    medication: Medication,
    onCheckIn: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    val statusColor = when (medication.status()) {
        StockStatus.OK -> MaterialTheme.colorScheme.primary
        StockStatus.LOW -> MaterialTheme.colorScheme.secondary
        else -> MaterialTheme.colorScheme.error
    }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text(medication.name, style = MaterialTheme.typography.titleLarge); Text("${medication.dose} · ${medication.frequencyPerDay} toma(s)/día") }
                AssistChip(onClick = {}, label = { Text(statusLabel(medication)) }, colors = AssistChipDefaults.assistChipColors(labelColor = statusColor))
            }
            Spacer(Modifier.height(8.dp))
            Text("${medication.stock} unidades (${medication.packs} cajas + ${medication.looseUnits} sueltas)")
            Text("${medication.remainingDays()} días restantes · ${medication.dosesTakenThisMonth}/${medication.monthlyLimit} unidades este mes")
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = onCheckIn, enabled = medication.stock >= medication.frequencyPerDay) { Icon(Icons.Default.Check, null); Spacer(Modifier.width(4.dp)); Text("Registrar toma") }
                onEdit?.let { OutlinedButton(onClick = it) { Icon(Icons.Default.Edit, null) } }
                onDelete?.let { IconButton(onClick = it) { Icon(Icons.Default.Delete, "Eliminar") } }
            }
        }
    }
}

private fun statusLabel(m: Medication) = when (m.status()) {
    StockStatus.OK -> "Bien"
    StockStatus.LOW -> "Bajo"
    StockStatus.CRITICAL -> "Crítico"
    StockStatus.OUT -> "Agotado"
}

@Composable
private fun EmptyState(title: String, message: String, action: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Medication, null, Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(message)
            Spacer(Modifier.height(12.dp))
            Button(onClick = action) { Text("Ir a inventario") }
        }
    }
}

@Composable
private fun MedicationFormDialog(existing: Medication?, onDismiss: () -> Unit, onSave: (Medication) -> Unit) {
    var name by remember { mutableStateOf(existing?.name.orEmpty()) }
    var format by remember { mutableStateOf(existing?.format ?: "Comprimidos") }
    var dose by remember { mutableStateOf(existing?.dose.orEmpty()) }
    var frequency by remember { mutableStateOf(existing?.frequencyPerDay?.toString() ?: "1") }
    var packSize by remember { mutableStateOf(existing?.unitsPerPack?.toString() ?: "30") }
    var packs by remember { mutableStateOf(existing?.packs?.toString() ?: "0") }
    var loose by remember { mutableStateOf(existing?.looseUnits?.toString() ?: "30") }
    var quota by remember { mutableStateOf(existing?.monthlyLimit?.toString() ?: "30") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (existing == null) "Añadir medicamento" else "Editar medicamento") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Nombre") }, singleLine = true)
                OutlinedTextField(format, { format = it }, label = { Text("Formato") }, singleLine = true)
                OutlinedTextField(dose, { dose = it }, label = { Text("Dosis (ej. 500 mg)") }, singleLine = true)
                NumberField("Tomas al día", frequency) { frequency = it }
                NumberField("Unidades por caja", packSize) { packSize = it }
                NumberField("Cajas cerradas", packs) { packs = it }
                NumberField("Unidades sueltas", loose) { loose = it }
                NumberField("Cuota mensual", quota) { quota = it }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank() && dose.isNotBlank()) onSave(
                    Medication(
                        id = existing?.id ?: 0,
                        name = name.trim(),
                        format = format.trim().ifBlank { "Comprimidos" },
                        dose = dose.trim(),
                        frequencyPerDay = frequency.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                        unitsPerPack = packSize.toIntOrNull()?.coerceAtLeast(1) ?: 1,
                        looseUnits = loose.toIntOrNull()?.coerceAtLeast(0) ?: 0,
                        packs = packs.toIntOrNull()?.coerceAtLeast(0) ?: 0,
                        monthlyLimit = quota.toIntOrNull()?.coerceAtLeast(0) ?: 0,
                        dosesTakenThisMonth = existing?.dosesTakenThisMonth ?: 0,
                        lastCheckIn = existing?.lastCheckIn,
                        createdAt = existing?.createdAt ?: java.time.LocalDate.now().toString()
                    )
                )
            }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun NumberField(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(value, { if (it.all(Char::isDigit)) onValueChange(it) }, label = { Text(label) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), singleLine = true)
}
