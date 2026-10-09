package com.example.laboratorio08prog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import com.example.laboratorio08prog.data.Task
import com.example.laboratorio08prog.data.TaskDatabase
import com.example.laboratorio08prog.ui.TaskFilter
import com.example.laboratorio08prog.ui.TaskViewModel
import com.example.laboratorio08prog.ui.TaskViewModelFactory

// Paleta GTA VI / Vice City Neon
val GtaDarkBg = Color(0xFF090710)
val GtaCardBg = Color(0xFF151022)
val GtaNeonPink = Color(0xFFFF007F)
val GtaNeonCyan = Color(0xFF00F0FF)
val GtaNeonPurple = Color(0xFF8A2BE2)
val GtaGold = Color(0xFFFFD700)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = Room.databaseBuilder(
            applicationContext,
            TaskDatabase::class.java,
            "task_db"
        ).build()

        val viewModel: TaskViewModel by viewModels {
            TaskViewModelFactory(db.taskDao())
        }

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = GtaNeonPink,
                    secondary = GtaNeonCyan,
                    background = GtaDarkBg,
                    surface = GtaDarkBg
                )
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = GtaDarkBg
                ) {
                    TaskScreenGtaStyle(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreenGtaStyle(viewModel: TaskViewModel) {
    val tasks by viewModel.tasks.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var newTaskText by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("MEDIA") }
    var taskToEdit by remember { mutableStateOf<Task?>(null) }

    // Filtrado por categoría + Búsqueda por texto (Característica #3 y #4)
    val filteredTasks = tasks.filter { task ->
        val matchesFilter = when (currentFilter) {
            TaskFilter.TODAS -> true
            TaskFilter.PENDIENTES -> !task.isCompleted
            TaskFilter.COMPLETADAS -> task.isCompleted
        }
        val matchesSearch = task.description.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    Scaffold(
        containerColor = GtaDarkBg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF100B1E)),
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("MIS TAREAS", fontWeight = FontWeight.Black, fontSize = 22.sp, color = GtaNeonPink, letterSpacing = 2.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("VI", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = GtaNeonCyan)
                        }
                        Text("VICE CITY // LAB 08", fontSize = 10.sp, color = GtaGold, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                    }
                },
                actions = {
                    if (tasks.isNotEmpty()) {
                        IconButton(onClick = { viewModel.deleteAllTasks() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar todas", tint = Color(0xFFFF3366))
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Característica #4: Búsqueda de tareas
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("BUSCAR MISIÓN...", color = Color.Gray, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar", tint = GtaNeonCyan) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar", tint = Color.Gray)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GtaNeonCyan,
                    unfocusedBorderColor = GtaNeonPurple,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = GtaCardBg,
                    unfocusedContainerColor = GtaCardBg
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Característica #3: Filtros por estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskFilter.values().forEach { filter ->
                    val isSelected = currentFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setFilter(filter) },
                        label = {
                            Text(filter.name, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = if (isSelected) Color.White else GtaNeonCyan)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GtaNeonPink,
                            containerColor = GtaCardBg
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Agregar nueva tarea + Selector de Prioridad (Característica #7)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(GtaCardBg, RoundedCornerShape(12.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = newTaskText,
                        onValueChange = { newTaskText = it },
                        placeholder = { Text("NUEVA MISIÓN...", color = Color.Gray, fontSize = 13.sp, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GtaNeonCyan,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            viewModel.addTask(newTaskText, selectedPriority)
                            newTaskText = ""
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Brush.horizontalGradient(listOf(GtaNeonPink, GtaNeonPurple)), RoundedCornerShape(12.dp))
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar", tint = Color.White)
                    }
                }

                // Selector de Prioridades (ALTA, MEDIA, BAJA)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("PRIORIDAD:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    listOf("ALTA", "MEDIA", "BAJA").forEach { priority ->
                        val isSel = selectedPriority == priority
                        val pColor = when (priority) {
                            "ALTA" -> Color(0xFFFF3366)
                            "MEDIA" -> GtaGold
                            else -> GtaNeonCyan
                        }
                        Text(
                            text = priority,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) pColor else Color.Gray,
                            modifier = Modifier
                                .clickable { selectedPriority = priority }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lista de Tareas
            if (filteredTasks.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("NO HAY MISIONES", color = Color.Gray, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(filteredTasks, key = { it.id }) { task ->
                        val priorityColor = when (task.priority.uppercase()) {
                            "ALTA" -> Color(0xFFFF3366)
                            "MEDIA" -> GtaGold
                            else -> GtaNeonCyan
                        }

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = GtaCardBg),
                            border = BorderStroke(1.5.dp, if (task.isCompleted) GtaNeonPink else GtaNeonCyan),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { viewModel.toggleTaskCompletion(task) }) {
                                    Icon(
                                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                        contentDescription = "Estado",
                                        tint = if (task.isCompleted) GtaNeonPink else GtaNeonCyan,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.description.uppercase(),
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (task.isCompleted) Color.Gray else Color.White,
                                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (task.isCompleted) "CUMPLIDA" else "PENDIENTE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (task.isCompleted) GtaNeonPink else GtaGold,
                                            letterSpacing = 1.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        // Tag de Prioridad (#7)
                                        Text(
                                            text = "[${task.priority.uppercase()}]",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = priorityColor
                                        )
                                    }
                                }

                                // Característica #1: Editar tarea
                                IconButton(onClick = { taskToEdit = task }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = GtaNeonCyan)
                                }

                                // Característica #2: Eliminar tarea individual
                                IconButton(onClick = { viewModel.deleteTask(task) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFFF3366))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal para Editar Tarea (#1)
    taskToEdit?.let { task ->
        var editDesc by remember { mutableStateOf(task.description) }
        var editPriority by remember { mutableStateOf(task.priority) }

        AlertDialog(
            onDismissRequest = { taskToEdit = null },
            containerColor = GtaCardBg,
            title = { Text("EDITAR MISIÓN", color = GtaNeonPink, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("Descripción", color = GtaNeonCyan) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = GtaNeonCyan,
                            unfocusedBorderColor = GtaNeonPurple
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                        listOf("ALTA", "MEDIA", "BAJA").forEach { p ->
                            Text(
                                text = p,
                                color = if (editPriority == p) GtaGold else Color.Gray,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { editPriority = p }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateTask(task, editDesc, editPriority)
                    taskToEdit = null
                }) {
                    Text("GUARDAR", color = GtaNeonCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToEdit = null }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            }
        )
    }
}