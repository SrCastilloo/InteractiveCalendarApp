package com.danielcastillo.calendariointeractivo.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Event
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.danielcastillo.calendariointeractivo.data.AppUser
import com.danielcastillo.calendariointeractivo.data.CalendarEvent
import com.danielcastillo.calendariointeractivo.data.EventDraft
import com.danielcastillo.calendariointeractivo.ui.AppUiState
import com.danielcastillo.calendariointeractivo.ui.theme.Gold
import com.danielcastillo.calendariointeractivo.ui.theme.Ink
import com.danielcastillo.calendariointeractivo.ui.theme.Lagoon
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val SpanishLocale: Locale = Locale.forLanguageTag("es-ES")

@Composable
fun CalendarScreen(
    uiState: AppUiState,
    onCreateEvent: (EventDraft) -> Unit,
    onDeleteEvent: (CalendarEvent) -> Unit,
    onProfile: () -> Unit,
    onLogout: () -> Unit
) {
    var visibleMonth by rememberSaveable { mutableStateOf(YearMonth.now()) }
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now()) }
    var showCreateDialog by rememberSaveable { mutableStateOf(false) }
    var showSuccessDialog by rememberSaveable { mutableStateOf(false) }
    var lastSuccessToken by rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(uiState.eventCreatedVisualToken) {
        if (uiState.eventCreatedVisualToken > 0 && uiState.eventCreatedVisualToken != lastSuccessToken) {
            lastSuccessToken = uiState.eventCreatedVisualToken
            showSuccessDialog = true
        }
    }

    val selectedEvents = remember(uiState.events, selectedDate) {
        uiState.events
            .filter { it.occursOn(selectedDate) }
            .sortedBy { it.createdAtMillis }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = 14.dp,
                end = 16.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                CalendarTopBar(
                    user = uiState.user,
                    onProfile = onProfile,
                    onLogout = onLogout
                )
            }

            item {
                MonthCalendar(
                    visibleMonth = visibleMonth,
                    selectedDate = selectedDate,
                    events = uiState.events,
                    onPrevious = { visibleMonth = visibleMonth.minusMonths(1) },
                    onNext = { visibleMonth = visibleMonth.plusMonths(1) },
                    onToday = {
                        selectedDate = LocalDate.now()
                        visibleMonth = YearMonth.now()
                    },
                    onDateSelected = { date ->
                        selectedDate = date
                        visibleMonth = YearMonth.from(date)
                    }
                )
            }

            item {
                SelectedDayHeader(
                    selectedDate = selectedDate,
                    eventsCount = selectedEvents.size
                )
            }

            if (selectedEvents.isEmpty()) {
                item { EmptyDay() }
            } else {
                items(
                    items = selectedEvents,
                    key = { it.id }
                ) { event ->
                    EventItem(
                        event = event,
                        canDelete = event.creatorUid == uiState.user?.uid,
                        onDelete = { onDeleteEvent(event) }
                    )
                }
            }
        }

        FloatingActionButton(
            onClick = { showCreateDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(22.dp),
            containerColor = Gold,
            contentColor = Ink
        ) {
            Icon(Icons.Rounded.Add, contentDescription = "Crear evento")
        }
    }

    if (showCreateDialog) {
        CreateEventDialog(
            initialDate = selectedDate,
            loading = uiState.operationInProgress,
            onDismiss = { showCreateDialog = false },
            onCreate = { draft ->
                onCreateEvent(draft)
                showCreateDialog = false
            }
        )
    }

    if (showSuccessDialog) {
        EventCreatedSuccessDialog(
            onDismiss = { showSuccessDialog = false }
        )
    }
}

@Composable
private fun CalendarTopBar(
    user: AppUser?,
    onProfile: () -> Unit,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(name = user?.name.orEmpty(), size = 52)

        Column(
            modifier = Modifier
                .padding(start = 12.dp)
                .weight(1f)
        ) {
            Text(
                text = "Calendario interactivo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = user?.name ?: "Usuario",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(onClick = onProfile) {
            Icon(Icons.Rounded.Edit, contentDescription = "Editar perfil")
        }

        IconButton(onClick = onLogout) {
            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "Cerrar sesión")
        }
    }
}

@Composable
private fun MonthCalendar(
    visibleMonth: YearMonth,
    selectedDate: LocalDate,
    events: List<CalendarEvent>,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevious) {
                    Icon(Icons.Rounded.ChevronLeft, contentDescription = "Mes anterior")
                }

                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = visibleMonth
                            .format(DateTimeFormatter.ofPattern("MMMM yyyy", SpanishLocale))
                            .replaceFirstChar { it.uppercase() },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )

                    TextButton(onClick = onToday) {
                        Icon(
                            imageVector = Icons.Rounded.Today,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text("Hoy")
                    }
                }

                IconButton(onClick = onNext) {
                    Icon(Icons.Rounded.ChevronRight, contentDescription = "Mes siguiente")
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                listOf("L", "M", "X", "J", "V", "S", "D").forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.labelLarge,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            monthCells(visibleMonth).chunked(7).forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    week.forEach { date ->
                        val dayEvents = if (date == null) {
                            emptyList()
                        } else {
                            events.filter { it.occursOn(date) }
                        }

                        DayCell(
                            date = date,
                            selected = date == selectedDate,
                            today = date == LocalDate.now(),
                            events = dayEvents,
                            onClick = {
                                if (date != null) onDateSelected(date)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.86f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate?,
    selected: Boolean,
    today: Boolean,
    events: List<CalendarEvent>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background = when {
        selected -> Lagoon
        today -> Gold.copy(alpha = 0.24f)
        else -> MaterialTheme.colorScheme.surface
    }

    val textColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = when {
                    selected -> Lagoon
                    today -> Gold
                    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.30f)
                },
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(enabled = date != null, onClick = onClick),
        color = if (date == null) Color.Transparent else background,
        shape = RoundedCornerShape(14.dp)
    ) {
        if (date != null) {
            Column(
                modifier = Modifier.padding(6.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    color = textColor,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Black
                )

                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                    events.take(3).forEach { event ->
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(colorFromHex(event.colorHex))
                        )
                    }

                    if (events.size > 3) {
                        Text(
                            text = "+${events.size - 3}",
                            style = MaterialTheme.typography.labelSmall,
                            color = textColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedDayHeader(
    selectedDate: LocalDate,
    eventsCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = selectedDate
                    .format(DateTimeFormatter.ofPattern("EEEE, d MMMM", SpanishLocale))
                    .replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black
            )

            Text(
                text = "$eventsCount evento${if (eventsCount == 1) "" else "s"} en este día",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Icon(
            imageVector = Icons.Rounded.Event,
            contentDescription = null,
            tint = Lagoon
        )
    }
}

@Composable
private fun EmptyDay() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.60f)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Rounded.CalendarToday, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Text("No hay eventos publicados para este día.")
        }
    }
}

@Composable
private fun EventItem(
    event: CalendarEvent,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    var confirmDelete by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(102.dp)
                    .clip(RoundedCornerShape(99.dp))
                    .background(colorFromHex(event.colorHex))
            )

            Spacer(Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black
                )

                Text(
                    text = event.dateLabel(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Lagoon,
                    fontWeight = FontWeight.Bold
                )

                if (event.description.isNotBlank()) {
                    Text(
                        text = event.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Creado por ${event.creatorName.ifBlank { "Usuario" }}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Creado el ${event.createdLabel()}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                )

                val daysLeft = event.endsInDaysFromToday()
                if (daysLeft in 0..15) {
                    Text(
                        text = "Quedan $daysLeft día${if (daysLeft == 1L) "" else "s"} para que finalice",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (canDelete) {
                IconButton(onClick = { confirmDelete = true }) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Eliminar evento",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = {
                Text(
                    text = "Eliminar evento",
                    fontWeight = FontWeight.Black
                )
            },
            text = {
                Text("¿Seguro que quieres eliminar este evento? Solo puede hacerlo quien lo creó.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmDelete = false
                        onDelete()
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateEventDialog(
    initialDate: LocalDate,
    loading: Boolean,
    onDismiss: () -> Unit,
    onCreate: (EventDraft) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }
    var startDate by rememberSaveable { mutableStateOf(initialDate) }
    var endDate by rememberSaveable { mutableStateOf(initialDate) }
    var colorHex by rememberSaveable { mutableStateOf("#10B7A7") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nuevo evento",
                fontWeight = FontWeight.Black
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Descripción") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    DateField(
                        label = "Inicio",
                        date = startDate,
                        onDateSelected = {
                            startDate = it
                            if (endDate.isBefore(it)) endDate = it
                        },
                        modifier = Modifier.weight(1f)
                    )

                    DateField(
                        label = "Fin",
                        date = endDate,
                        onDateSelected = {
                            endDate = if (it.isBefore(startDate)) startDate else it
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                ColorPicker(
                    selected = colorHex,
                    onSelected = { colorHex = it }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreate(
                        EventDraft(
                            title = title.trim(),
                            description = description.trim(),
                            startDate = startDate,
                            endDate = endDate,
                            colorHex = colorHex
                        )
                    )
                },
                enabled = !loading && title.isNotBlank()
            ) {
                Icon(Icons.Rounded.Check, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Publicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Icon(Icons.Rounded.Close, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Cancelar")
            }
        },
        shape = RoundedCornerShape(26.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    label: String,
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by rememberSaveable { mutableStateOf(false) }

    OutlinedButton(
        onClick = { showPicker = true },
        modifier = modifier
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(
                text = date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (showPicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = date.toMillis()
        )

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let {
                            onDateSelected(it.toLocalDate())
                        }
                        showPicker = false
                    }
                ) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = state)
        }
    }
}

@Composable
private fun ColorPicker(
    selected: String,
    onSelected: (String) -> Unit
) {
    val colors = listOf(
        "#10B7A7",
        "#FF6B6B",
        "#FFBF47",
        "#6672FF",
        "#33A1FD",
        "#7A5CFF"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        colors.forEach { color ->
            val selectedColor = colorFromHex(color)

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(selectedColor)
                    .border(
                        width = if (selected == color) 4.dp else 1.dp,
                        color = if (selected == color) Ink else Color.White,
                        shape = CircleShape
                    )
                    .clickable { onSelected(color) },
                contentAlignment = Alignment.Center
            ) {
                if (selected == color) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun EventCreatedSuccessDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                modifier = Modifier.size(68.dp),
                shape = CircleShape,
                color = Lagoon.copy(alpha = 0.18f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = Lagoon,
                        modifier = Modifier.size(42.dp)
                    )
                }
            }
        },
        title = {
            Text(
                text = "¡Evento creado!",
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Text(
                text = "El evento se ha publicado correctamente y los demás usuarios serán notificados.",
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Perfecto")
            }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun Avatar(
    name: String,
    size: Int
) {
    val initials = remember(name) {
        name
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifBlank { "U" }
    }

    Surface(
        modifier = Modifier.size(size.dp),
        shape = CircleShape,
        color = Lagoon.copy(alpha = 0.18f),
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initials,
                color = Lagoon,
                fontWeight = FontWeight.Black
            )
        }
    }
}

private fun monthCells(month: YearMonth): List<LocalDate?> {
    val first = month.atDay(1)
    val leadingEmptyCells = first.dayOfWeek.value - 1

    return buildList {
        repeat(leadingEmptyCells) { add(null) }

        for (day in 1..month.lengthOfMonth()) {
            add(month.atDay(day))
        }

        while (size % 7 != 0) {
            add(null)
        }
    }
}

private fun LocalDate.toMillis(): Long {
    return atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
}

private fun Long.toLocalDate(): LocalDate {
    return Instant
        .ofEpochMilli(this)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()
}

private fun colorFromHex(hex: String): Color {
    return runCatching {
        Color(android.graphics.Color.parseColor(hex))
    }.getOrDefault(Lagoon)
}