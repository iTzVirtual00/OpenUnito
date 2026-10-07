package me.itzvirtual.myunitox.ui.feature_examdates


import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.itzvirtual.myunitox.domain.InvokeDate
import me.itzvirtual.myunitox.ui.components.StdTextButton
import me.itzvirtual.myunitox.ui.feature_profile.ProfileScreenContent
import me.itzvirtual.myunitox.ui.feature_profile.ProfileViewModel
import me.itzvirtual.myunitox.ui.themes.MyUnitoXTheme
import java.util.Date

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
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.screenState.collectAsStateWithLifecycle()
    var showStatistics by remember { mutableStateOf(false) }

    ProfileScreenContent(state, modifier)
}

@Composable
fun InvokeScreenContent(
    modifier: Modifier = Modifier,
    dates: List<InvokeDate>,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    Column() {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp),
        ) {
            StdTextButton(
                modifier = Modifier
                    .weight(1f),
                onClick = {selectedTab = 0},
                text = "Bookable", //TODO: translation
                shape = RectangleShape,
                tonalElevation = when (selectedTab) {
                    0 -> 4f.dp
                    else -> 1f.dp
                }
            )
            StdTextButton(
                modifier = Modifier
                    .weight(1f),
                onClick = {selectedTab = 1},
                text = "Booked", //TODO: translation
                shape = RectangleShape,
                tonalElevation = when (selectedTab) {
                    0 -> 1f.dp
                    else -> 4f.dp
                }
            )
        }
        Surface(
            tonalElevation = 2f.dp
        ) {
            when (selectedTab) {
                0 -> {
                    LazyColumn(
                        modifier = modifier
                    ) {
                        items(dates) { date ->
                            ExamCard(
                                title = date.title,
                                courseId = date.adCode,
                                bookingEnabled = date.bookingEnabled,
                                bookingFirstDate = date.bookingStartDate,
                                bookingLastDate = date.bookingEndDate
                            )
                        }
                    }
                }
                1 -> {
                    val date = dates.get(0)
                    ExamCard(
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


@Composable
fun ExamCard(
    title: String,
    courseId: String,
    bookingEnabled: Boolean,
    bookingFirstDate: Date,
    bookingLastDate: Date
) {

    Surface() {
        Text(
            text = "suca"
        )
    }

}

@Composable
fun ExamEntry(

) {

}


@Composable
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
fun ProfileScreenPreview() {
    MyUnitoXTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            InvokeScreenContent(
                modifier = Modifier.padding(innerPadding),
                dates = List<InvokeDate>(5) {
                    InvokeDate(
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
                    InvokeDate(
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
                    InvokeDate(
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
                    InvokeDate(
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
        }
    }
}
