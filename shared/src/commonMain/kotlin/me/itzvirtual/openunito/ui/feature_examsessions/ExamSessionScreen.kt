package me.itzvirtual.openunito.ui.feature_examsessions



import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.itzvirtual.openunito.domain.examdate.ExamDate
import me.itzvirtual.openunito.domain.examdate.examDateSample
import me.itzvirtual.openunito.ui.components.ListWrapper
import me.itzvirtual.openunito.ui.components.StdEntry
import me.itzvirtual.openunito.ui.components.StdSurface
import me.itzvirtual.openunito.ui.components.StdTextButton
import me.itzvirtual.openunito.ui.shared.formatDate
import me.itzvirtual.openunito.ui.shared.unShoutCourseName
import me.itzvirtual.openunito.ui.themes.OpenUnitoTheme
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
    viewModel: ExamSessionViewModel = koinViewModel()
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()

    InvokeScreenContent(modifier, state)
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
                        .weight(1f),
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
                        .weight(1f),
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
                            subjects = dates,
                            isLoading = uiState.isLoading,
                            stateError = uiState.error,
                            debug = debug,
                        )
                    }

                    1 -> {
                        ExamBookingList(
                            subjects = dates,
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
    subjects: HashMap<String, List<ExamDate>>,
    isLoading: Boolean,
    stateError: String?,
    debug: Boolean,
) {
    ListWrapper(
        isEmpty = subjects.isEmpty(),
        debug = debug,
        isLoading = isLoading,
        stateError = stateError
    ) {
        LazyColumn(
            modifier = modifier
                .fillMaxSize(),
        ) {
            subjects.forEach { (string, subject) ->
                item {
                    Spacer(
                        modifier = Modifier.height(6.dp)
                    )
                    ExamBookingCard(
                        dates = subject
                    )
                }
            }
        }
    }
}

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
        onClick = if (bookable) onClick else { {} }
    ) {
        Column {
            Text(
                text = unShoutCourseName(description)
            )
            Text(
                text = formatDate(date),
                fontWeight = FontWeight.Bold
            )
            Text( // TODO: Add translation
                text = "Open from ${formatDate(bookingStartDate)} to ${formatDate(bookingEndDate)}"
            )
        }
    }
}

@Composable
@Preview(showBackground = true)
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

