package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.MainViewModel
import com.example.data.*
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: MainViewModel,
    onNavigateToAdmin: () -> Unit,
    onNavigateToChat: (String) -> Unit // triggers navigation to selected chat session
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentUser.collectAsState()
    val config by viewModel.configState.collectAsState()

    val chatSessions by viewModel.chatSessions.collectAsState()
    val imageHistory by viewModel.imageHistory.collectAsState()
    val videoHistory by viewModel.videoHistory.collectAsState()

    val appLanguage = config.appLanguage

    var activeHistoryTab by remember { mutableStateOf("Chats") } // "Chats", "Images", "Videos"
    var showLanguageDropdown by remember { mutableStateOf(false) }

    val languagesList = listOf(
        "English", "বাংলা", "Hindi", "Arabic", "Urdu", "Chinese", 
        "Japanese", "Korean", "French", "Spanish", "German", 
        "Portuguese", "Turkish", "Russian"
    )

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
            // Header
            TopAppBar(
                title = {
                    Text(
                        text = Translation.translate("profile", appLanguage),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicSlateDark)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // User info card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorderDark, RoundedCornerShape(24.dp)),
                    color = CosmicSlateDark,
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // User avatar
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(CosmicSlateMedium)
                                .border(2.dp, CosmicAccentEmerald, CircleShape)
                        ) {
                            if (currentUser?.photoUrl?.isNotEmpty() == true) {
                                AsyncImage(
                                    model = currentUser!!.photoUrl,
                                    contentDescription = "User Avatar",
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

                        Spacer(modifier = Modifier.height(14.dp))

                        // User names & email
                        Text(
                            text = currentUser?.name ?: "User Name",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentUser?.email ?: "user@gmail.com",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Premium simulation upgrade panel
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (config.isPremium) CosmicAccentTeal.copy(alpha = 0.15f)
                                    else CosmicPurpleMain.copy(alpha = 0.15f)
                                )
                                .border(
                                    1.dp,
                                    if (config.isPremium) CosmicAccentTeal else CosmicPurpleMain,
                                    RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    // Toggle Premium Status dynamically
                                    viewModel.adminConfig.updateConfig { current ->
                                        current.copy(isPremium = !current.isPremium)
                                    }
                                    Toast.makeText(
                                        context,
                                        if (!config.isPremium) "Upgraded successfully to PRO plan!" else "Downgraded to FREE plan",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = if (config.isPremium) CosmicAccentTeal else CosmicAccentEmerald,
                                        modifier = Modifier.size(22.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (config.isPremium) Translation.translate("premium_user", appLanguage) else Translation.translate("free_user", appLanguage),
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (config.isPremium) "Unlimited Ad-free creations active" else "Tap to Upgrade to Premium",
                                            color = Color.LightGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = if (config.isPremium) Icons.Default.CheckCircle else Icons.Default.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = if (config.isPremium) CosmicAccentTeal else CosmicPurpleMain
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Settings & Customizing section
                Text(
                    text = Translation.translate("settings", appLanguage),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorderDark, RoundedCornerShape(20.dp)),
                    color = CosmicSlateDark,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column {
                        // Language Settings Selector with interactive popup
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLanguageDropdown = true }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = CosmicAccentTeal, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = Translation.translate("language", appLanguage), color = Color.White, fontSize = 14.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = appLanguage, color = Color.Gray, fontSize = 13.sp)
                                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
                            }
                        }

                        // Language drop-down dialog overlay
                        if (showLanguageDropdown) {
                            AlertDialog(
                                onDismissRequest = { showLanguageDropdown = false },
                                title = { Text("App Language") },
                                text = {
                                    Column(
                                        modifier = Modifier.verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        languagesList.forEach { lang ->
                                            Text(
                                                text = lang,
                                                color = if (appLanguage == lang) CosmicAccentEmerald else Color.White,
                                                fontWeight = if (appLanguage == lang) FontWeight.Bold else FontWeight.Normal,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        viewModel.adminConfig.updateConfig { it.copy(appLanguage = lang) }
                                                        showLanguageDropdown = false
                                                    }
                                                    .padding(vertical = 12.dp, horizontal = 8.dp)
                                            )
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showLanguageDropdown = false }) {
                                        Text("Close", color = CosmicAccentTeal)
                                    }
                                },
                                containerColor = CosmicSlateDark
                            )
                        }

                        HorizontalDivider(color = GlassBorderDark, thickness = 1.dp)

                        // In-app Admin Panel Entry button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToAdmin() }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = CosmicAccentEmerald, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = Translation.translate("admin_panel", appLanguage), color = Color.White, fontSize = 14.sp)
                            }
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                        }

                        HorizontalDivider(color = GlassBorderDark, thickness = 1.dp)

                        // Logout button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.logout() }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ExitToApp, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(text = Translation.translate("logout", appLanguage), color = Color.Red, fontSize = 14.sp)
                            }
                            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Historical items display Section
                Text(
                    text = Translation.translate("history", appLanguage),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                // History Tab Selectors
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CosmicSlateDark)
                        .padding(4.dp)
                ) {
                    listOf("Chats", "Images", "Videos").forEach { tab ->
                        val isSelected = activeHistoryTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CosmicPurpleMain else Color.Transparent)
                                .clickable { activeHistoryTab = tab }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (tab) {
                                    "Chats" -> Translation.translate("history_chat", appLanguage)
                                    "Images" -> Translation.translate("history_images", appLanguage)
                                    else -> Translation.translate("history_videos", appLanguage)
                                },
                                color = if (isSelected) Color.White else Color.Gray,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Load designated lists based on tab
                when (activeHistoryTab) {
                    "Chats" -> {
                        if (chatSessions.isEmpty()) {
                            HistoryEmptyState("No chat sessions yet.")
                        } else {
                            chatSessions.forEach { session ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(CosmicSlateDark)
                                        .clickable { onNavigateToChat(session.id) }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = null, tint = CosmicAccentTeal, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(text = session.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    }
                                    IconButton(
                                        onClick = { viewModel.deleteSession(session.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                    
                    "Images" -> {
                        if (imageHistory.isEmpty()) {
                            HistoryEmptyState("No generated art yet.")
                        } else {
                            // Display grid layout of images
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                imageHistory.chunked(2).forEach { rowItems ->
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                        rowItems.forEach { img ->
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .aspectRatio(1f)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(CosmicSlateDark)
                                            ) {
                                                AsyncImage(
                                                    model = img.imageUrl,
                                                    contentDescription = null,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                                
                                                // Style Tag
                                                Box(
                                                    modifier = Modifier
                                                        .align(Alignment.TopStart)
                                                        .padding(6.dp)
                                                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                ) {
                                                    Text(text = img.style, color = CosmicAccentTeal, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }

                                                IconButton(
                                                    onClick = { viewModel.deleteImage(img.id) },
                                                    modifier = Modifier
                                                        .align(Alignment.BottomEnd)
                                                        .padding(4.dp)
                                                        .size(24.dp)
                                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(12.dp))
                                                }
                                            }
                                        }
                                        if (rowItems.size < 2) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    "Videos" -> {
                        if (videoHistory.isEmpty()) {
                            HistoryEmptyState("No generated video clips yet.")
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                videoHistory.forEach { vid ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(14.dp))
                                            .background(CosmicSlateDark)
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(70.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        ) {
                                            AsyncImage(
                                                model = vid.videoUrl,
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.align(Alignment.Center)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = vid.prompt, color = Color.White, fontSize = 12.sp, maxLines = 1, fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(text = "${vid.duration}s • ${vid.aspectRatio} • ${vid.mode}", color = Color.Gray, fontSize = 10.sp)
                                        }
                                        IconButton(onClick = { viewModel.deleteVideo(vid.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun HistoryEmptyState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray.copy(alpha = 0.4f), modifier = Modifier.size(36.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, color = Color.Gray, fontSize = 12.sp, textAlign = TextAlign.Center)
    }
}
