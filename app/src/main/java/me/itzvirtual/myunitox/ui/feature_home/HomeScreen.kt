package me.itzvirtual.myunitox.ui.feature_home

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.itzvirtual.myunitox.R
import me.itzvirtual.myunitox.domain.agenda.ActivityType
import me.itzvirtual.myunitox.domain.agenda.AgendaEntry
import me.itzvirtual.myunitox.domain.agenda.Professor
import me.itzvirtual.myunitox.domain.agenda.RoomLocation
import me.itzvirtual.myunitox.domain.student.sampleStudentData
import me.itzvirtual.myunitox.ui.feature_agenda.EventDetailView
import me.itzvirtual.myunitox.ui.feature_career.UserInitialsIcon
import me.itzvirtual.myunitox.ui.themes.MyUnitoXTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun HomeScreenPreview() {
    val sampleTodaysClasses = listOf(
        AgendaEntry(
            id = "0",
            title = "Digital Thinking",
            startDateTime = Instant.parse("2026-02-22T06:00:00.000Z"),
            endDateTime = Instant.parse("2026-02-22T08:00:00.000Z"),
            activityCode = "DT001",
            courseYear = 1,
            status = "P",
            rooms = listOf(
                RoomLocation(id = "room1", code = "MA", name = "Main auditorium")
            ),
            professors = listOf(
                Professor("prof1", "Elena", "Galli")
            ),
            activityType = ActivityType("LEZ", "Lezione")
        ),
        AgendaEntry(
            id = "1",
            title = "Digital Thinking",
            startDateTime = Instant.parse("2026-02-22T08:00:00.000Z"),
            endDateTime = Instant.parse("2026-02-22T10:00:00.000Z"),
            activityCode = "DT001",
            courseYear = 1,
            status = "P",
            rooms = listOf(
                RoomLocation(id = "room1", code = "MA", name = "Main auditorium")
            ),
            professors = listOf(
                Professor("prof1", "Elena", "Galli")
            ),
            activityType = ActivityType("LEZ", "Lezione")
        ),
        AgendaEntry(
            id = "2",
            title = "Computer Science",
            startDateTime = Instant.parse("2026-02-22T11:00:00.000Z"),
            endDateTime = Instant.parse("2026-02-22T13:00:00.000Z"),
            activityCode = "CS001",
            courseYear = 1,
            status = "P",
            rooms = listOf(
                RoomLocation(id = "room2", code = "A2", name = "Aula 2")
            ),
            professors = listOf(
                Professor("prof2", "Marco", "Rossi")
            ),
            activityType = ActivityType("LEZ", "Lezione")
        ),
        AgendaEntry(
            id = "2",
            title = "Kitchen",
            startDateTime = Instant.parse("2026-02-22T19:00:00.000Z"),
            endDateTime = Instant.parse("2026-02-22T23:30:00.000Z"),
            activityCode = "CS001",
            courseYear = 1,
            status = "P",
            rooms = listOf(
                RoomLocation(id = "room2", code = "A2", name = "Aula 2")
            ),
            professors = listOf(
                Professor("prof2", "Marco", "Rossi")
            ),
            activityType = ActivityType("LEZ", "Lezione")
        ),
        AgendaEntry(
            id = "2",
            title = "Sleep",
            startDateTime = Instant.parse("2026-02-22T23:40:00.000Z"),
            endDateTime = Instant.parse("2026-02-23T07:40:00.000Z"),
            activityCode = "CS001",
            courseYear = 1,
            status = "P",
            rooms = listOf(
                RoomLocation(id = "room2", code = "A2", name = "Aula 2")
            ),
            professors = listOf(
                Professor("prof2", "Marco", "Rossi")
            ),
            activityType = ActivityType("LEZ", "Lezione")
        ),
    )
    val sampleTomorrowClasses = listOf(
        AgendaEntry(
            id = "3",
            title = "Mathematics",
            startDateTime = Instant.parse("2026-02-23T08:00:00.000Z"),
            endDateTime = Instant.parse("2026-02-23T10:00:00.000Z"),
            activityCode = "MATH001",
            courseYear = 1,
            status = "P",
            rooms = listOf(
                RoomLocation(id = "room3", code = "B1", name = "Aula B1")
            ),
            professors = listOf(
                Professor("prof3", "Luca", "Bianchi")
            ),
            activityType = ActivityType("LEZ", "Lezione")
        ),
        AgendaEntry(
            id = "3",
            title = "Mathematics",
            startDateTime = Instant.parse("2026-02-23T10:00:00.000Z"),
            endDateTime = Instant.parse("2026-02-23T12:00:00.000Z"),
            activityCode = "MATH001",
            courseYear = 1,
            status = "P",
            rooms = listOf(
                RoomLocation(id = "room3", code = "B1", name = "Aula B1")
            ),
            professors = listOf(
                Professor("prof3", "Luca", "Bianchi")
            ),
            activityType = ActivityType("LEZ", "Lezione")
        ),
    )
    MyUnitoXTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            HomeScreen(
                state = HomeScreenState(
                    studentState = StudentDataUiState(data = sampleStudentData),
                    todaysClasses = DailyClassesUiState(classes = sampleTodaysClasses),
                    tomorrowClasses = DailyClassesUiState(classes = sampleTomorrowClasses)
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }

}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSchedule: () -> Unit = {}
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    HomeScreen(state = state, modifier = modifier, onNavigateToSchedule = onNavigateToSchedule)
}

@Composable
fun StudentDataSection(homeState: HomeScreenState, modifier: Modifier = Modifier) {
    var state = homeState.studentState
    Box(modifier = modifier) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }

            state.error != null -> {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = state.error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            else -> {
                val studentData = state.data!!
                val initials =
                    "${studentData.firstname.firstOrNull() ?: ""}${studentData.lastname.firstOrNull() ?: ""}".uppercase()
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        UserInitialsIcon(initials)
                        Text(
                            text = "${studentData.firstname} ${studentData.lastname}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }

}

@Composable
fun HomeScreen(
    state: HomeScreenState,
    modifier: Modifier = Modifier,
    onNavigateToSchedule: () -> Unit = {}
) {
    val studentState = state.studentState
    val todaysClassesState = state.todaysClasses
    val tomorrowClassesState = state.tomorrowClasses

    // State for selected event detail view
    var selectedEvent by remember { mutableStateOf<AgendaEntry?>(null) }
    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
        ) {
            item {
                when {
                    studentState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    studentState.error != null -> {
                        Text(
                            text = studentState.error,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    studentState.data != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        ) {
                            StudentDataSection(state)
                            Spacer(modifier = Modifier.height(16.dp))
                            TodaysClassesSection(
                                state = todaysClassesState,
                                onOpenScheduleClick = onNavigateToSchedule,
                                onEventClick = { event -> selectedEvent = event },
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            TomorrowClassesSection(
                                state = tomorrowClassesState,
                                onOpenScheduleClick = onNavigateToSchedule,
                                onEventClick = { event -> selectedEvent = event },
                            )
                            Spacer(modifier = Modifier.height(32.dp))
                            Row(
                                modifier = modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(
                                    onClick = {},
                                    modifier = modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp),
                                    //border = borderStroke
                                ) {}
                            }
                        }
                    }
                }
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

// TODO: generalize classes sections since today and tomorrow classes are almost identical
@Composable
fun TodaysClassesSection(
    state: DailyClassesUiState,
    onOpenScheduleClick: () -> Unit,
    onEventClick: (AgendaEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.todays_classes_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.clickable { onOpenScheduleClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.open_schedule),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Classes list
                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    }

                    state.error != null -> {
                        Surface(
                            modifier = modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                        ) {
                            val errMsg = if (state.error == "NO_CALENDAR") {
                                stringResource(R.string.error_no_calendar_configured)
                            } else {
                                state.error
                            }
                            Row(modifier.padding(12.dp)) {
                                Text(
                                    text = errMsg,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    state.classes.isEmpty() -> {
                        Surface(
                            modifier = modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                        ) {
                            Row(modifier.padding(12.dp)) {
                                Text(
                                    text = stringResource(R.string.no_classes_today),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    else -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            state.classes.forEach { entry ->
                                TodaysClassCard(
                                    entry = entry,
                                    onClick = { onEventClick(entry) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TomorrowClassesSection(
    state: DailyClassesUiState,
    onOpenScheduleClick: () -> Unit,
    onEventClick: (AgendaEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Header row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.tomorrow_classes_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.clickable { onOpenScheduleClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.open_schedule),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Classes list
                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        }
                    }

                    state.error != null -> {
                        Surface(
                            modifier = modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                        ) {
                            Row(modifier.padding(12.dp)) {
                                val errMsg = if (state.error == "NO_CALENDAR") {
                                    stringResource(R.string.error_no_calendar_configured)
                                } else {
                                    state.error
                                }
                                Text(
                                    text = errMsg,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    state.classes.isEmpty() -> {
                        Surface(
                            modifier = modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                        ) {
                            Row(modifier.padding(12.dp)) {
                                Text(
                                    text = stringResource(R.string.no_classes_tomorrow),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    else -> {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            state.classes.forEach { entry ->
                                TodaysClassCard(
                                    entry = entry,
                                    onClick = { onEventClick(entry) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodaysClassCard(
    entry: AgendaEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
        .withZone(ZoneId.systemDefault())

    // Determine the state of the class
    val now = Instant.now()
    val isInProgress = now.isAfter(entry.startDateTime) && now.isBefore(entry.endDateTime)
    val isPast = now.isAfter(entry.endDateTime)

    // Styling based on state
    val backgroundColor = when {
        isPast -> MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)
        else -> MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
    }
    val contentColor = when {
        isPast -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        else -> MaterialTheme.colorScheme.onSurface
    }
    val secondaryContentColor = when {
        isPast -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val borderStroke = if (isInProgress) {
        BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
    } else {
        null
    }

    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        shadowElevation = 2.dp,
        border = borderStroke
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isPast) 12.dp else 16.dp)
        ) {
            // Class title - always shown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.title,
                    style = if (isPast) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium,
                    fontWeight = if (isPast) FontWeight.Medium else FontWeight.Bold,
                    fontStyle = if (isPast) FontStyle.Italic else FontStyle.Normal,
                    textDecoration = if (isPast) TextDecoration.LineThrough else TextDecoration.None,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                // For past entries, show time inline with title
                if (isPast) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.AccessTime,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = secondaryContentColor
                        )
                        Text(
                            text = "${timeFormatter.format(entry.startDateTime)} - ${
                                timeFormatter.format(
                                    entry.endDateTime
                                )
                            }",
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryContentColor
                        )
                    }
                }
            }

            // For non-past entries, show full details
            if (!isPast) {
                Spacer(modifier = Modifier.height(4.dp))

                // Time and loc row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = secondaryContentColor
                    )
                    Text(
                        text = "${timeFormatter.format(entry.startDateTime)} - ${
                            timeFormatter.format(
                                entry.endDateTime
                            )
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryContentColor
                    )

                    // Location - shown inline if available
                    if (entry.rooms.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = secondaryContentColor
                        )
                        Text(
                            text = entry.rooms.first().name,
                            style = MaterialTheme.typography.bodySmall,
                            color = secondaryContentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Professor name
                if (entry.professors.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = entry.professors.joinToString(", ") { it.fullName },
                        style = MaterialTheme.typography.bodySmall,
                        color = secondaryContentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
