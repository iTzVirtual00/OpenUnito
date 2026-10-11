package me.itzvirtual.myunitox.ui.feature_examdates


import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.itzvirtual.myunitox.domain.examdate.ExamDate
import me.itzvirtual.myunitox.ui.components.ListWrapper
import me.itzvirtual.myunitox.ui.components.StdSurface
import me.itzvirtual.myunitox.ui.components.StdTextButton
import me.itzvirtual.myunitox.ui.themes.MyUnitoXTheme
import java.util.Date

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
    viewModel: ExamSessionViewModel = hiltViewModel()
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()

    InvokeScreenContent(modifier, state)
}

@Composable
fun InvokeScreenContent(
    modifier: Modifier = Modifier,
    state: ExamDatesScreenState
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val uiState = state.examDatesState
    val dates = uiState.dates
    val debug = state.debug


    Column() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
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
                    else -> 1f.dp
                }
            )
            StdTextButton(
                modifier = Modifier
                    .weight(1f),
                onClick = { selectedTab = 1 },
                text = "Booked", //TODO: translation
                shape = RectangleShape,
                tonalElevation = when (selectedTab) {
                    0 -> 1f.dp
                    else -> 4f.dp
                }
            )
        }
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(0.dp),
            tonalElevation = 2f.dp
        ) {
            when (selectedTab) {
                0 -> {
                    ExamBookingList(
                        subjects = dates,
                        //                            dates = dates.filter {
                        //                            //TODO: filter for NOT booked
                        //                        },
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
                .fillMaxSize()
                .padding(8.dp, 0.dp)
        ) {

            subjects.forEach { string, subject ->
                item {
                    Spacer(
                        modifier = Modifier.height(12.dp)
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
    dates: List<ExamDate>,
) {
    StdSurface(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = dates.get(0).title
                )
            }
            dates.forEach { date ->
                ExamSessionEntry( //TODO: smthing doesn't work
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
    date: Date,
    description: String,
    bookable: Boolean,
    bookingStartDate: Date,
    bookingEndDate: Date,
) {
    StdSurface(
        modifier = Modifier
            .height(90.dp)
    ) {
        Column() {
            Text(
                text = date.toString(),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description
            )
            Text(
                text = "Pren from ${bookingStartDate} to ${bookingEndDate}"
            )
            Row() {
                StdTextButton(
                    text = "Prenot",
                    onClick = {},
                )
                StdTextButton(
                    text = "Prenot",
                    onClick = {},
                )
            }
        }
    }
}

@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
fun ProfileScreenPreview() {
    MyUnitoXTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            InvokeScreenContent(
                modifier = Modifier.padding(innerPadding),
                state = ExamDatesScreenState(
                    ExamDatesUiState(
                        dates = mapOf<String, List<ExamDate>>(
                            "mate" to List(2) {
                                ExamDate(
                                    title = "mate",
                                    description = "cazzo ne so",
                                    note = "",
                                    adCode = "INF001",
                                    adDescription = "",
                                    appDescription = "",
                                    date = Date(),
                                    bookingStartDate = Date(),
                                    bookingEndDate = Date(),
                                    bookableExam = true,
                                    bookingEnabled = true,
                                    unbookingEnabled = true,
                                    students = 0,
                                    president = "Conti",
                                )
                                ExamDate(
                                    "mate",
                                    "cazzo ne so",
                                    "",
                                    "INF001",
                                    "",
                                    "",
                                    Date(),
                                    Date(),
                                    Date(),
                                    true,
                                    true,
                                    true,
                                    0,
                                    "Conti",
                                )
                            },
                            "prog" to List(2) {
                                ExamDate(
                                    "prog",
                                    "cazzo ne so",
                                    "",
                                    "INF001",
                                    "",
                                    "",
                                    Date(),
                                    Date(),
                                    Date(),
                                    true,
                                    true,
                                    true,
                                    0,
                                    "Conti",
                                )
                                ExamDate(
                                    "prog",
                                    "cazzo ne so",
                                    "",
                                    "INF001",
                                    "",
                                    "",
                                    Date(),
                                    Date(),
                                    Date(),
                                    true,
                                    true,
                                    true,
                                    0,
                                    "Conti",
                                )
                            }
                        ) as HashMap
                    )
                )
            )
        }
    }
}

