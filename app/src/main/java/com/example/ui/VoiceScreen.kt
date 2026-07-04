package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.Translation
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val config by viewModel.configState.collectAsState()
    val isVoiceActive by viewModel.isVoiceConversationActive.collectAsState()
    val subtitle by viewModel.voiceSubtitle.collectAsState()
    val selectedVoiceGender by viewModel.selectedVoiceGender.collectAsState()

    val appLanguage = config.appLanguage

    // Infinite animation to show pulsating voice waves when active
    val infiniteTransition = rememberInfiniteTransition(label = "waves")
    val pulseScale1 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale1"
    )
    val pulseScale2 by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale2"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(CosmicDeepSpace, CosmicSlateDark)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            TopAppBar(
                title = {
                    Text(
                        text = "Google Omi Voice Experience",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.toggleVoiceConversation(false)
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Indicator Details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSlateDark.copy(alpha = 0.5f)),
                    border = BorderStroke(1.dp, GlassBorderDark)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Natural AI Companion",
                            color = CosmicAccentTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Speak naturally in ${config.appLanguage}. I will analyze your voice, transcribe the text, and respond audibly in a real-time natural frequency.",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Middle: Interactive Voice Wave Visualizers
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .testTag("voice_visualizer_box"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isVoiceActive) {
                        // Pulsing Wave Ring 1
                        Box(
                            modifier = Modifier
                                .scale(pulseScale1)
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(CosmicAccentTeal.copy(alpha = 0.12f))
                        )
                        // Pulsing Wave Ring 2
                        Box(
                            modifier = Modifier
                                .scale(pulseScale2)
                                .size(170.dp)
                                .clip(CircleShape)
                                .background(CosmicAccentEmerald.copy(alpha = 0.08f))
                        )
                    }

                    // Main central pulsing sphere
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(CosmicPurpleMain, CosmicAccentTeal)
                                )
                            )
                            .border(2.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            .clickable {
                                if (!isVoiceActive) {
                                    viewModel.toggleVoiceConversation(true)
                                }
                                viewModel.startVoiceListening()
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isVoiceActive) Icons.Default.Hearing else Icons.Default.Mic,
                            contentDescription = "Microphone",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Subtitle Display Transcripts
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = subtitle,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .padding(horizontal = 24.dp)
                            .testTag("voice_subtitle_text")
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Voice settings (Male vs Female vs Natural)
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(CosmicSlateDark)
                            .padding(4.dp)
                    ) {
                        listOf("Female", "Male", "Natural").forEach { gender ->
                            val isSelected = selectedVoiceGender == gender
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) CosmicAccentTeal else Color.Transparent)
                                    .clickable { viewModel.setVoiceGender(gender) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = when (gender) {
                                        "Male" -> Translation.translate("male", appLanguage)
                                        "Female" -> Translation.translate("female", appLanguage)
                                        else -> Translation.translate("natural_voice", appLanguage)
                                    },
                                    color = if (isSelected) CosmicDeepSpace else Color.Gray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Stop button
                TextButton(
                    onClick = {
                        viewModel.toggleVoiceConversation(false)
                        onNavigateBack()
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PowerSettingsNew, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "End Voice Session", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
