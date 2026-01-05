package com.cil.shift.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.TabNavigator

@Composable
fun BottomNavigationBar(
    tabNavigator: TabNavigator,
    onAddClick: () -> Unit
) {
    val tabs = listOf(HomeTab, StatisticsTab, CalendarTab, ProfileTab)
    val navBarColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onBackground

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Navigation Bar
        NavigationBar(
            containerColor = navBarColor,
            contentColor = textColor,
            tonalElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            // First two tabs
            tabs.take(2).forEach { tab ->
                NavigationBarItem(
                    selected = tabNavigator.current.key == tab.key,
                    onClick = { tabNavigator.current = tab },
                    icon = {
                        tab.options.icon?.let { painter ->
                            Icon(
                                painter = painter,
                                contentDescription = tab.options.title
                            )
                        }
                    },
                    label = {
                        Text(
                            text = tab.options.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF4E7CFF),
                        selectedTextColor = Color(0xFF4E7CFF),
                        unselectedIconColor = textColor.copy(alpha = 0.5f),
                        unselectedTextColor = textColor.copy(alpha = 0.5f),
                        indicatorColor = Color(0xFF4E7CFF).copy(alpha = 0.15f)
                    )
                )
            }

            // Spacer for center FAB
            Spacer(modifier = Modifier.weight(1f))

            // Last two tabs
            tabs.drop(2).forEach { tab ->
                NavigationBarItem(
                    selected = tabNavigator.current.key == tab.key,
                    onClick = { tabNavigator.current = tab },
                    icon = {
                        tab.options.icon?.let { painter ->
                            Icon(
                                painter = painter,
                                contentDescription = tab.options.title
                            )
                        }
                    },
                    label = {
                        Text(
                            text = tab.options.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF4E7CFF),
                        selectedTextColor = Color(0xFF4E7CFF),
                        unselectedIconColor = textColor.copy(alpha = 0.5f),
                        unselectedTextColor = textColor.copy(alpha = 0.5f),
                        indicatorColor = Color(0xFF4E7CFF).copy(alpha = 0.15f)
                    )
                )
            }
        }

        // Center FAB - Positioned above the navigation bar
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (-30).dp)
        ) {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = Color(0xFF4E7CFF),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp,
                    hoveredElevation = 10.dp
                ),
                shape = CircleShape,
                modifier = Modifier
                    .size(64.dp)
                    .shadow(12.dp, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add habit",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
