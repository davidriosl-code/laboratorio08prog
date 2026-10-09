package com.example.laboratorio08prog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import com.example.laboratorio08prog.data.TaskDatabase
import com.example.laboratorio08prog.ui.TaskFilter
import com.example.laboratorio08prog.ui.TaskViewModel
import com.example.laboratorio08prog.ui.TaskViewModelFactory

// Paleta de colores GTA VI / Vice City Neon
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
    var newTaskText by remember { mutableStateOf("") }

    val filteredTasks = when (currentFilter) {
        TaskFilter.TODAS -> tasks
        TaskFilter.PENDIENTES -> tasks.filter { !it.isCompleted }
        TaskFilter.COMPLETADAS -> tasks.filter { it.isCompleted }
    }

    Scaffold(
        containerColor = GtaDarkBg,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF100B1E)
                ),
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MIS TAREAS",
                                fontWeight = FontWeight.Black,
                                fontSize = 22.sp,
                                color = GtaNeonPink,
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "VI",
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = GtaNeonCyan
                            )
                        }
                        Text(
                            text = "VICE CITY // LAB 08",
                            fontSize = 10.sp,
                            color = GtaGold,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        )
                    }
                },
                actions = {
                    if (tasks.isNotEmpty()) {
                        IconButton(onClick = { viewModel.deleteAllTasks() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar todas",
                                tint = Color(0xFFFF3366)
                            )
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

            // Chips de Filtro con Neón
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
                            Text(
                                text = filter.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSelected) Color.White else GtaNeonCyan
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GtaNeonPink,
                            containerColor = GtaCardBg
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Campo para ingresar nueva tarea estilo GTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newTaskText,
                    onValueChange = { newTaskText = it },
                    placeholder = {
                        Text(
                            text = "INGRESA UNA NUEVA MISIÓN...",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    modifier = Modifier.weight(1f),
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

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        viewModel.addTask(newTaskText)
                        newTaskText = ""
                    },
                    modifier = Modifier
                        .size(54.dp)
                        .background(
                            brush = Brush.horizontalGradient(listOf(GtaNeonPink, GtaNeonPurple)),
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Agregar",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lista de Tareas estilo Tarjetas Neón
            if (filteredTasks.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NO HAY MISIONES ACTIVAS",
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTasks, key = { it.id }) { task ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = GtaCardBg),
                            border = BorderStroke(
                                width = 1.5.dp,
                                color = if (task.isCompleted) GtaNeonPink else GtaNeonCyan
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Botón Checkbox Neón
                                IconButton(onClick = { viewModel.toggleTaskCompletion(task) }) {
                                    Icon(
                                        imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                                        contentDescription = "Estado",
                                        tint = if (task.isCompleted) GtaNeonPink else GtaNeonCyan,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = task.description.uppercase(),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (task.isCompleted) Color.Gray else Color.White,
                                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                    Text(
                                        text = if (task.isCompleted) "MISIÓN CUMPLIDA" else "MISIÓN PENDIENTE",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (task.isCompleted) GtaNeonPink else GtaGold,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Fuego",
                                    tint = if (task.isCompleted) Color.Gray else GtaNeonPink,
                                    modifier = Modifier
                                        .size(22.dp)
                                        .padding(end = 4.dp)
                                )

                                // Botón de Eliminar Tarea Individual
                                IconButton(onClick = { viewModel.deleteTask(task) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Eliminar tarea",
                                        tint = Color(0xFFFF3366)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}