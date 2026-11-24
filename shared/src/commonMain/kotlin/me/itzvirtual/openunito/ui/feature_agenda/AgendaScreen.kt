package me.itzvirtual.openunito.ui.feature_agenda


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.plus
import me.itzvirtual.openunito.data.calendar.local.CalendarConfig
import me.itzvirtual.openunito.data.localization.withDateFormatter
import me.itzvirtual.openunito.domain.agenda.ActivityType
import me.itzvirtual.openunito.domain.agenda.AgendaEntry
import me.itzvirtual.openunito.domain.agenda.Building
import me.itzvirtual.openunito.domain.agenda.Coordinates
import me.itzvirtual.openunito.domain.agenda.Floor
import me.itzvirtual.openunito.domain.agenda.Professor
import me.itzvirtual.openunito.domain.agenda.RoomLocation
import me.itzvirtual.openunito.domain.getDayOfWeek
import me.itzvirtual.openunito.domain.getToday
import me.itzvirtual.openunito.domain.toLocalDate
import me.itzvirtual.openunito.domain.toLocalTime
import me.itzvirtual.openunito.resources.*
import me.itzvirtual.openunito.ui.components.GradientBackground
import me.itzvirtual.openunito.ui.components.ShadowDefaults
import me.itzvirtual.openunito.ui.components.StaleCacheBanner
import me.itzvirtual.openunito.ui.shared.LoadingIconItem
import me.itzvirtual.openunito.ui.themes.LocalCustomColorsPalette
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlin.time.Instant

/*********************************************************************************************
 * IMPORTANT:
 * This screen was generated with AI as a proof of concept at the beginning of development,
 * and it was fully functional, so we kept it while developing the rest of the app.
 * We are planning to rewrite and improve it in the near future.
 * Please don't start screaming "AHHHH, THIS APP IS FULLY SLOPPED AHHHHH", it is not.
 *********************************************************************************************/

/**
 * Filter options for the agenda
 */
enum class AgendaFilter {
    EVENTS,
    LESSONS,
    EXAMS
}

private sealed interface UpsertErrorState {
    data object EmptyName : UpsertErrorState
    data object InvalidLink : UpsertErrorState
    data class Remote(val message: String) : UpsertErrorState
}

// Predefined colors for events based on activity code hash

/**
 * Get a consistent color for an event based on its activity code
 */
@Composable
fun AgendaEntry.getColor(): Color {
    val rand = Random(activityCode.hashCode())

    val hue = rand.nextInt(0, 360 / 5) * 5f
    val sat = rand.nextDouble(0.65, 0.85).toFloat()

    return Color.hsv(hue, sat, 0.65f)
}

/**
 * Extension to get LocalDate from Instant
 */
fun AgendaEntry.getLocalDate(): LocalDate {
    return startDateTime.toLocalDate()
}

/**
 * Extension to get start LocalTime from Instant
 */
fun AgendaEntry.getStartTime(): LocalTime {
    return startDateTime.toLocalTime()
}

/**
 * Extension to get end LocalTime from Instant
 */
fun AgendaEntry.getEndTime(): LocalTime {
    return endDateTime.toLocalTime()
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
    viewModel: AgendaViewModel = koinViewModel()
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    Box(modifier = modifier.fillMaxSize()) {
        // Always render the Agenda grid
        AgendaScreenContent(
            events = screenState.agendaState.agendaEntries,
            modifier = Modifier.fillMaxSize(),
            currentWeekStart = getDayOfWeek(
                DayOfWeek.MONDAY,
                screenState.selectedStart ?: getToday(),
            ),
            onNextWeek = { viewModel.onNextWeek() },
            goToToday = { viewModel.goToToday() },
            onPreviousWeek = { viewModel.onPreviousWeek() },
            calendars = screenState.calendars,
            selectedCalendar = screenState.selectedCalendar,
            isCacheStale = false, // TODO: this screenState does not have isStale parameter
            onCalendarSelected = { viewModel.selectCalendar(it) },
            onCalendarAdd = { name, linkId, calendarId, onResult ->
                viewModel.upsertCalendar(
                    name,
                    linkId,
                    calendarId,
                    onResult
                )
            },
            onCalendarDelete = { viewModel.deleteCalendar(it) },
        )

        if (screenState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                LoadingIconItem(modifier = Modifier.size(64.dp))
            }
        } else if (screenState.agendaState.error != null) {
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
                var errMsg = screenState.agendaState.error!!
                if (errMsg == "NO_CALENDAR") {
                    errMsg = stringResource(Res.string.error_no_calendar_configured)
                }
                Text(
                    text = errMsg,
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
fun AgendaScreenContent(
    events: List<AgendaEntry>,
    modifier: Modifier = Modifier,
    currentWeekStart: LocalDate = getDayOfWeek(DayOfWeek.MONDAY),
    isCacheStale: Boolean = false,
    onNextWeek: () -> Unit = {},
    goToToday: () -> Unit = {},
    onPreviousWeek: () -> Unit = {},
    calendars: List<CalendarConfig> = emptyList(),
    selectedCalendar: CalendarConfig? = null,
    onCalendarSelected: (CalendarConfig) -> Unit = {},
    onCalendarAdd: (String, String, Int?, (Boolean, String) -> Unit) -> Unit = { _, _, _, _ -> },
    onCalendarDelete: (Int) -> Unit = {},
) {

    var selectedFilter by remember { mutableStateOf(AgendaFilter.EVENTS) }
    var selectedEvent by remember { mutableStateOf<AgendaEntry?>(null) }

    val currentWeekEnd by remember(currentWeekStart) {
        derivedStateOf { currentWeekStart.plus(4, DateTimeUnit.DAY) } // Monday to Friday
    }

    val filteredEvents by remember(events, selectedFilter) {
        derivedStateOf {
            when (selectedFilter) { // Future-proofing
                AgendaFilter.EVENTS -> events // Always .EVENTS
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
        Scaffold(
            containerColor = Color.Transparent,
            modifier = modifier,
            topBar = {
                GradientBackground(
                    colors = listOf(
                        MaterialTheme.colorScheme.onPrimary,
                        LocalCustomColorsPalette.current.onAgendaColor,
                    ),
                ) {
                    AgendaHeader(
                        modifier = Modifier
                            .padding(top = 8.dp, bottom = 8.dp)
                            .statusBarsPadding(),
                        currentWeekStart = currentWeekStart,
                        currentWeekEnd = currentWeekEnd,
                        onNextWeek = onNextWeek,
                        onPreviousWeek = onPreviousWeek,
                        calendars = calendars,
                        selectedCalendar = selectedCalendar,
                        onCalendarSelected = onCalendarSelected,
                        onCalendarAdd = onCalendarAdd,
                        onCalendarDelete = onCalendarDelete,
                    )
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                StaleCacheBanner(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .padding(horizontal = 16.dp),
                    visible = isCacheStale,
                )

                WeekDaysHeader(
                    modifier = Modifier
                        .padding(top = 8.dp),
                    weekStartDate = currentWeekStart,
                )

                AgendaGrid(
                    events = filteredEvents,
                    startHour = startHour,
                    weekStartDate = currentWeekStart,
                    onEventClick = { event -> selectedEvent = event },
                    onNextWeek = onNextWeek,
                    onPreviousWeek = onPreviousWeek,
                )
            }
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
    modifier: Modifier = Modifier,
    currentWeekStart: LocalDate,
    currentWeekEnd: LocalDate,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    calendars: List<CalendarConfig>,
    selectedCalendar: CalendarConfig?,
    onCalendarSelected: (CalendarConfig) -> Unit,
    onCalendarAdd: (String, String, Int?, (Boolean, String) -> Unit) -> Unit,
    onCalendarDelete: (Int) -> Unit,
) {
    var showCalendarDropdown by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<CalendarConfig?>(null) }
    var deleteTarget by remember { mutableStateOf<CalendarConfig?>(null) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Calendar selector (left side)
        Box {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.clickable { showCalendarDropdown = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = selectedCalendar?.name ?: stringResource(Res.string.agenda_no_calendar),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = showCalendarDropdown,
                onDismissRequest = { showCalendarDropdown = false }
            ) {
                calendars.forEach { cal ->
                    DropdownMenuItem(
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = cal.name,
                                    modifier = Modifier.weight(1f),
                                    fontWeight = if (cal.id == selectedCalendar?.id) FontWeight.Bold else FontWeight.Normal,
                                )
                                IconButton(
                                    onClick = {
                                        showCalendarDropdown = false
                                        renameTarget = cal
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = stringResource(Res.string.agenda_rename_calendar_cd),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        showCalendarDropdown = false
                                        deleteTarget = cal
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = stringResource(Res.string.agenda_delete_calendar_cd),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        onClick = {
                            onCalendarSelected(cal)
                            showCalendarDropdown = false
                        }
                    )
                }
                HorizontalDivider()
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(Res.string.agenda_add_calendar))
                        }
                    },
                    onClick = {
                        showCalendarDropdown = false
                        showAddDialog = true
                    }
                )
            }
        }

        // Date navigation
        Row(
            modifier = Modifier
                .dropShadow(
                    RoundedCornerShape(16.dp),
                    shadow = ShadowDefaults.Text
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(onClick = onPreviousWeek, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(Res.string.agenda_previous_week_cd)
                )
            }
            withDateFormatter {
                Text(
                    text = stringResource(
                        Res.string.agenda_date_range,
                        currentWeekStart.formatFriendlyDayMonth(),
                        currentWeekEnd.formatFriendlyDayMonth()
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(onClick = onNextWeek, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(Res.string.agenda_next_week_cd)
                )
            }
        }
    }

    // Add Calendar Dialog
    if (showAddDialog) {
        UpsertCalendarDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, linkId, calendarId, onResult ->
                onCalendarAdd(name, linkId, calendarId) { success, msg ->
                    onResult(success, msg)
                    if (success) showAddDialog = false
                }
            }
        )
    }

    // Rename Dialog
    renameTarget?.let { cal ->
        UpsertCalendarDialog(
            calendarEntryId = cal.id,
            initialName = cal.name,
            initialLinkId = cal.calendarLinkId,
            onDismiss = { renameTarget = null },
            onAdd = { name, linkId, calendarId, onResult ->
                onCalendarAdd(name, linkId, calendarId) { success, msg ->
                    onResult(success, msg)
                    if (success) renameTarget = null
                }
            }
        )
    }

    // Delete Confirmation Dialog
    deleteTarget?.let { cal ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(Res.string.agenda_delete_calendar_title)) },
            text = { Text(stringResource(Res.string.agenda_delete_calendar_message, cal.name)) },
            confirmButton = {
                TextButton(onClick = {
                    onCalendarDelete(cal.id)
                    deleteTarget = null
                }) {
                    Text(
                        stringResource(Res.string.action_delete),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            }
        )
    }
}

@Composable
private fun UpsertCalendarDialog(
    calendarEntryId: Int? = null,
    initialName: String = "",
    initialLinkId: String = "",
    onDismiss: () -> Unit,
    onAdd: (String, String, Int?, (Boolean, String) -> Unit) -> Unit,
) {
    var name by remember { mutableStateOf(initialName) }
    var linkInput by remember { mutableStateOf(initialLinkId) }
    var errorState by remember { mutableStateOf<UpsertErrorState?>(null) }
    var isValidating by remember { mutableStateOf(false) }

    val hexRegex = remember { Regex("^[a-fA-F0-9]{24}$") }
    val urlParamRegex = remember { Regex("linkCalendarioId=([a-fA-F0-9]+)") }

    fun extractCalendarLinkId(input: String): String? {
        val trimmed = input.trim()
        if (hexRegex.matches(trimmed)) return trimmed
        urlParamRegex.find(trimmed)?.let { return it.groupValues[1] }
        return null
    }

    val errorMessage = when (val err = errorState) {
        UpsertErrorState.EmptyName -> stringResource(Res.string.agenda_error_empty_name)
        UpsertErrorState.InvalidLink -> stringResource(Res.string.agenda_error_invalid_link)
        is UpsertErrorState.Remote -> err.message
        null -> null
    }

    AlertDialog(
        onDismissRequest = { if (!isValidating) onDismiss() },
        title = {
            Text(
                stringResource(
                    if (calendarEntryId == null) Res.string.agenda_add_calendar
                    else Res.string.agenda_edit_calendar
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(Res.string.agenda_calendar_name_label)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = linkInput,
                    onValueChange = {
                        linkInput = it
                        errorState = null
                    },
                    label = { Text(stringResource(Res.string.agenda_calendar_link_label)) },
                    singleLine = true,
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (isValidating) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            stringResource(Res.string.agenda_validating),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val extracted = extractCalendarLinkId(linkInput)
                    if (name.isBlank()) {
                        errorState = UpsertErrorState.EmptyName
                        return@TextButton
                    }
                    if (extracted == null) {
                        errorState = UpsertErrorState.InvalidLink
                        return@TextButton
                    }
                    isValidating = true
                    onAdd(name.trim(), extracted, calendarEntryId) { success, msg ->
                        isValidating = false
                        if (!success) errorState = UpsertErrorState.Remote(msg)
                    }
                },
                enabled = !isValidating,
            ) {
                Text(
                    stringResource(
                        if (calendarEntryId == null) Res.string.action_add
                        else Res.string.action_edit
                    )
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isValidating) {
                Text(stringResource(Res.string.action_cancel))
            }
        }
    )
}

@Composable
private fun WeekDaysHeader(
    modifier: Modifier = Modifier,
    weekStartDate: LocalDate
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 48.dp) // Space for time column
    ) {
        for (i in 0..4) { // Monday to Friday
            val date = weekStartDate.plus(i.toLong(), DateTimeUnit.DAY)
            val isToday = date == getToday()

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                withDateFormatter {
                    Text(
                        text = date.formatFriendlyDayOfWeek(),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun AgendaGrid(
    events: List<AgendaEntry>,
    weekStartDate: LocalDate,
    startHour: Int = 8,
    endHour: Int = 20,
    hourHeight: Dp = 60.dp,
    onEventClick: (AgendaEntry) -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
) {
    val verticalScrollState = rememberScrollState()
    var totalDrag = 0f
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
                        text = "${hour.toString().padStart(2, '0')}:00",
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
                .draggable(
                    state = rememberDraggableState { delta ->
                        totalDrag += delta
                    },
                    orientation = Orientation.Horizontal,
                    onDragStarted = {
                        totalDrag = 0f
                    },
                    onDragStopped = {
                        val threshold = 200f
                        if (totalDrag < -threshold) {
                            onNextWeek()
                        } else if (totalDrag > threshold) {
                            onPreviousWeek()
                        }
                        totalDrag = 0f
                    }
                )
        ) {
            for (dayOffset in 0..4) { // Monday to Friday
                val currentDate = weekStartDate.plus(dayOffset.toLong(), DateTimeUnit.DAY)
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
                maxLines = 5,
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
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = true,
        onBackCompleted = onDismiss
    )


    val scope = rememberCoroutineScope()
    // tack vertical offset
    val scrollState = rememberScrollState() // internal scroll state for the content
    var offsetY by remember { mutableFloatStateOf(0f) } // current vertical offset for drag gesture

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
                if (offsetY > 300f) {
                    // Dismiss case
                    offsetY = 0f
                    onDismiss()
                    return available
                } else if (offsetY > 0f) {
                    // Snap back case: User dragged it a bit, but not enough to dismiss.
                    // We launch the animation back to 0.
                    scope.launch {
                        // Animatable or simply updating the state in a loop/animateTo
                        // Assuming offsetY is a MutableState<Float>
                        animate(initialValue = offsetY, targetValue = 0f) { value, _ ->
                            offsetY = value
                        }
                    }
                    // Consume the velocity so the inner list doesn't scroll
                    // while the whole component is snapping back.
                    return available
                }
                return Velocity.Zero
            }
        }
    }
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
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
                        contentDescription = stringResource(Res.string.action_close_cd),
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
                    label = stringResource(Res.string.agenda_activity_code_label),
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
                        withDateFormatter {
                            Text(
                                text = event.getLocalDate().formatFriendlyFullDate()
                                    .replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stringResource(
                                    Res.string.agenda_time_range,
                                    event.getStartTime().formatFriendlyTime(),
                                    event.getEndTime().formatFriendlyTime()
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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
                                        text = stringResource(
                                            Res.string.agenda_room_capacity,
                                            capacity
                                        ),
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
                                text = stringResource(
                                    if (event.professors.size == 1) Res.string.agenda_professor_singular
                                    else Res.string.agenda_professor_plural
                                ),
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
                    label = stringResource(Res.string.agenda_course_year_label),
                    value = stringResource(Res.string.agenda_course_year_value, event.courseYear)
                )

                if (event.isRemote) {
                    DetailRow(
                        label = stringResource(Res.string.agenda_delivery_mode_label),
                        value = stringResource(Res.string.agenda_delivery_mode_remote)
                    )
                }

                // English title if available
                event.titleEn?.let { titleEn ->
                    DetailRow(
                        label = stringResource(Res.string.agenda_english_title_label),
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
            RoomLocation(
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
        title = "EPS A - ELEMENTI DI PROBABILITÀ E STATISTICA",
        titleEn = "DATABASES",
        startDateTime = Instant.parse("2026-02-16T10:00:00.000Z"),
        endDateTime = Instant.parse("2026-02-16T12:00:00.000Z"),
        activityCode = "INF0123",
        courseYear = 2,
        status = "P",
        rooms = listOf(
            RoomLocation(
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
            RoomLocation(id = "room3", code = "5S", name = "Aula 5S")
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
            RoomLocation(id = "room4", code = "2", name = "Aula 2")
        ),
        professors = listOf(
            Professor("prof4", "Anna", "BIANCHI")
        ),
        activityType = ActivityType("LEZ", "Lezione")
    )
)

@Composable
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
fun AgendaScreenContentPreview() {
    OpenUnitoTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            AgendaScreenContent(
                modifier = Modifier.padding(innerPadding),
                events = sampleEvents,
                currentWeekStart = getDayOfWeek(DayOfWeek.MONDAY, LocalDate(2026, 2, 16)),
            )
        }
    }
}

@Composable
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
fun EventDetailPreview() {
    OpenUnitoTheme {
        EventDetailView(
            event = sampleEvents[0],
            onDismiss = {}
        )
    }
}