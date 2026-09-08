package com.anuj.graphsonic.feature.history


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HistoryScreen(
    history: List<String>,
    onEquationSelected: (String) -> Unit
) {
    if (history.isEmpty()) {
        Box(
            modifier =
                Modifier.fillMaxSize(),
            contentAlignment =
                Alignment.Center
        ) {
            Text(
                text =
                    "No equation history yet"
            )
        }

        return
    }

    LazyColumn(
        modifier =
            Modifier.fillMaxSize(),
        verticalArrangement =
            Arrangement.spacedBy(
                0.dp
            )
    ) {
        items(
            items = history,
            key = { it }
        ) { expression ->

            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            onEquationSelected(
                                expression
                            )
                        }
                        .padding(
                            horizontal = 20.dp,
                            vertical = 18.dp
                        )
            ) {
                Text(
                    text = expression
                )
            }

            HorizontalDivider()
        }
    }
}