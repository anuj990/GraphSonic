package com.anuj.graphsonic.feature.equation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EquationScreen(
    onGraph: (List<String>) -> Boolean,
    modifier: Modifier = Modifier
) {
    var equations by rememberSaveable {
        mutableStateOf(listOf(""))
    }

    var errorMessage by rememberSaveable {
        mutableStateOf<String?>(null)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "GraphSonic"
        )

        Text(
            text = "Enter equations to graph together",
            modifier = Modifier.padding(
                top = 8.dp,
                bottom = 16.dp
            )
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemsIndexed(
                items = equations,
                key = { index, _ -> index }
            ) { index, equation ->

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement =
                        Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = equation,
                        onValueChange = { value ->
                            equations =
                                equations.toMutableList().apply {
                                    this[index] = value
                                }
                            errorMessage = null
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = {
                            Text(
                                text = "Enter equation"
                            )
                        },
                        isError =
                            errorMessage != null &&
                                    equation.isNotBlank()
                    )

                    if (equations.size > 1) {
                        TextButton(
                            onClick = {
                                equations =
                                    equations.toMutableList().apply {
                                        removeAt(index)
                                    }

                                errorMessage = null
                            }
                        ) {
                            Text("×")
                        }
                    }
                }
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                modifier = Modifier.padding(
                    top = 8.dp
                )
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (equations.size >= 16) {
                        errorMessage =
                            "Maximum 16 equations"
                    } else {
                        equations =
                            equations + ""
                        errorMessage = null
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Add equation")
            }

            Button(
                onClick = {
                    val cleaned =
                        equations
                            .map { it.trim() }
                            .filter { it.isNotEmpty() }

                    if (cleaned.isEmpty()) {
                        errorMessage =
                            "Enter at least one equation"
                        return@Button
                    }

                    val success =
                        onGraph(cleaned)

                    if (!success) {
                        errorMessage =
                            "One or more equations are invalid"
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Graph")
            }
        }
    }
}