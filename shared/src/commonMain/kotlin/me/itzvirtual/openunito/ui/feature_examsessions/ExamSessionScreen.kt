package me.itzvirtual.openunito.ui.feature_examsessions


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.itzvirtual.openunito.data.localization.rememberDateFormatter
import me.itzvirtual.openunito.data.localization.withDateFormatter
import me.itzvirtual.openunito.domain.examdate.ExamBookingState
import me.itzvirtual.openunito.domain.examdate.ExamDate
import me.itzvirtual.openunito.domain.examdate.ExamDateId
import me.itzvirtual.openunito.domain.examdate.examDateSample
import me.itzvirtual.openunito.domain.examdate.id
import me.itzvirtual.openunito.domain.examdate.subjectsListSample
import me.itzvirtual.openunito.resources.Res
import me.itzvirtual.openunito.resources.all
import me.itzvirtual.openunito.resources.examsession_open_from_to
import me.itzvirtual.openunito.resources.examsession_title
import me.itzvirtual.openunito.ui.components.GradientBackground
import me.itzvirtual.openunito.ui.components.ListWrapper
import me.itzvirtual.openunito.ui.components.ShadowDefaults
import me.itzvirtual.openunito.ui.components.StdButtonEmpty
import me.itzvirtual.openunito.ui.components.StdSurface
import me.itzvirtual.openunito.ui.shared.CalendarEventModel
import me.itzvirtual.openunito.ui.shared.annotatedStringResource
import me.itzvirtual.openunito.ui.shared.rememberCalendarLauncher
import me.itzvirtual.openunito.ui.shared.unShoutCourseName
import me.itzvirtual.openunito.ui.themes.LocalCustomColorsPalette
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Instant


enum class FilterState {
    ANY,
    BOOKED,
    UNBOOKED
}

@Composable
fun ExamSessionScreen(
    modifier: Modifier = Modifier,
    viewModel: ExamSessionViewModel = koinViewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    val uiState = state.examDatesState

    ExamSessionScreenContent(
        modifier = modifier,
        dates = uiState.dates,
        subjectsList = uiState.subjects,
        isLoading = uiState.isLoading,
        stateError = uiState.error,
        debug = state.debug,
        onBack = onBack,
    )
}

@Composable
fun ExamSessionScreenContent(
    modifier: Modifier = Modifier,
    dates: List<ExamDate>,
    subjectsList: List<String>,
    isLoading: Boolean = false,
    stateError: String? = null,
    debug: Boolean? = null,
    onBack: () -> Unit = {},
) {
    val selectedSubject = remember { mutableStateOf<Int?>(null) }
    var selectedFilter by remember { mutableStateOf(FilterState.ANY) }

    // Filter dates
    val filteredDates = dates
        .filter { // if selected == null don't, otherwise filter by subject
            selectedSubject.value == null || it.title == subjectsList[selectedSubject.value!!]
        }
        .filter {
            when (selectedFilter) {
                FilterState.BOOKED -> it.bookingState == ExamBookingState.BOOKED
                FilterState.UNBOOKED -> it.bookingState != ExamBookingState.BOOKED
                else -> true // ANY
            }
        }

    Scaffold(
        modifier = modifier,
        topBar = {
            GradientBackground(
                // TODO: do this for every screen
                colors = listOf(
                    MaterialTheme.colorScheme.onPrimary,
                    LocalCustomColorsPalette.current.onCareerColor
                ),
            ) {
                Column(
                    modifier = Modifier
                        .safeDrawingPadding()
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                modifier = Modifier
                                    .dropShadow(
                                        RoundedCornerShape(16.dp),
                                        shadow = ShadowDefaults.Text
                                    ),
                                text = stringResource(Res.string.examsession_title),
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                    contentDescription = "Back"
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors().copy(
                            containerColor = Color.Transparent,
                        )
                    )
                    FilterTopBar( // TODO: change colors to lighter ones
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        selectedSubject = selectedSubject,
                        subjectsList = subjectsList,
                        filter = selectedFilter,
                        onClickBooked = {
                            selectedFilter = when (selectedFilter) {
                                FilterState.BOOKED -> FilterState.ANY
                                else -> FilterState.BOOKED
                            }
                        },
                        onClickUnbooked = {
                            selectedFilter = when (selectedFilter) {
                                FilterState.UNBOOKED -> FilterState.ANY
                                else -> FilterState.UNBOOKED
                            }
                        }
                    )
                }
            }
        }

    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp),
            ) {
                ExamBookingList(
                    modifier = Modifier
                        .padding(horizontal = 8.dp),
                    exams = filteredDates,
                    isLoading = isLoading,
                    stateError = stateError,
                    debug = debug,
                )
            }
        }
    }
}

@Composable
fun FilterTopBar(
    modifier: Modifier = Modifier,
    selectedSubject: MutableState<Int?>,
    subjectsList: List<String>,
    filter: FilterState,
    onClickBooked: () -> Unit = {},
    onClickUnbooked: () -> Unit = {},
) {
    // TODO: translation
    val fontSize = 16.sp

    val selectedColor = MaterialTheme.colorScheme.primary
    val unselectedColor = MaterialTheme.colorScheme.surface

    Row(
        modifier = modifier,
    ) {
        SubjectDropdownMenu(
            modifier = Modifier
                .weight(0.9f)
                .padding(horizontal = 12.dp),
            selectedSubject = selectedSubject,
            subjectsList = subjectsList,
            fontSize = fontSize,
        )

//        Row(
//            modifier = Modifier
//                .weight(1f)
//                .padding(end = horPadding),
//        ) {
//            // Booked
//            StdButtonEmpty(
//                modifier = Modifier
//                    .weight(0.5f),
//                onClick = onClickBooked,
//                color = when (filter) {
//                    filterState.BOOKED -> selectedColor
//                    else -> unselectedColor
//                },
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp, 8.dp),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.Center
//                ) {
//                    Text(
//                        text = "Booked",
//                        fontSize = fontSize,
//                        fontWeight = FontWeight.Medium
//                    )
//                }
//            }
//
//            // Unbooked
//            StdButtonEmpty(
//                modifier = Modifier
//                    .weight(0.5f),
//                onClick = onClickUnbooked,
//                color = when (filter) {
//                    filterState.UNBOOKED -> selectedColor
//                    else -> unselectedColor
//                },
//            ) {
//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth()
//                        .padding(8.dp, 8.dp),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.Center
//                ) {
//                    Text(
//                        text = "Unbooked",
//                        fontSize = fontSize,
//                        fontWeight = FontWeight.Medium,
//                    )
//                }
//            }
//        }
    }
}

@Composable
fun SubjectDropdownMenu(
    modifier: Modifier = Modifier,
    subjectsList: List<String>,
    selectedSubject: MutableState<Int?>,
    fontSize: TextUnit = TextUnit.Unspecified,
) {
    var isDropDownExpanded by remember { mutableStateOf(false) }

    val color = when (selectedSubject.value) {
        null -> Color.White
        else -> Color.White
    }

    Box(
        modifier = modifier
    ) {
        StdButtonEmpty(
            modifier = Modifier
                .dropShadow(
                    RoundedCornerShape(16.dp),
                    shadow = ShadowDefaults.Surface
                ),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
            onClick = {
                isDropDownExpanded = true
            }
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    val smallFontSize = fontSize.times(0.65)

                    Text(
                        text = "Subjects", // TODO: add translations
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = smallFontSize,
                        lineHeight = smallFontSize.times(1.4),
                    )
                    Row {
                        Spacer(
                            modifier = Modifier.width(8.dp)
                        )
                        Text(
                            text = when (selectedSubject.value) {
                                null -> stringResource(Res.string.all)
                                else -> unShoutCourseName(subjectsList[selectedSubject.value!!])
                            },
                            fontSize = fontSize,
                            lineHeight = fontSize.times(1.4),
                            color = color
                        )
                    }
                }
                AnimatedContent(
                    targetState = isDropDownExpanded
                ) { scope ->
                    val rot = if (!scope) 90f else 270f
                    Icon(
                        modifier = Modifier
                            .rotate(rot),
                        imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                        contentDescription = null,
                        tint = Color.White,
                    )
                }
            }
        }
        DropdownMenu(
            expanded = isDropDownExpanded,
            onDismissRequest = { isDropDownExpanded = false },
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = stringResource(Res.string.all),
                    )
                },
                onClick = {
                    isDropDownExpanded = false
                    selectedSubject.value = null
                }
            )

            subjectsList.forEachIndexed { index, subjectString ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = unShoutCourseName(subjectString),
                        )
                    },
                    onClick = {
                        isDropDownExpanded = false
                        selectedSubject.value = index
                    }
                )
            }
        }
    }
}

@Composable
fun ExamBookingList(
    modifier: Modifier = Modifier,
    exams: List<ExamDate>,
    isLoading: Boolean,
    stateError: String?,
    debug: Boolean?,
) {
    var expandedExamId by remember { mutableStateOf<ExamDateId?>(null) }
    val formatter = rememberDateFormatter()
    val examsByMonth =
        remember(exams) { with(formatter) { exams.groupBy { exam -> exam.date.formatFullMonth() } } }

    ListWrapper(
        modifier = modifier,
        isEmpty = examsByMonth.isEmpty(),
        debug = debug,
        isLoading = isLoading,
        stateError = stateError
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            item {
                Spacer(
                    modifier = Modifier.height(8.dp)
                )
            }

            examsByMonth.forEach { (month, examList) ->
                item {
                    MonthCard(
                        modifier = Modifier
                            .padding(),
                        month = month.replaceFirstChar { // Capitalize month
                            if (it.isLowerCase()) it.titlecase() else it.toString()
                        }
                    )
                }

                examList.forEach { exam ->
                    item {
                        ExamSessionCard(
                            modifier = Modifier
                                .padding(bottom = 10.dp),
                            exam = exam,
                            expanded = exam.id() == expandedExamId,
                            onClick = {
                                val id = exam.id()
                                expandedExamId = if (expandedExamId != id) id else null
                            }
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun MonthCard(
    modifier: Modifier = Modifier,
    month: String,
) {
    val color = MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier,
    ) {
        Spacer(Modifier.height(6.dp))

//        HorizontalDivider(
//            color = color,
//            thickness = 1.dp
//        )
//
//        Spacer(Modifier.height(4.dp))
        // TODO: find a better fucking design, it terribly sucks
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = LocalCustomColorsPalette.current.careerColor,
            )
            Text(
                modifier = Modifier
                    .padding(horizontal = 8.dp),
                text = month,
                fontWeight = FontWeight.Medium,
                color = Color.White,
            )
        }
//        Spacer(Modifier.height(2.dp))

//        HorizontalDivider(
//            color = color,
//            thickness = 1.dp
//        )

        Spacer(Modifier.height(6.dp))
    }
}

@Composable
fun ExamSessionCard(
    modifier: Modifier = Modifier,
    exam: ExamDate,
    expanded: Boolean,
    onClick: () -> Unit,
) {
    // TODO: Add *gesture on the face* etiquette to say if prenoted, not prenoted, prenotable
    // yellow: prenotable but not prenoted
    //  green: prenoted
    //   grey: unprenotable
    // italien!
    val sideColor = when (exam.bookingState) {
        ExamBookingState.BOOKED -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val verPadding = 10.dp
    val horPadding = 16.dp

    val border = when {
//        exam.bookableExam -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        else -> BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    }


    StdSurface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            ),
        border = border,
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.TopEnd,
        ) {
            ExamSessionCardContent(
                modifier = Modifier
                    .padding(vertical = verPadding)
                    .padding(start = horPadding, end = horPadding),
                exam = exam,
                expanded = expanded,
            )

            // 2 - Circle Option

//            if (booked) {
//                Box(
//                    modifier = Modifier
//                        .size(40.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Surface(
//                        modifier = Modifier
//                            .size(18.dp),
//                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
//                        color = Color(0.3f,0.8f,0.0f),
//                        shape = CircleShape,
//
//                    ) {}
//                }
//            }

            // 3 - Sidebar Option

            // Measure Content height and feed it to the sidebar
//            MeasureHeightLayout(
//                viewToMeasure = {
//                    ExamSessionCardContent(
//                        modifier = Modifier
//                            .padding(vertical = verPadding)
//                            .padding(start = horPadding, end = horPadding * 2),
//                        exam = exam,
//                        expanded = expanded,
//                    )
//                }
//            ) { measuredHeight ->
//                Surface(
//                    modifier = Modifier
//                        .width(horPadding)
//                        .height(measuredHeight),
//                    color = sideColor
//                ) {}
//            }
        }
    }
}


@Composable
fun ExamSessionCardContent(
    // TODO: add booked tag
    modifier: Modifier = Modifier,
    exam: ExamDate,
    expanded: Boolean,
) {
    Column(
        modifier = modifier
    ) {
        ExamSessionCardHeader(
            modifier = Modifier
                .fillMaxWidth(),
            exam = exam,
        )

        AnimatedVisibility(
            visible = expanded,
        ) {
            ExamSessionCardDrawer(
                exam = exam,
            )
        }
    }
}

@Composable
fun ExamSessionCardHeader(
    modifier: Modifier = Modifier,
    exam: ExamDate,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            if (exam.title != exam.description) {
                Text(
                    text = unShoutCourseName(exam.title),
                    color = MaterialTheme.colorScheme.outline,
                    fontSize = 13.sp,
                    lineHeight = (13 * 1.47f).sp
                )
            }
            Text(
                text = unShoutCourseName(exam.description),
                fontWeight = FontWeight.Bold
            )
            withDateFormatter {
                Text(
                    text = exam.date.formatMediumDate(),
                    fontWeight = FontWeight.Bold,
                    color = LocalCustomColorsPalette.current.careerColor
                )
            }
        }
    }
}

@Composable
fun ExamSessionCardDrawer(
    modifier: Modifier = Modifier,
    exam: ExamDate,
) {
    val prenotWeight = 0.7f

    Column(
        modifier = modifier,
    ) {
        Spacer(Modifier.height(8.dp))

        HorizontalDivider()

        Spacer(Modifier.height(8.dp))

        InfoSection(
            activityCode = exam.activityCode,
            president = exam.president,
            subscribedStudents = exam.subscribedStudents,
            note = exam.note,
            spacing = 4.dp,
        )

        Spacer(Modifier.height(4.dp))

        BookingStartEndRow(exam.bookingStartDate, exam.bookingEndDate)

        Spacer(Modifier.height(12.dp))

        // Buttons
        val prenotWeight = 0.58f
        Row(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
//            Spacer(
//                modifier = Modifier.weight(0.1f)
//            )

            val calendarLauncher = rememberCalendarLauncher()

            AddCalendarButton(
                modifier = Modifier
                    .weight(1f - prenotWeight),
                onAddToCal = {
                    val event = CalendarEventModel(
                        title = exam.title,
                        description = exam.description,
                        startTimeMillis = exam.date.toEpochMilliseconds(),
                        endTimeMillis = exam.date.toEpochMilliseconds() + (60 * 60 * 1000),
                    )

                    calendarLauncher.addEvent(event)
                    // TODO: Add this exam to system calendar
                }
            )


            Spacer(
                modifier = Modifier.width(10.dp)
            )

            PrenotButton(
                // TODO: Add real vars
                modifier = Modifier
                    .weight(prenotWeight),
                editable = true,
                booked = exam.bookingState == ExamBookingState.BOOKED,
                onClick = { },
            )

//            Spacer(
//                modifier = Modifier.weight(0.1f)
//            )
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
fun InfoSection(
    activityCode: String,
    president: String,
    subscribedStudents: Int,
    note: String,
    spacing: Dp = 4.dp,
) {
    val weakColor = MaterialTheme.colorScheme.outline
    val weakWeight = FontWeight.Medium

    val strongColor = MaterialTheme.colorScheme.outline
    val strongWeight = FontWeight.Medium

    val size = 13.sp
    Column {
        // Activity code, Subscribed
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f),
            ) {
                Text(
                    text = "Activity Code: ",
                    color = weakColor,
                    fontWeight = weakWeight,
                    fontSize = size,
                    lineHeight = size.times(1.4f),
                )
                Text(
                    text = activityCode,
                    fontWeight = strongWeight,
                    fontSize = size,
                    lineHeight = size.times(1.4f),
                )
            }

            Row(
                modifier = Modifier
                    .weight(1f),
            ) {
                Text(
                    text = "Subscribed: ",
                    color = weakColor,
                    fontWeight = weakWeight,
                    fontSize = size,
                    lineHeight = size.times(1.4f),
                )
                Text(
                    text = subscribedStudents.toString(),
                    fontWeight = strongWeight,
                    fontSize = size,
                    lineHeight = size.times(1.4f),
                )
            }
        }

        Spacer(
            modifier = Modifier.height(spacing),
        )

        // President
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            Text(
                text = "President: ",
                color = weakColor,
                fontWeight = weakWeight,
                fontSize = size,
                lineHeight = size.times(1.4f),
            )
            Text(
                text = unShoutCourseName(president),
                fontWeight = strongWeight,
                fontSize = size,
                lineHeight = size.times(1.4f),
            )
        }

        Spacer(
            modifier = Modifier.height(spacing),
        )

        // Session Notes
        if (note != "") {
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "Notes: ",
                    color = weakColor,
                    fontWeight = weakWeight,
                    fontSize = size,
                    lineHeight = size.times(1.4f),
                )
                Text(
                    text = note,
                    fontWeight = strongWeight,
                    fontSize = size,
                    lineHeight = size.times(1.4f),
                )
            }
        }
    }
}

@Composable
fun BookingStartEndRow(
    startDate: Instant,
    endDate: Instant,
) {
    val weakStyle = SpanStyle(
        color = MaterialTheme.colorScheme.outline,
        fontWeight = FontWeight.Light
    )

    val strongStyle = SpanStyle(
        color = MaterialTheme.colorScheme.outline,
        fontWeight = FontWeight.Medium
    )

    val size = 12.sp
    Row(
        horizontalArrangement = Arrangement.Start
    ) {
        withDateFormatter {
            Text(
                text = annotatedStringResource(
                    Res.string.examsession_open_from_to,
                    weakStyle,
                    strongStyle,
                    startDate.formatDate(),
                    endDate.formatDate()
                ),
                fontSize = size,
                lineHeight = size.times(1.4f),
            )
        }
    }
}

@Composable
fun PrenotButton(
    modifier: Modifier = Modifier,
    editable: Boolean,
    booked: Boolean,
    onClick: () -> Unit,
) {
    val bookString = "Prenota"
    val revokeString = "UNPRENOT"

    val goodRedColor = Color( // TODO: Make this into fkn error color, now it sucks
        217f / 256f,
        85f / 256f,
        85f / 256f
    )

    val borderColor = when {
        editable -> LocalCustomColorsPalette.current.careerColor
        else -> LocalCustomColorsPalette.current.careerColor.copy(alpha = 0.5f)
    }
    val textColor = when {
        editable -> Color.Unspecified
        else -> Color.White.copy(alpha = 0.5f)
    }
    val textString = when {
        !booked -> bookString
        else -> revokeString
    }

    StdButtonEmpty(
        modifier = modifier,
        border = BorderStroke(2.dp, borderColor),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Text(
                modifier = Modifier,
                text = textString,
                fontWeight = FontWeight.Bold,
                color = textColor
            )

            Spacer(
                modifier = Modifier.weight(1.4f)
            )
        }
    }
}

@Composable
fun AddCalendarButton(
    modifier: Modifier = Modifier,
    onAddToCal: () -> Unit = {},
) {

    // Ensure that Prenot button is large enough to contain both strings
    StdButtonEmpty(
        modifier = modifier,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        onClick = onAddToCal,
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.EditCalendar, // TODO: make this addtoCal
                contentDescription = null,
            )

            Spacer(
                modifier = Modifier.weight(1f)
            )

            Text(
                text = "Aggiungi",
                fontWeight = FontWeight.Bold
            )
            Spacer(
                modifier = Modifier.weight(1.4f)
            )
        }
//        Row(
//            modifier = Modifier
//                .padding(8.dp)
//                .padding(vertical = 4.dp),
//            verticalAlignment = Alignment.CenterVertically,
//            horizontalArrangement = Arrangement.Center
//        ) {
//            Icon(
//                imageVector = Icons.Default.EventNote,
//                contentDescription = null,
//            )
//
//            Spacer(
//                Modifier.width(6.dp)
//            )
//
//            Text(
//                text = "Aggiungi",
//                fontWeight = FontWeight.Bold
//            )
//        }
    }
}

//--------------------
//------ Legacy ------
//--------------------
/*
@Composable
fun ExamBookingCard(
    modifier: Modifier = Modifier,
    dates: List<ExamDate>,
) {
    val firstDate = dates.get(0)

    StdSurface(
        modifier = modifier
            .fillMaxWidth(),
        border = BorderStroke(
            1.dp, MaterialTheme.colorScheme.primary
        )

    ) {
        Column(
            modifier = Modifier
                .padding(16.dp, 16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                ) {
                    Text(
                        text = unShoutCourseName(firstDate.title),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                ) {
                    Text(
                        text = firstDate.activityCode,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            dates.forEach { date ->
                Spacer(
                    modifier = Modifier
                        .height(8.dp)
                )
                HorizontalDivider()
                Spacer(
                    modifier = Modifier
                        .height(8.dp)
                )
                ExamSessionEntry(
                    modifier = Modifier,
                    date = date.date,
                    description = date.description,
                    bookable = date.bookableExam,
                    bookingStartDate = date.bookingStartDate,
                    bookingEndDate = date.bookingEndDate
                )
            }
        }
    }
}

@Composable
fun ExamSessionEntry(
    modifier: Modifier = Modifier,
    date: Instant,
    description: String,
    bookable: Boolean,
    bookingStartDate: Instant,
    bookingEndDate: Instant,
    onClick: () -> Unit = {},
) {
    StdEntry(
        modifier = modifier,
        onClick = if (bookable) onClick else {
            {}
        }
    ) {
        Column {
            Text(
                text = unShoutCourseName(description)
            )
            Text(
                text = date.formatDate(),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Open from ${bookingStartDate.formatDate()} to ${bookingEndDate.formatDate()}"
            )
        }
    }
}
*/


// PREVIEW
@Composable
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
fun ProfileScreenPreview() {
    OpenUnitoTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            ExamSessionScreenContent(
                modifier = Modifier.padding(innerPadding),
                dates = examDateSample,
                subjectsList = subjectsListSample,
            )
        }
    }
}

@Composable
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
fun ExamCardPreview() {
    OpenUnitoTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            ExamSessionCard(
                exam = examDateSample[0],
                expanded = true,
                onClick = {},
            )
        }
    }
}