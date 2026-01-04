package com.cil.shift.feature.settings.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsOfServiceScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Terms of Service",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A1628)
                )
            )
        },
        containerColor = Color(0xFF0A1628)
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TermsSection(
                    title = "Acceptance of Terms",
                    content = "By accessing and using Shift, you accept and agree to be bound by the terms and provisions of this agreement."
                )
            }

            item {
                TermsSection(
                    title = "Use License",
                    content = "Permission is granted to use Shift for personal, non-commercial purposes. You may not modify, copy, or distribute the app without prior written consent."
                )
            }

            item {
                TermsSection(
                    title = "User Responsibilities",
                    content = "You are responsible for maintaining the confidentiality of your account and for all activities that occur under your account."
                )
            }

            item {
                TermsSection(
                    title = "Service Modifications",
                    content = "We reserve the right to modify or discontinue the service at any time without notice. We shall not be liable to you or any third party for any modification or discontinuance."
                )
            }

            item {
                TermsSection(
                    title = "Limitation of Liability",
                    content = "In no event shall Shift be liable for any damages arising out of the use or inability to use the app, even if we have been notified of the possibility of such damages."
                )
            }

            item {
                TermsSection(
                    title = "Governing Law",
                    content = "These terms shall be governed by and construed in accordance with applicable laws, without regard to its conflict of law provisions."
                )
            }

            item {
                Text(
                    text = "Last updated: January 2026",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.4f),
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun TermsSection(
    title: String,
    content: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A2942))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = content,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.7f),
            lineHeight = 20.sp
        )
    }
}
