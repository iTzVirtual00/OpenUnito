package me.itzvirtual.myunitox.ui.feature_home

import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.itzvirtual.myunitox.R
import me.itzvirtual.myunitox.domain.agenda.ActivityType
import me.itzvirtual.myunitox.domain.agenda.AgendaEntry
import me.itzvirtual.myunitox.domain.agenda.Professor
import me.itzvirtual.myunitox.domain.agenda.RoomLocation
import me.itzvirtual.myunitox.domain.student.sampleStudentData
import me.itzvirtual.myunitox.ui.components.ListWrapper
import me.itzvirtual.myunitox.ui.components.StdIconTextButton
import me.itzvirtual.myunitox.ui.feature_agenda.EventDetailView
import me.itzvirtual.myunitox.ui.feature_examsessions.InvokeScreen
import me.itzvirtual.myunitox.ui.themes.MyUnitoXTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
//TODO: "Back" button goes to previous tab

sealed interface HomeDestination {
    data object Main : HomeDestination
    data object ProfileScreen : HomeDestination
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToSchedule: () -> Unit = {}
) {
    var showAttendancesTracking by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val backStack = remember { mutableStateListOf<HomeDestination>(HomeDestination.Main) }
    val current = backStack.last()
    BackHandler(enabled = backStack.size > 1) {
        backStack.removeAt(backStack.lastIndex)
    }

    if (showAttendancesTracking) {
        //TODO: call attendance tracking
        Toast.makeText(
            context,
            "WIP",
            Toast.LENGTH_SHORT
        ).show()
        showAttendancesTracking = false
    }


    AnimatedContent(
        targetState = current,
        modifier = modifier.fillMaxSize(),
        transitionSpec = {
            // Compare where the screens live in the stack
            val initialIndex = backStack.indexOf(initialState)
            val targetIndex = backStack.indexOf(targetState)
            val isBack = targetIndex < initialIndex || initialIndex == -1

            if (isBack) {
                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                    slideOutHorizontally { width -> width } + fadeOut()
            } else {
                slideInHorizontally { width -> width } + fadeIn() togetherWith
                    slideOutHorizontally { width -> -width } + fadeOut()
            }
        },
        label = "HomeStackTransition"
    ) { destination ->
        when (destination) {
            is HomeDestination.Main -> {
                val state by viewModel.screenState.collectAsStateWithLifecycle()
                HomeScreenContent(
                    state = state,
                    modifier = modifier,
                    onNavigateToSchedule = onNavigateToSchedule,
                    onOpenProfile = {
                        backStack.add(HomeDestination.ProfileScreen)
                    },
                    onClickAttendance = {
                        showAttendancesTracking = true;
                    }
                )
            }

            is HomeDestination.ProfileScreen -> {
                ProfileScreenContent(
                    onBack = { backStack.removeAt(backStack.lastIndex) }
                )
            }
        }
    }
}

@Composable
fun HomeScreenContent(
    state: HomeScreenState,
    modifier: Modifier = Modifier,
    onNavigateToSchedule: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onClickAttendance: () -> Unit = {}
) {
    val showAttendance = true // TODO: add this in settings

    val studentState = state.studentState
    val todayClassesState = state.todaysClasses
    val tomorrowClassesState = state.tomorrowClasses


    // State for selected event detail view
    // What does this even mean?
    var selectedEvent by remember { mutableStateOf<AgendaEntry?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                ProfileSection(state, Modifier.clickable {
                    onOpenProfile()
                })
                Spacer(modifier = Modifier.height(16.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    ClassesSection(
                        title = stringResource(R.string.todays_classes_title),
                        classes = todayClassesState.classes,
                        isLoading = todayClassesState.isLoading,
                        stateError = todayClassesState.error,
                        onOpenScheduleClick = onNavigateToSchedule,
                        onEventClick = { event -> selectedEvent = event },
                        debug = state.debug,
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    ClassesSection(
                        title = stringResource(R.string.tomorrow_classes_title),
                        classes = tomorrowClassesState.classes,
                        isLoading = tomorrowClassesState.isLoading,
                        stateError = tomorrowClassesState.error,
                        onOpenScheduleClick = onNavigateToSchedule,
                        onEventClick = { event -> selectedEvent = event },
                        debug = state.debug,
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
                if (showAttendance) {
                    StdIconTextButton(
                        icon = Icons.Default.HistoryEdu, //Maybe change with EventAvailable
                        text = stringResource(R.string.attendance_tracking_button_name),
                        onClick = onClickAttendance,
                        modifier = Modifier.padding(0.dp, 0.dp),
                        height = 80f,
                        border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }
        // Class detail overlay
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreenContent(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
//            ProfileScreen()// TODO: restore this
            InvokeScreen()
        }
    }
}

@Composable
fun UserInitialsIcon(
    initials: String,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = initials,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun ProfileSection( //TODO: pull state to top level
    homeState: HomeScreenState,
    modifier: Modifier = Modifier
) {
    val state = homeState.studentState
    val debug = homeState.debug

    val initials: String
    val firstName: String
    val lastName: String

    if (state.data != null) {
        val studentData = state.data
        initials =
            "${studentData.firstname.firstOrNull() ?: ""}${studentData.lastname.firstOrNull() ?: ""}".uppercase()
        firstName = studentData.firstname
        lastName = studentData.lastname
    }
    else {
        initials = "/"
        firstName = "Profile" //add translation
        lastName = ""
    }


    Box(modifier = modifier) {
        if (debug && state.error != null) {
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
        else {
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
                        text = "$firstName $lastName",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

}

@Composable
fun ClassesSection(
    title: String,
    modifier: Modifier = Modifier,
    classes: List<AgendaEntry>,
    isLoading: Boolean,
    stateError: String?,
    onOpenScheduleClick: () -> Unit,
    onEventClick: (AgendaEntry) -> Unit,
    debug: Boolean,
) {


    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
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

                ListWrapper(
                    isEmpty = classes.isEmpty(),
                    debug = debug,
                    isLoading = isLoading,
                    stateError =
                        if (stateError == "NO_CALENDAR") stringResource(R.string.error_no_calendar_configured)
                        else stateError
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        classes.forEach { entry ->
                            ClassCard(
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

@Composable
fun ClassCard(
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
            HomeScreenContent(
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