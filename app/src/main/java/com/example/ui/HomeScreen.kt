package com.example.ui

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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.MainViewModel
import com.example.data.Translation
import com.example.ui.theme.*
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onTabSelected: (Int) -> Unit,
    onOpenDrawer: () -> Unit,
    onNavigateToVoice: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val config by viewModel.configState.collectAsState()
    val isInternet by viewModel.isInternetConnected.collectAsState()

    val userName = currentUser?.name ?: "User"
    val appLanguage = config.appLanguage

    // Dynamically calculate greeting based on system hour
    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> Translation.translate("greeting_morning", appLanguage)
            in 12..16 -> Translation.translate("greeting_afternoon", appLanguage)
            in 17..19 -> Translation.translate("greeting_evening", appLanguage)
            else -> Translation.translate("greeting_night", appLanguage)
        }
    }
    
    val greetingIcon = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 5..11 -> "🌞"
            in 12..16 -> "☀️"
            in 17..19 -> "🌇"
            else -> "🌙"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CosmicDeepSpace)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Elegant top bar with hamburger menu for drawer, custom avatar and app title
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = CosmicAccentEmerald,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = config.appName,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open Drawer",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // Profile Photo
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(CosmicSlateMedium)
                            .border(1.5.dp, CosmicAccentTeal, CircleShape)
                            .clickable { onTabSelected(4) } // Profile tab is 4
                    ) {
                        if (currentUser?.photoUrl?.isNotEmpty() == true) {
                            AsyncImage(
                                model = currentUser!!.photoUrl,
                                contentDescription = "Profile Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.LightGray,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            // Main scrollable content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                // Time-based Greetings banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_greeting_card"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = CosmicSlateDark),
                    border = BorderStroke(1.dp, GlassBorderDark)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "$greetingIcon $greeting,",
                                color = CosmicAccentTeal,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = userName,
                                color = Color.White,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        // Status Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (config.isPremium) CosmicAccentTeal.copy(alpha = 0.2f)
                                    else CosmicAccentEmerald.copy(alpha = 0.15f)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (config.isPremium) "PRO" else "FREE",
                                color = if (config.isPremium) CosmicAccentTeal else CosmicAccentEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Premium Banner: Elek Featured Engine
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clickable { onTabSelected(1) }, // Chat
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Background gradient
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(CosmicPurpleMain, CosmicSlateDark)
                                    )
                                )
                        )
                        
                        // Glowing effect
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(120.dp)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(CosmicAccentTeal.copy(alpha = 0.3f), Color.Transparent)
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Gemini AI Engine Active",
                                color = CosmicAccentTeal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Experience ultimate chat & supercharged voice assistant",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.fillMaxWidth(0.85f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = Translation.translate("ai_voice", appLanguage),
                                    color = CosmicAccentEmerald,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = CosmicAccentEmerald,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Section title
                Text(
                    text = "AI Capabilities",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Feature Grid Layout (2x2 Grid)
                Row(modifier = Modifier.fillMaxWidth()) {
                    FeatureCard(
                        title = Translation.translate("ai_chat", appLanguage),
                        desc = "ChatGPT-like super conversational intelligence",
                        icon = Icons.Default.Chat,
                        color = CosmicAccentEmerald,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabSelected(1) } // Chat
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    FeatureCard(
                        title = Translation.translate("ai_image", appLanguage),
                        desc = "Text to high quality custom styles art",
                        icon = Icons.Default.Image,
                        color = CosmicAccentTeal,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabSelected(2) } // Image
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    FeatureCard(
                        title = Translation.translate("ai_video", appLanguage),
                        desc = "Animate images or text to stunning video clips",
                        icon = Icons.Default.VideoCall,
                        color = CosmicPurpleMain,
                        modifier = Modifier.weight(1f),
                        onClick = { onTabSelected(3) } // Video
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    FeatureCard(
                        title = Translation.translate("realtime_voice", appLanguage),
                        desc = "Speak to Elek in English, বাংলা and more",
                        icon = Icons.Default.Mic,
                        color = Color(0xFFFF9100),
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToVoice
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Quick Tip widget
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CosmicSlateMedium)
                        .border(1.dp, GlassBorderDark, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = CosmicAccentTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Tip: Create art in AI Image, then click 'Make Video' directly beside it to animate!",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun FeatureCard(
    title: String,
    desc: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicSlateDark),
        border = BorderStroke(1.dp, GlassBorderDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = desc,
                    color = Color.Gray,
                    fontSize = 11.sp,
                    lineHeight = 14.sp
                )
            }
        }
    }
}
