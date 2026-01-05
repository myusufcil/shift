package com.cil.shift.feature.onboarding.presentation.walkthrough

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun WalkthroughScreen(
    onComplete: (WalkthroughState) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: WalkthroughViewModel = viewModel { WalkthroughViewModel() }
) {
    val state by viewModel.state.collectAsState()
    val pagerState = rememberPagerState(pageCount = { state.totalPages })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            viewModel.onEvent(WalkthroughEvent.PageChanged(page))
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Skip button (only show on intro pages 0-1)
            if (state.currentPage < 2) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    androidx.compose.material3.TextButton(
                        onClick = { onComplete(state) }
                    ) {
                        Text(
                            text = "Skip",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = textColor.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(60.dp))
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = true
            ) { page ->
                when (page) {
                    0, 1 -> IntroPage(
                        page = walkthroughPages[page],
                        modifier = Modifier.fillMaxSize()
                    )
                    2 -> AgeSelectionPage(
                        selectedAge = state.selectedAge,
                        onAgeSelected = { viewModel.onEvent(WalkthroughEvent.AgeSelected(it)) },
                        modifier = Modifier.fillMaxSize()
                    )
                    3 -> FocusAreasPage(
                        selectedAreas = state.selectedFocusAreas,
                        onAreaToggled = { viewModel.onEvent(WalkthroughEvent.FocusAreaToggled(it)) },
                        modifier = Modifier.fillMaxSize()
                    )
                    4 -> UserProfilePage(
                        userName = state.userName,
                        onUserNameChanged = { viewModel.onEvent(WalkthroughEvent.UserNameChanged(it)) },
                        selectedRhythm = state.dailyRhythm,
                        onRhythmSelected = { viewModel.onEvent(WalkthroughEvent.DailyRhythmSelected(it)) },
                        modifier = Modifier.fillMaxSize()
                    )
                    5 -> GoalsPage(
                        selectedWeeklyGoal = state.weeklyGoal,
                        onWeeklyGoalSelected = { viewModel.onEvent(WalkthroughEvent.WeeklyGoalSelected(it)) },
                        selectedHabitCount = state.startingHabitCount,
                        onHabitCountSelected = { viewModel.onEvent(WalkthroughEvent.StartingHabitCountSelected(it)) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            PageIndicator(
                pageCount = state.totalPages,
                currentPage = state.currentPage,
                modifier = Modifier.padding(vertical = 24.dp)
            )

            Button(
                onClick = {
                    if (state.isLastPage) {
                        onComplete(state)
                    } else {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(state.currentPage + 1)
                        }
                    }
                },
                enabled = state.isNextEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 48.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00D9FF),
                    disabledContainerColor = Color(0xFF00D9FF).copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(28.dp)
            ) {
                Text(
                    text = if (state.isLastPage) "Get Started" else "Next",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (state.isNextEnabled) Color.Black else Color.Black.copy(alpha = 0.5f)
                )
            }
        }
    }
}

@Composable
private fun IntroPage(
    page: WalkthroughPage,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface

    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(280.dp)
                .clip(CircleShape)
                .background(cardColor.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF00D9FF).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = page.emoji,
                    fontSize = 120.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(56.dp))

        Text(
            text = page.title,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center,
            lineHeight = 44.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = page.description,
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            color = textColor.copy(alpha = 0.75f),
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )
    }
}

@Composable
private fun AgeSelectionPage(
    selectedAge: AgeRange?,
    onAgeSelected: (AgeRange) -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎂",
            fontSize = 80.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Let's get to\nknow you",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "What's your age range?",
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            color = textColor.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            AgeRange.entries.forEach { age ->
                SelectableCard(
                    text = age.label,
                    isSelected = selectedAge == age,
                    onClick = { onAgeSelected(age) }
                )
            }
        }
    }
}

@Composable
private fun FocusAreasPage(
    selectedAreas: Set<FocusArea>,
    onAreaToggled: (FocusArea) -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎯",
            fontSize = 80.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "What do you want\nto focus on?",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Select one or more areas",
            fontSize = 17.sp,
            fontWeight = FontWeight.Normal,
            color = textColor.copy(alpha = 0.7f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(40.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            FocusArea.entries.forEach { area ->
                SelectableCard(
                    text = "${area.emoji}  ${area.label}",
                    isSelected = area in selectedAreas,
                    onClick = { onAreaToggled(area) }
                )
            }
        }
    }
}

@Composable
private fun UserProfilePage(
    userName: String,
    onUserNameChanged: (String) -> Unit,
    selectedRhythm: DailyRhythm?,
    onRhythmSelected: (DailyRhythm) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    val textColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = modifier
            .padding(horizontal = 32.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "👋",
            fontSize = 80.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "What should we\ncall you?",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = userName,
            onValueChange = onUserNameChanged,
            placeholder = {
                Text(
                    text = "Enter your name",
                    color = textColor.copy(alpha = 0.4f)
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00D9FF),
                unfocusedBorderColor = textColor.copy(alpha = 0.3f),
                cursorColor = Color(0xFF00D9FF),
                focusedTextColor = textColor,
                unfocusedTextColor = textColor
            ),
            shape = RoundedCornerShape(16.dp),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Are you a morning person\nor night owl?",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            textAlign = TextAlign.Center,
            lineHeight = 28.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            DailyRhythm.entries.forEach { rhythm ->
                SelectableCardWithDescription(
                    emoji = rhythm.emoji,
                    title = rhythm.label,
                    description = rhythm.description,
                    isSelected = selectedRhythm == rhythm,
                    onClick = { onRhythmSelected(rhythm) }
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun GoalsPage(
    selectedWeeklyGoal: WeeklyGoal?,
    onWeeklyGoalSelected: (WeeklyGoal) -> Unit,
    selectedHabitCount: StartingHabitCount?,
    onHabitCountSelected: (StartingHabitCount) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val textColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = modifier
            .padding(horizontal = 32.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "📈",
            fontSize = 80.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Set your\ngoals",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            textAlign = TextAlign.Center,
            lineHeight = 40.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Weekly Goal Section
        Text(
            text = "How often do you want to\npractice your habits?",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            WeeklyGoal.entries.forEach { goal ->
                SelectableChip(
                    title = goal.label,
                    subtitle = goal.days,
                    isSelected = selectedWeeklyGoal == goal,
                    onClick = { onWeeklyGoalSelected(goal) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Starting Habit Count Section
        Text(
            text = "How many habits do you\nwant to start with?",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            StartingHabitCount.entries.forEach { count ->
                SelectableCardWithDescription(
                    emoji = when (count) {
                        StartingHabitCount.FEW -> "🌱"
                        StartingHabitCount.MODERATE -> "🌿"
                        StartingHabitCount.MANY -> "🌳"
                    },
                    title = count.label,
                    description = count.description,
                    isSelected = selectedHabitCount == count,
                    onClick = { onHabitCountSelected(count) }
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun SelectableCard(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF00D9FF).copy(alpha = 0.15f) else cardColor,
        label = "backgroundColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF00D9FF) else Color.Transparent,
        label = "borderColor"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (isSelected) Color(0xFF00D9FF) else textColor
        )
    }
}

@Composable
private fun SelectableCardWithDescription(
    emoji: String,
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF00D9FF).copy(alpha = 0.15f) else cardColor,
        label = "backgroundColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF00D9FF) else Color.Transparent,
        label = "borderColor"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = emoji,
            fontSize = 32.sp
        )

        Column {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSelected) Color(0xFF00D9FF) else textColor
            )
            Text(
                text = description,
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
private fun SelectableChip(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = MaterialTheme.colorScheme.surface
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF00D9FF).copy(alpha = 0.15f) else cardColor,
        label = "backgroundColor"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF00D9FF) else Color.Transparent,
        label = "borderColor"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isSelected) Color(0xFF00D9FF) else textColor,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontSize = 11.sp,
            color = textColor.copy(alpha = 0.5f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PageIndicator(
    pageCount: Int,
    currentPage: Int,
    modifier: Modifier = Modifier
) {
    val textColor = MaterialTheme.colorScheme.onBackground

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(pageCount) { index ->
            Box(
                modifier = Modifier
                    .size(
                        width = if (index == currentPage) 24.dp else 8.dp,
                        height = 8.dp
                    )
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        if (index == currentPage) Color(0xFF00D9FF)
                        else textColor.copy(alpha = 0.3f)
                    )
            )
        }
    }
}
