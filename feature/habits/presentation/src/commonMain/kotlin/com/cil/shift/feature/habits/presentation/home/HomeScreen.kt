package com.cil.shift.feature.habits.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.koinInject
import com.cil.shift.feature.habits.presentation.home.components.HabitCard

@Composable
fun HomeScreen(
    onNavigateToCreateHabit: () -> Unit,
    onNavigateToHabitDetail: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val accentColor = Color(0xFF4E7CFF)

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateHabit,
                containerColor = accentColor,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create new habit",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = accentColor
                    )
                }
                state.error != null -> {
                    Text(
                        text = state.error ?: "Unknown error",
                        color = Color.Red,
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp)
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 24.dp,
                            end = 24.dp,
                            top = 16.dp,
                            bottom = 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Hello, ${state.userName}",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textColor
                                )
                                Text(
                                    text = state.currentDate,
                                    fontSize = 14.sp,
                                    color = textColor.copy(alpha = 0.6f)
                                )
                            }
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Today's Habits",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = textColor
                            )
                        }

                        if (state.habits.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 48.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No habits yet. Tap + to create one!",
                                        fontSize = 16.sp,
                                        color = textColor.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        } else {
                            items(
                                items = state.habits,
                                key = { it.habit.id }
                            ) { habitWithCompletion ->
                                HabitCard(
                                    habitWithCompletion = habitWithCompletion,
                                    onToggle = {
                                        viewModel.onEvent(
                                            HomeEvent.ToggleHabit(habitWithCompletion.habit.id)
                                        )
                                    },
                                    onClick = {
                                        onNavigateToHabitDetail(habitWithCompletion.habit.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
