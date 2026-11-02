package me.itzvirtual.openunito.ui.feature_home


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RollerSkating
import androidx.compose.material.icons.filled.SatelliteAlt
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.LocationOn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.itzvirtual.openunito.domain.agenda.AgendaEntry
import me.itzvirtual.openunito.domain.formatFriendlyTime
import me.itzvirtual.openunito.domain.getNowInstant
import me.itzvirtual.openunito.domain.student.StudentData
import me.itzvirtual.openunito.domain.student.studentDataSample
import me.itzvirtual.openunito.resources.ND
import me.itzvirtual.openunito.resources.Res
import me.itzvirtual.openunito.resources.error_no_calendar_configured
import me.itzvirtual.openunito.resources.open_schedule
import me.itzvirtual.openunito.resources.todays_classes_title
import me.itzvirtual.openunito.resources.tomorrow_classes_title
import me.itzvirtual.openunito.ui.components.ListWrapper
import me.itzvirtual.openunito.ui.components.TopBar
import me.itzvirtual.openunito.ui.feature_agenda.EventDetailView
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinViewModel(),
    onNavigateToSchedule: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    notchPadding: PaddingValues = PaddingValues(0.dp),
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    val showAttendance = state.showAttendance

    HomeScreenContent(
        modifier = modifier,
        notchPadding = notchPadding,
        studentState = state.studentState,
        todayClassesState = state.todaysClasses,
        tomorrowClassesState = state.tomorrowClasses,
        showAttendance = showAttendance,
        onNavigateToSchedule = onNavigateToSchedule,
        onOpenProfile = onOpenProfile,
        onClickAttendance = {} // TODO: Implement AttendanceTrackingScreen
    )
}

@Composable
fun HomeScreenContent(
    modifier: Modifier = Modifier,
    notchPadding: PaddingValues = PaddingValues(0.dp),
    todayClassesState: DailyClassesUiState,
    tomorrowClassesState: DailyClassesUiState,
    studentState: StudentDataUiState,
    showAttendance: Boolean?,
    debug: Boolean? = null,
    onNavigateToSchedule: () -> Unit = {},
    onOpenProfile: () -> Unit = {},
    onClickAttendance: () -> Unit = {}
) {
    // State for selected event detail view
    var selectedEvent by remember { mutableStateOf<AgendaEntry?>(null) }
    val insetPadding = 12.dp

    Scaffold(
        modifier = modifier,
        contentWindowInsets = WindowInsets(
            insetPadding,
            insetPadding,
            insetPadding,
            insetPadding,
        ),
        topBar = {
            TopBar(
                // TODO: do this for every screen
                colors = listOf(
                    MaterialTheme.colorScheme.onPrimary,
                    Color(0.29f, 0.251f, 0.467f, 1.0f)
//                    MaterialTheme.colorScheme.onTertiary.copy(green = 0.4f), // TODO: find a fucking cool color
//                    lerp(
//                        MaterialTheme.colorScheme.surface,
//                        MaterialTheme.colorScheme.onPrimary,
//                        0.2f
//                    ),
                ),
            ) {
                ProfileSection(
                    modifier = Modifier
                        .padding(12.dp)
                        .safeDrawingPadding(), // statusBarPadding
//                        .padding(notchPadding), // Apply padding only to content
                    data = studentState.data,
                    stateError = studentState.error,
                    debug = debug,
                    onOpenProfile = onOpenProfile
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                ClassesSection(
                    title = stringResource(Res.string.todays_classes_title),
                    classes = todayClassesState.classes,
                    isLoading = todayClassesState.isLoading,
                    stateError = todayClassesState.error,
                    onOpenScheduleClick = onNavigateToSchedule,
                    onEventClick = { event -> selectedEvent = event },
                    debug = debug,
                )
                Spacer(modifier = Modifier.height(32.dp))
                ClassesSection(
                    title = stringResource(Res.string.tomorrow_classes_title),
                    classes = tomorrowClassesState.classes,
                    isLoading = tomorrowClassesState.isLoading,
                    stateError = tomorrowClassesState.error,
                    onOpenScheduleClick = onNavigateToSchedule,
                    onEventClick = { event -> selectedEvent = event },
                    debug = debug,
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
//            // Attendance Tracking
//            if (showAttendance ?: false) { // TODO: Uncomment when showAttendance is ready
//                StdIconTextButton(
//                    icon = Icons.Default.HistoryEdu,
//                    text = stringResource(Res.string.attendance_tracking_button_name),
//                    onClick = onClickAttendance,
//                    modifier = Modifier
//                        .padding(0.dp, 0.dp)
//                        .height(90.dp),
//                    border = BorderStroke(2.dp, MaterialTheme.colorScheme.outlineVariant)
//                )
//            }
        }

        var displayedEvent by remember { mutableStateOf<AgendaEntry?>(null) }
        if (selectedEvent != null) {
            displayedEvent = selectedEvent
        }

        AnimatedVisibility(
            visible = selectedEvent != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it })
        ) {
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
fun UserProfileIcon(
    modifier: Modifier = Modifier,
    special: Int?,
    size: Dp = 40.dp
) {
    /*  Specials:
            null: everyone (Person)
            0: Mario
            1: Emi // Hopefully one day she'll have the app
            2: Anna // Beta tester #1
            3: Luca Neri // Genetics was cool but not present in this lib
    */
    val icon = when (special) {
        0 -> Icons.Default.SatelliteAlt
        1 -> Icons.Default.RollerSkating
        2 -> Icons.Default.BugReport
        3 -> Icons.Default.Translate // TODO: add library
        else -> Icons.Default.Person
    }

    Surface(
        modifier = modifier.size(size),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.size(29.dp),
                imageVector = icon,
                contentDescription = null,
            )
        }
    }
}

@Composable
fun ProfileSection(
    //TODO: pull state to top level
    modifier: Modifier = Modifier,
    data: StudentData? = null,
    stateError: String?,
    debug: Boolean? = null,
    onOpenProfile: () -> Unit,
) {
    val firstName = data?.firstname ?: stringResource(Res.string.ND)
    val lastName = data?.lastname ?: ""

    Box(
        modifier = Modifier
            .clickable(
                onClick = onOpenProfile,
            )
            .then(modifier)
    ) {
        if (debug ?: false && stateError != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp) // TODO: fix this padding shit
            ) {
                Text(
                    text = stateError,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        } else {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UserProfileIcon(
                        special = when {
                            firstName == "Mario" && lastName == "Rossi" -> 0 // TODO: REMOVE these name, maybe hash
                            firstName == "Maria" && lastName == "Bianchi" -> 1
                            firstName == "Anna" && lastName == "Verdi" -> 2
                            else -> null
                        }
                    )
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
    debug: Boolean?,
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
                    text = stringResource(Res.string.open_schedule),
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
                        if (stateError == "NO_CALENDAR") stringResource(Res.string.error_no_calendar_configured)
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
    // Determine the state of the class
    val now = getNowInstant()
    val isInProgress = now > entry.startDateTime && now < entry.endDateTime
    val isPast = now > entry.endDateTime

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
                            text = "${entry.startDateTime.formatFriendlyTime()} - ${
                                entry.endDateTime.formatFriendlyTime()
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
                        text = "${entry.startDateTime.formatFriendlyTime()} - ${
                            entry.endDateTime.formatFriendlyTime()
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

@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPreview() {
    OpenUnitoTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            HomeScreenContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                todayClassesState = DailyClassesUiState(classes = todaysClassesSample),
                tomorrowClassesState = DailyClassesUiState(classes = tomorrowClassesSample),
                studentState = StudentDataUiState(data = studentDataSample),
                showAttendance = true,
            )
        }
    }
}