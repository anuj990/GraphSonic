package com.anuj.graphsonic.feature.equation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EquationScreen(
    onGraph: (String) -> Boolean,
    modifier: Modifier = Modifier
) {
    var equation by remember {
        mutableStateOf("")
    }

    var errorMessage by remember {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement =
            Arrangement.Center
    ) {

        Text(
            text = "GraphSonic"
        )

        OutlinedTextField(
            value = equation,
            onValueChange = {
                equation = it
                errorMessage = null
            },
            modifier = Modifier
                .fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(
                    text = "Enter equation"
                )
            },
            isError =
                errorMessage != null
        )

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                modifier = Modifier
                    .padding(
                        top = 8.dp
                    )
            )
        }

        Button(
            onClick = {

                val expression =
                    equation.trim()

                if (expression.isEmpty()) {
                    errorMessage =
                        "Enter an equation"
                    return@Button
                }

                val success =
                    onGraph(expression)

                if (!success) {
                    errorMessage =
                        "Equation is invalid"
                }

            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 16.dp
                )
        ) {
            Text(
                text = "Graph"
            )
        }
    }
}