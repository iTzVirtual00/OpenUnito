package me.itzvirtual.openunito.ui.feature_examsessions


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.text.font.FontWeight
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
import me.itzvirtual.openunito.ui.shared.unShoutCourseName
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
                    SingleExamSessionCard(
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
fun SingleExamSessionCard(
    modifier: Modifier = Modifier,
    exam: ExamDate,
    onClick: () -> Unit = {}
) {
    var expanded = true

    StdSurface(
        modifier = modifier
            .fillMaxWidth(),
//        border = BorderStroke( // with or without border?
//            0.dp, Color.Transparent
//        )
    ) {
        // TODO: Add *gesture on the face* etiquette to say if prenoted, not prenoted, prenotable
        // yellow: prenotable but not prenoted
        //  green: prenoted
        //   grey: unprenotable
        // italien!
        val arrowWeight = 0.12f

        Column (
            modifier = Modifier

                .padding(16.dp, 12.dp),
            ) {
            Row(
                modifier = Modifier
                    .clickable(
                        onClick = onClick
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1 - arrowWeight)
                ) {
                    val a = 12.sp
                    Text(
                        text = exam.activityCode,
                        fontSize = a,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.outline,
                        lineHeight = a.times(1.4f),
                    )
                    Text(
                        text = unShoutCourseName(exam.title),
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = unShoutCourseName(exam.description)
                    )
                    Text(
                        text = exam.date.formatDate(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    modifier = Modifier
                        .weight(arrowWeight)
                        .rotate(90f)
                )
            }
/*
Corso
    Prog 2
Presidente
    Barbieri
Dal al
Sessioni:
    2025/2026 - SESSIONE
Note appello?

ADD TO CALENDAR!!
 */

            Column () {
                Spacer(Modifier.height(8.dp))
                HorizontalDivider()
                Spacer(Modifier.height(8.dp))
                InfoSection(
                    president = exam.president,
                    note = exam.note
                )
                BookingEndStartRow(exam.bookingStartDate, exam.bookingEndDate)
                Spacer(Modifier.height(8.dp))

                PrenotButton()
            }
        }
    }
}

@Composable
fun InfoSection(
    president: String,
    note: String,
) {
    val weakColor = MaterialTheme.colorScheme.outline
    val weakWeight = FontWeight.Medium

    val strongColor = MaterialTheme.colorScheme.outline
    val strongWeight = FontWeight.Medium

    val size = 13.sp
    Column {
        //President
        Row {
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
            modifier = Modifier.height(4.dp),
        )
        //Notes
        Row (
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
fun BookingEndStartRow(
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
    ){
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
fun PrenotButton() {

    val book = "PRENOT"
    val revoke = "UNPRENOT"

    val goodRedColor = Color(217f/256f, 85f/256f, 85f/256f) // TODO: Make this into fkn error color, now it sucks

    Row (
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        // Ensure that Prenot button is large enough to contain both strings
        MeasureUnconstrainedViewWidth (
            viewToMeasure = {
                StdButtonEmpty() {
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
                modifier = Modifier
                    .width(measuredWidth),
                border = BorderStroke(1.5.dp, goodRedColor),
                onClick = {} // TODO: Add onclick prenot
            ) {
                Row (
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

