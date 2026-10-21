package me.itzvirtual.openunito.ui.feature_examsessions


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.itzvirtual.openunito.domain.examdate.ExamDate
import me.itzvirtual.openunito.domain.examdate.examDateSample
import me.itzvirtual.openunito.domain.formatDate
import me.itzvirtual.openunito.ui.components.ListWrapper
import me.itzvirtual.openunito.ui.components.StdButtonEmpty
import me.itzvirtual.openunito.ui.components.StdEntry
import me.itzvirtual.openunito.ui.components.StdSurface
import me.itzvirtual.openunito.ui.components.StdTextButton
import me.itzvirtual.openunito.ui.shared.shimmerEffect
import me.itzvirtual.openunito.ui.shared.unShoutCourseName
import me.itzvirtual.openunito.ui.themes.LocalCustomColorsPalette
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
import org.koin.compose.viewmodel.koinViewModel
import kotlin.time.Instant

//Maybe call it ExamSession or just Session

/*
lista materie (codice):
    date (opt, orale/scritto):
        prenotabile dal:
        prenotabile fino al:
        if (prenotabile) prenota
 */

/*
//Which one is the fucking booked tag?

- "adsce_id":23948175,
- "ad_id":95183,
- "cod":"INF0328",
- "title":"MATEMATICA DISCRETA, ALGEBRA E GEOMETRIA",
- "description":"M.D.A.G- PARTE DI MATEMATICA DISCRETA",
- "note":"aule A+B+C+D+F",
- "date":"/Date(1800313200000+0100)/",
- "bookingStartDate":"/Date(1798585200000+0100)/",
- "bookingEndDate":"/Date(1799708400000+0100)/",
- "bookableExam":true,
- "bookingEnabled":false,
- "unbookingEnabled":false,
- "cds_app_id":14928,
- "ad_app_id":95183,
- "iscritti":0,
- "posiz":null,
- "adreg_id":null,
- "applista_id":null,
- "app_id":17,
- "tipo_app_cod":"PF",
- "ad_cod":"INF0328",
- "ad_des":"MATEMATICA DISCRETA, ALGEBRA E GEOMETRIA",
- "app_des":"M.D.A.G- PARTE DI MATEMATICA DISCRETA",
- "presidente":"PAOLA CONTI",
- "tipo_des":null,
- "stato_quest":1,
- "des_edificio":null,
- "des_aula":null,
- "dom_part_cod":null,
- "fat_part_cod":null,
- "turni":null,
- "sessioni":null,
- "misureComp":null,
- "tipiSvolgimentoEsame":null,
- "chkQuestVal":"0",
- "visCancPren":true
 */

@Composable
fun InvokeScreen(
    modifier: Modifier = Modifier,
    viewModel: ExamSessionViewModel = koinViewModel(),
    onBack: () -> Unit = {}
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()

    InvokeScreenContent(modifier, state, onBack = onBack)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvokeScreenContent(
    modifier: Modifier = Modifier,
    state: ExamDatesScreenState,
    onBack: () -> Unit = {},
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val uiState = state.examDatesState
    val dates = uiState.dates
    val debug = state.debug


    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Appelli", // TODO: add translation
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
                }
            )
        }

    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp)
                    .padding(0.dp),
            ) {
                StdTextButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    onClick = { selectedTab = 0 },
                    text = "Bookable", //TODO: translation
                    shape = RectangleShape,
                    tonalElevation = when (selectedTab) {
                        0 -> 2f.dp
                        else -> 0f.dp
                    }
                )
                StdTextButton(
                    modifier = Modifier
                        .weight(1f)
                        .height(80.dp),
                    onClick = { selectedTab = 1 },
                    text = "Booked", //TODO: translation
                    shape = RectangleShape,
                    tonalElevation = when (selectedTab) {
                        0 -> 0f.dp
                        else -> 4f.dp
                    }
                )
            }
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp),
            ) {
                when (selectedTab) {
                    0 -> {
                        ExamBookingList(
                            modifier = Modifier
                                .padding(8.dp),
                            exams = dates,
                            isLoading = uiState.isLoading,
                            stateError = uiState.error,
                            debug = debug,
                        )
                    }

                    1 -> {
                        ExamBookingList(
                            modifier = Modifier
                                .padding(8.dp),
                            exams = dates,
//                            dates = dates.filter {
//                            //TODO: filter for booked
//                        },
                            isLoading = uiState.isLoading,
                            stateError = uiState.error,
                            debug = debug
                        )
                    }
                }
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
    debug: Boolean,
) {
    ListWrapper(
        modifier = modifier,
        isEmpty = exams.isEmpty(),
        debug = debug,
        isLoading = isLoading,
        stateError = stateError
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
        ) {
            exams.forEach { exam ->
                item {
                    ExamSessionCard(
                        modifier = Modifier
                            .padding(bottom = 10.dp),
                        exam = exam
                    )
                }
            }
        }
    }
}

@Composable
fun ExamSessionCard(
    modifier: Modifier = Modifier,
    exam: ExamDate,
    onClick: () -> Unit = {}
) {
    var expanded = false

    val bookingEnabled = exam.bookingEnabled
    val booked = true

    val sideColor = when {
        !bookingEnabled -> Color.Gray
        bookingEnabled && !booked -> Color.Yellow
        booked -> LocalCustomColorsPalette.current.gradePassed
        else -> Color.DarkGray
    }

    val horizontalPadding = 18.dp

    StdSurface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClick = onClick
            ),
    ) {
        // TODO: Add *gesture on the face* etiquette to say if prenoted, not prenoted, prenotable
        // yellow: prenotable but not prenoted
        //  green: prenoted
        //   grey: unprenotable
        // italien!
        Box (
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.TopEnd,
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = horizontalPadding, vertical = 12.dp),
            ) {
                ExamSessionCardHeader(
                    modifier = Modifier
                        .fillMaxWidth(),
                    exam = exam,
                )

                AnimatedVisibility(
                    visible = expanded,
                ) {
                    ExamSessionCardExpandedContent(
                        exam = exam,
                    )
                }
            }
            MeasureHeight(
                viewToMeasure = {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = horizontalPadding, vertical = 12.dp),
                    ) {
                        ExamSessionCardHeader(
                            exam = exam,
                        )

                        AnimatedVisibility(
                            visible = expanded,
                        ) {
                            ExamSessionCardExpandedContent(
                                exam = exam,
                            )
                        }
                    }
                }
            ) { measuredHeight ->
                Surface (
                    modifier = Modifier
                        .width(horizontalPadding)
                        .height(measuredHeight),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    color = sideColor
                ) {}
            }

        }
    }
}

@Composable
fun MeasureHeight(
    viewToMeasure: @Composable () -> Unit,
    content: @Composable (measuredWidth: Dp) -> Unit,
) {
    SubcomposeLayout { constraints ->
        val measuredComponent = subcompose("viewToMeasure", viewToMeasure)[0]
            .measure(Constraints())

        val contentPlaceable = subcompose("content") {
            content(measuredComponent.height.toDp())
        }[0].measure(constraints)
        layout(contentPlaceable.width, contentPlaceable.height) {
            contentPlaceable.place(0,0)
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
            Text(
                text = exam.date.formatDate(),
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = unShoutCourseName(exam.title),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = unShoutCourseName(exam.description)
            )
        }
    }
}

@Composable
fun ExamSessionCardExpandedContent(
    modifier: Modifier = Modifier,
    exam: ExamDate,
) {
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
            startDate = exam.bookingStartDate,
            endDate = exam.bookingEndDate,
        )
        Spacer(Modifier.height(6.dp))
        BookingStartEndRow(exam.bookingStartDate, exam.bookingEndDate)

        Spacer(Modifier.height(4.dp))

        // Buttons
        Row (
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Spacer(
                modifier = Modifier.weight(0.1f)
            )

            AddCalendarButton()

            Spacer(
                modifier = Modifier.weight(0.15f)
            )

            PrenotButton(
                modifier = Modifier,
            )

            Spacer(
                modifier = Modifier.weight(0.1f)
            )
        }
    }
}

@Composable
fun InfoSection(
    activityCode: String,
    president: String,
    subscribedStudents: Int,
    note: String,
    startDate: Instant,
    endDate: Instant,
) {
    val weakColor = MaterialTheme.colorScheme.outline
    val weakWeight = FontWeight.Medium

    val strongColor = MaterialTheme.colorScheme.outline
    val strongWeight = FontWeight.Medium

    val size = 13.sp
    Column {
        // Activity code, sessions
        Row (
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
                    text = "Sessions: ",
                    color = weakColor,
                    fontWeight = weakWeight,
                    fontSize = size,
                    lineHeight = size.times(1.4f),
                )
                Text(
                    text = "Placeholder",
                    fontWeight = strongWeight,
                    fontSize = size,
                    lineHeight = size.times(1.4f),
                )
            }
        }

        Spacer(
            modifier = Modifier.height(4.dp),
        )

        // President, Subscribed
        Row (
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
        ) {
            Row(
                modifier = Modifier
                    .weight(1f),
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
            modifier = Modifier.height(4.dp),
        )

        // Session Notes
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

@Composable
fun BookingStartEndRow(
    startDate: Instant,
    endDate: Instant,
) {
    // TODO: Add translation
    val weakColor = MaterialTheme.colorScheme.outline
    val weakWeight = FontWeight.Light

    val strongColor = MaterialTheme.colorScheme.outline
    val strongWeight = FontWeight.Medium

    val size = 12.sp
    Row(
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = "Open from ",
            color = weakColor,
            fontWeight = weakWeight,
            fontSize = size,
        )
        Text(
            text = startDate.formatDate(),
            color = strongColor,
            fontWeight = strongWeight,
            fontSize = size,
        )
        Text(
            text = " to ",
            color = weakColor,
            fontWeight = weakWeight,
            fontSize = size,
        )
        Text(
            text = endDate.formatDate(),
            color = strongColor,
            fontWeight = strongWeight,
            fontSize = size,
        )
    }
}

// Copy-pasted from stackOverflow
@Composable
fun MeasureUnconstrainedViewWidth(
    viewToMeasure: @Composable () -> Unit,
    content: @Composable (measuredWidth: Dp) -> Unit,
) {
    SubcomposeLayout { constraints ->
        val measuredWidth = subcompose("viewToMeasure", viewToMeasure)[0]
            .measure(Constraints()).width.toDp()

        val contentPlaceable = subcompose("content") {
            content(measuredWidth)
        }[0].measure(constraints)
        layout(contentPlaceable.width, contentPlaceable.height) {
            contentPlaceable.place(0, 0)
        }
    }
}

@Composable
fun PrenotButton(
    modifier: Modifier = Modifier,
) {
    val book = "PRENOT"
    val revoke = "UNPRENOT"

    val goodRedColor = Color(
        217f / 256f,
        85f / 256f,
        85f / 256f
    ) // TODO: Make this into fkn error color, now it sucks

    // Ensure that Prenot button is large enough to contain both strings
    MeasureUnconstrainedViewWidth(
        viewToMeasure = {
            StdButtonEmpty(
                modifier = modifier
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 9.dp),
                    text = if (book.length > revoke.length) book else revoke,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) { measuredWidth ->
        StdButtonEmpty(
            modifier = modifier
                .width(measuredWidth),
            border = BorderStroke(1.5.dp, goodRedColor),
            onClick = {} // TODO: Add onclick prenot
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 8.dp, vertical = 9.dp),
                    text = "PRENOT",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AddCalendarButton(
    modifier: Modifier = Modifier,
) {

    val add = "Add"
    val remove = "Remove"

    val goodRedColor = Color(
        217f / 256f,
        85f / 256f,
        85f / 256f
    ) // TODO: Make this into fkn error color, now it sucks

    // Ensure that Prenot button is large enough to contain both strings
    MeasureUnconstrainedViewWidth(
        viewToMeasure = {
            Text(
                modifier = Modifier,
                text = if (add.length > remove.length) add else remove,
                fontWeight = FontWeight.Bold
            )
        }
    ) { measuredWidth ->
        StdButtonEmpty(
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.tertiary),
            onClick = {} // TODO: Add onclick addToCalendar
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 9.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,

                ) {
                Icon(
                    modifier = Modifier,
                    imageVector = Icons.Default.EditCalendar,
                    contentDescription = "suca",
                )
                Spacer(
                    modifier = Modifier.width(4.dp),
                )
                Text(
                    modifier = Modifier
                        .width(measuredWidth),
                    text = add, // TODO: Switch to remove when clicked
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

//--------------------
//------ Legacy ------
//--------------------
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
            Column(
            ) {
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
            Text( // TODO: Add translation
                text = "Open from ${bookingStartDate.formatDate()} to ${bookingEndDate.formatDate()}"
            )
        }
    }
}


// PREVIEW
@Composable
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
fun ProfileScreenPreview() {
    OpenUnitoTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            InvokeScreenContent(
                modifier = Modifier.padding(innerPadding),
                state = ExamDatesScreenState(
                    ExamDatesUiState(
                        dates = examDateSample
                    )
                )
            )
        }
    }
}

