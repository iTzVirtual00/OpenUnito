package me.itzvirtual.myunitox.ui.agenda

import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.itzvirtual.myunitox.data.models.ActivityType
import me.itzvirtual.myunitox.data.models.AgendaEntry
import me.itzvirtual.myunitox.data.models.Building
import me.itzvirtual.myunitox.data.models.Coordinates
import me.itzvirtual.myunitox.data.models.Floor
import me.itzvirtual.myunitox.data.models.Professor
import me.itzvirtual.myunitox.data.models.Room
import me.itzvirtual.myunitox.ui.themes.LocalCustomColorsPalette
import me.itzvirtual.myunitox.ui.themes.MyUnitoXTheme
import me.itzvirtual.myunitox.ui.util.LoadingIconItem
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Filter options for the agenda
 */
enum class AgendaFilter {
    EVENTS,
    LESSONS,
    EXAMS
}

// Predefined colors for events based on activity code hash

/**
 * Get a consistent color for an event based on its activity code
 */
@Composable
fun AgendaEntry.getColor(): Color {
    val bucket = LocalCustomColorsPalette.current.calendarBucket
    val index = kotlin.math.abs(activityCode.hashCode()) % bucket.size
    return bucket[index]
}

/**
 * Extension to get LocalDate from Instant
 */
fun AgendaEntry.getLocalDate(): LocalDate {
    return startDateTime.atZone(ZoneId.systemDefault()).toLocalDate()
}

/**
 * Extension to get start LocalTime from Instant
 */
fun AgendaEntry.getStartTime(): LocalTime {
    return startDateTime.atZone(ZoneId.systemDefault()).toLocalTime()
}

/**
 * Extension to get end LocalTime from Instant
 */
fun AgendaEntry.getEndTime(): LocalTime {
    return endDateTime.atZone(ZoneId.systemDefault()).toLocalTime()
}

/**
 * Extension to get room display name
 */
fun AgendaEntry.getRoomDisplayName(): String {
    return rooms.firstOrNull()?.name ?: ""
}

@Composable
fun AgendaScreen(
    modifier: Modifier = Modifier,
    viewModel: AgendaViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        // Always render the Agenda grid
        Agenda(
            events = uiState.agendaEntries,
            modifier = Modifier.fillMaxSize(),
            initialDate = uiState.selectedStart ?: LocalDate.now(),
            onWeekChange = { startDate, endDate ->
                viewModel.loadAgendaForWeek(startDate, endDate)
            }
        )

        // Loading overlay
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                LoadingIconItem(modifier = Modifier.size(64.dp))
            }
        }

        // Error message overlay
        if (uiState.errorMessage != null) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(
                        MaterialTheme.colorScheme.errorContainer,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = uiState.errorMessage ?: "Errore sconosciuto",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * Main Agenda composable that displays a weekly calendar view
 */
@Composable
fun Agenda(
    events: List<AgendaEntry>,
    modifier: Modifier = Modifier,
    initialDate: LocalDate = LocalDate.now(),
    onWeekChange: (LocalDate, LocalDate) -> Unit = { _, _ -> }
) {
    var currentWeekStart by remember {
        mutableStateOf(initialDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
    }

    var selectedFilter by remember { mutableStateOf(AgendaFilter.EVENTS) }
    var showFilterDropdown by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<AgendaEntry?>(null) }

    val currentWeekEnd by remember(currentWeekStart) {
        derivedStateOf { currentWeekStart.plusDays(4) } // Monday to Friday
    }

    val filteredEvents by remember(events, selectedFilter) {
        derivedStateOf {
            when (selectedFilter) {
                AgendaFilter.EVENTS -> events
                AgendaFilter.LESSONS -> events.filter { it.activityType.code == "LEZ" }
                AgendaFilter.EXAMS -> events.filter { it.activityType.code == "ESA" || it.activityType.code == "PRF" }
            }
        }
    }

    val startHour = remember(filteredEvents) {
        filteredEvents.minOfOrNull { it.getStartTime().hour }?.coerceAtLeast(8) ?: 9
    }
//    val endHour = remember(filteredEvents) {
//        filteredEvents.maxOfOrNull { it.getEndTime().hour+2 }?.coerceAtMost(20) ?: 20
//    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header with filter and date navigation
            AgendaHeader(
                currentWeekStart = currentWeekStart,
                currentWeekEnd = currentWeekEnd,
                selectedFilter = selectedFilter,
                showFilterDropdown = showFilterDropdown,
//                onFilterClick = { showFilterDropdown = true },
//                onFilterDismiss = { showFilterDropdown = false },
//                onFilterSelect = { filter ->
//                    selectedFilter = filter
//                    showFilterDropdown = false
//                },
                onPreviousWeek = {
                    currentWeekStart = currentWeekStart.minusWeeks(1)
                    onWeekChange(currentWeekStart, currentWeekStart.plusDays(6))
                },
                onNextWeek = {
                    currentWeekStart = currentWeekStart.plusWeeks(1)
                    onWeekChange(currentWeekStart, currentWeekStart.plusDays(6))
                }
            )

            // Week days header
            WeekDaysHeader(currentWeekStart)

            // Calendar grid with events
            AgendaGrid(
                events = filteredEvents,
                startHour = startHour,
//                endHour = endHour,
                weekStartDate = currentWeekStart,
                onEventClick = { event -> selectedEvent = event }
            )
        }

        // Event detail overlay
        // Keep a reference to the event being displayed (for exit animation)
        var displayedEvent by remember { mutableStateOf<AgendaEntry?>(null) }
        if (selectedEvent != null) {
            displayedEvent = selectedEvent
        }

        AnimatedVisibility(
            visible = selectedEvent != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
            // Use displayedEvent which keeps the last non-null value during exit animation
            displayedEvent?.let { event ->
                EventDetailView(
                    event = event,
                    onDismiss = { selectedEvent = null }
                )
            }
        }
    }
}

@Composable
private fun AgendaHeader(
    currentWeekStart: LocalDate,
    currentWeekEnd: LocalDate,
    selectedFilter: AgendaFilter,
    showFilterDropdown: Boolean,
//    onFilterClick: () -> Unit,
//    onFilterDismiss: () -> Unit,
//    onFilterSelect: (AgendaFilter) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("d MMM", Locale.ITALIAN) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.weight(1f))
        // Filter dropdown
//        Box {
//            Surface(
//                shape = RoundedCornerShape(8.dp),
//                color = MaterialTheme.colorScheme.surfaceVariant,
//                modifier = Modifier.clickable { onFilterClick() }
//            ) {
//                Row(
//                    verticalAlignment = Alignment.CenterVertically,
//                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
//                ) {
//                    Text(
//                        text = when (selectedFilter) {
//                            AgendaFilter.EVENTS -> "Eventi"
//                            AgendaFilter.LESSONS -> "Lezioni"
//                            AgendaFilter.EXAMS -> "Esami"
//                        },
//                        style = MaterialTheme.typography.bodyMedium
//                    )
//                    Icon(
//                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
//                        contentDescription = null,
//                        modifier = Modifier.size(20.dp)
//                    )
//                }
//            }
//
//            DropdownMenu(
//                expanded = showFilterDropdown,
//                onDismissRequest = onFilterDismiss
//            ) {
//                AgendaFilter.entries.forEach { filter ->
//                    DropdownMenuItem(
//                        text = {
//                            Text(
//                                when (filter) {
//                                    AgendaFilter.EVENTS -> "Eventi"
//                                    AgendaFilter.LESSONS -> "Lezioni"
//                                    AgendaFilter.EXAMS -> "Esami"
//                                }
//                            )
//                        },
//                        onClick = { onFilterSelect(filter) }
//                    )
//                }
//            }
//        }

        // Date navigation
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onPreviousWeek, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = "Previous week"
                )
            }

            Text(
                text = "${currentWeekStart.format(dateFormatter)} - ${
                    currentWeekEnd.format(
                        dateFormatter
                    )
                }",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            IconButton(onClick = onNextWeek, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Next week"
                )
            }
        }
    }
}

@Composable
private fun WeekDaysHeader(weekStartDate: LocalDate) {
    val dayFormatter = remember { DateTimeFormatter.ofPattern("EEE d", Locale.ITALIAN) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 48.dp) // Space for time column
    ) {
        for (i in 0..4) { // Monday to Friday
            val date = weekStartDate.plusDays(i.toLong())
            val isToday = date == LocalDate.now()

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = date.format(dayFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun AgendaGrid(
    events: List<AgendaEntry>,
    weekStartDate: LocalDate,
    onEventClick: (AgendaEntry) -> Unit,
    startHour: Int = 8,
    endHour: Int = 20,
    hourHeight: Dp = 60.dp
) {
    val horizontalScrollState = rememberScrollState()
    val verticalScrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(verticalScrollState)
    ) {
        // Time column
        Column(
            modifier = Modifier
                .width(48.dp)
                .padding(vertical = 8.dp)
        ) {
            for (hour in startHour until endHour) {
                Box(
                    modifier = Modifier
                        .height(hourHeight)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.TopEnd
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%02d:00", hour),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .offset(y = (-6).dp)
                    )
                }
            }
        }

        // Days columns with events
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(horizontalScrollState)
        ) {
            for (dayOffset in 0..4) { // Monday to Friday
                val currentDate = weekStartDate.plusDays(dayOffset.toLong())
                val dayEvents = events.filter { it.getLocalDate() == currentDate }

                DayColumn(
                    events = dayEvents,
                    startHour = startHour,
                    endHour = endHour,
                    hourHeight = hourHeight,
                    onEventClick = onEventClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DayColumn(
    events: List<AgendaEntry>,
    startHour: Int,
    endHour: Int,
    hourHeight: Dp,
    onEventClick: (AgendaEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalHours = endHour - startHour

    Box(
        modifier = modifier
            .height(hourHeight * totalHours)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )
    ) {
        // Hour grid lines
        for (hour in 0 until totalHours) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .offset(y = hourHeight * hour)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            )
        }

        // Events
        events.forEach { event ->
            val startTime = event.getStartTime()
            val endTime = event.getEndTime()
            val startMinutes = startTime.hour * 60 + startTime.minute
            val endMinutes = endTime.hour * 60 + endTime.minute
            val gridStartMinutes = startHour * 60

            val topOffset = ((startMinutes - gridStartMinutes) / 60f) * hourHeight.value
            val eventHeight = ((endMinutes - startMinutes) / 60f) * hourHeight.value

            if (topOffset >= 0 && topOffset < (hourHeight.value * totalHours)) {
                EventCard(
                    event = event,
                    onClick = { onEventClick(event) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 1.dp)
                        .height(eventHeight.dp)
                        .offset(y = topOffset.dp)
                )
            }
        }
    }
}

@Composable
private fun EventCard(
    event: AgendaEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = event.getColor(),
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
        ) {
            Text(
                text = event.title,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 12.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            val roomName = event.getRoomDisplayName()
            if (roomName.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(10.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = roomName,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 9.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Detail view shown when an event is clicked
 */
@Composable
internal fun EventDetailView(
    event: AgendaEntry,
    onDismiss: () -> Unit
) {
    // Handle back button press to dismiss the detail view
    BackHandler(onBack = onDismiss)

    val timeFormatter = remember { DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.ITALIAN) }

    // tack vertical offset
    val scrollState = rememberScrollState() // internal scroll state for the content
    var offsetY by remember { mutableStateOf(0f) } // current vertical offset for drag gesture

    // Smoothly animate the snap-back
    val animatedOffset by animateFloatAsState(targetValue = offsetY, label = "offset")

    val nestedScrollConnection = remember {
        // WARNING: AI SLOP
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y

                // CASE: User is swiping UP (delta < 0)
                // If the view is currently offset (slid down), we must move it
                // back up to 0 BEFORE letting the internal column scroll.
                return if (delta < 0 && offsetY > 0) {
                    val oldOffset = offsetY
                    // Don't let it go above 0
                    offsetY = (offsetY + delta).coerceAtLeast(0f)
                    val consumed = offsetY - oldOffset
                    Offset(0f, consumed)
                } else {
                    Offset.Zero
                }
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                // CASE: User is swiping DOWN (available.y > 0)
                // If the column has finished scrolling to the top, start sliding the view down.
                if (available.y > 0 && scrollState.value == 0) {
                    offsetY += available.y
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                // If we've dragged the view down significantly
                if (offsetY > 300f) {
                    onDismiss()
                    // Consume all velocity so it doesn't "slide" further while closing
                    return available
                } else {
                    // If it was just a small wiggle, snap back to top
                    offsetY = 0f

                    // If the swipe was downward (available.y > 0),
                    // consume the velocity so the text inside doesn't suddenly
                    // start scrolling when the offset hits zero.
                    return if (available.y > 0) available else Velocity.Zero
                }
            }
        }
    }
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .offset { IntOffset(0, animatedOffset.roundToInt()) }
            .nestedScroll(nestedScrollConnection)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Header with color bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(event.getColor())
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = event.activityType.description,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Activity code
                DetailRow(
                    label = "Codice Attività",
                    value = event.activityCode
                )

                // Date and time
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = event.getLocalDate().format(dateFormatter)
                                .replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${
                                event.getStartTime().format(timeFormatter)
                            } - ${event.getEndTime().format(timeFormatter)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                HorizontalDivider()

                // Room information
                if (event.rooms.isNotEmpty()) {
                    event.rooms.forEach { room ->
                        Row(
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Column {
                                Text(
                                    text = room.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                room.floor?.let { floor ->
                                    Text(
                                        text = floor.description,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                room.building?.let { building ->
                                    Text(
                                        text = building.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    building.address?.let { address ->
                                        val fullAddress = buildString {
                                            append(address)
                                            building.streetNumber?.let { append(" $it") }
                                            building.city?.let { append(", $it") }
                                        }
                                        Text(
                                            text = fullAddress,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                room.capacity?.let { capacity ->
                                    Text(
                                        text = "Capienza: $capacity posti",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider()
                }

                // Professors
                if (event.professors.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = if (event.professors.size == 1) "Docente" else "Docenti",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            event.professors.forEach { professor ->
                                Text(
                                    text = professor.fullName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                    HorizontalDivider()
                }

                // Additional info
                DetailRow(
                    label = "Anno di corso",
                    value = "${event.courseYear}° anno"
                )

                if (event.isRemote) {
                    DetailRow(
                        label = "Modalità",
                        value = "Teledidattica"
                    )
                }

                // English title if available
                event.titleEn?.let { titleEn ->
                    DetailRow(
                        label = "Titolo (EN)",
                        value = titleEn
                    )
                }
            }
        }
    }
}

@Composable
internal fun DetailRow(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

// Sample data for preview
private val sampleEvents = listOf(
    AgendaEntry(
        id = "1",
        title = "RICERCA OPERATIVA",
        titleEn = "OPERATIONAL RESEARCH",
        startDateTime = Instant.parse("2026-02-16T08:00:00.000Z"),
        endDateTime = Instant.parse("2026-02-16T10:00:00.000Z"),
        activityCode = "INF0327",
        courseYear = 1,
        status = "P",
        rooms = listOf(
            Room(
                id = "room1",
                code = "AULA_A_INFO",
                name = "Aula A",
                capacity = 218,
                building = Building(
                    id = "building1",
                    name = "Dipartimento di Informatica",
                    address = "Via Pessinetto",
                    streetNumber = "12",
                    city = "Torino",
                    coordinates = Coordinates(45.0899689, 7.6591649)
                ),
                floor = Floor("P00", "Piano Terra")
            )
        ),
        professors = listOf(
            Professor("prof1", "Marco", "FERRARI")
        ),
        activityType = ActivityType("LEZ", "Lezione")
    ),
    AgendaEntry(
        id = "2",
        title = "BASI DI DATI",
        titleEn = "DATABASES",
        startDateTime = Instant.parse("2026-02-16T10:00:00.000Z"),
        endDateTime = Instant.parse("2026-02-16T12:00:00.000Z"),
        activityCode = "INF0123",
        courseYear = 2,
        status = "P",
        rooms = listOf(
            Room(
                id = "room2",
                code = "LABINF",
                name = "Laboratorio Informatica",
                capacity = 100
            )
        ),
        professors = listOf(
            Professor("prof2", "Mario", "ROSSI")
        ),
        activityType = ActivityType("LAB", "Laboratorio")
    ),
    AgendaEntry(
        id = "3",
        title = "FISICA I",
        startDateTime = Instant.parse("2026-02-17T09:00:00.000Z"),
        endDateTime = Instant.parse("2026-02-17T11:00:00.000Z"),
        activityCode = "FIS001",
        courseYear = 1,
        status = "P",
        rooms = listOf(
            Room(id = "room3", code = "5S", name = "Aula 5S")
        ),
        professors = listOf(
            Professor("prof3", "Luigi", "VERDI")
        ),
        activityType = ActivityType("LEZ", "Lezione")
    ),
    AgendaEntry(
        id = "4",
        title = "ALGEBRA LINEARE",
        startDateTime = Instant.parse("2026-02-18T14:00:00.000Z"),
        endDateTime = Instant.parse("2026-02-18T16:00:00.000Z"),
        activityCode = "MAT002",
        courseYear = 1,
        status = "P",
        rooms = listOf(
            Room(id = "room4", code = "2", name = "Aula 2")
        ),
        professors = listOf(
            Professor("prof4", "Anna", "BIANCHI")
        ),
        activityType = ActivityType("LEZ", "Lezione")
    )
)

@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
fun AgendaPreview() {
    MyUnitoXTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Agenda(
                events = sampleEvents,
                initialDate = LocalDate.of(2026, 2, 16),
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(showBackground = true)
fun EventDetailPreview() {
    MyUnitoXTheme {
        EventDetailView(
            event = sampleEvents[0],
            onDismiss = {}
        )
    }
}
