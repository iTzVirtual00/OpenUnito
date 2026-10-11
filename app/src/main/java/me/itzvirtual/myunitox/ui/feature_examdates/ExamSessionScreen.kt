package me.itzvirtual.myunitox.ui.feature_examdates


import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.itzvirtual.myunitox.domain.examdate.ExamDate
import me.itzvirtual.myunitox.ui.components.ListWrapper
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
                    ListWrapper(
                        list = dates,
                        debug = debug,
                        isLoading = uiState.isLoading,
                        stateError = uiState.error
                    ) {
                        LazyColumn(
                            modifier = modifier
                        ) {
                            items(dates) { date ->
                                ExamBookingCard(
                                    title = date.title,
                                    courseId = date.adCode,
                                    bookingEnabled = date.bookingEnabled,
                                    bookingFirstDate = date.bookingStartDate,
                                    bookingLastDate = date.bookingEndDate
                                )
                            }
                        }
                    }
                }

                1 -> {
                    ListWrapper(
                        list = dates,
                        debug = debug,
                        isLoading = uiState.isLoading,
                        stateError = uiState.error
                    ) {
                        val date = dates[0]
                        ExamBookingCard(
                            title = date.title,
                            courseId = date.adCode,
                            bookingEnabled = date.bookingEnabled,
                            bookingFirstDate = date.bookingStartDate,
                            bookingLastDate = date.bookingEndDate
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ExamBookingCard(
    title: String,
    courseId: String,
    bookingEnabled: Boolean,
    bookingFirstDate: Date,
    bookingLastDate: Date
) {

    Surface() {
        Text(
            text = title
        )
    }

}

@Composable
fun ExamSessionEntry(

) {

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
                        dates = List(5) {
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
                        }
                    )
                )
            )
        }
    }
}
