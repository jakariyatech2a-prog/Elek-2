package com.example.ui

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.AdminConfig
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val config by viewModel.configState.collectAsState()

    // Temporary form states
    var appNameInput by remember { mutableStateOf(config.appName) }
    var watermarkInput by remember { mutableStateOf(config.watermarkText) }
    var watermarkSizeInput by remember { mutableStateOf(config.watermarkSize) }
    var watermarkAlphaInput by remember { mutableStateOf(config.watermarkAlpha) }
    var watermarkPositionInput by remember { mutableStateOf(config.watermarkPosition) }
    var aiModelInput by remember { mutableStateOf(config.aiModel) }
    var apiKeyInput by remember { mutableStateOf(config.apiKey) }
    var rewardTimeInput by remember { mutableStateOf(config.rewardTimeSec.toString()) }
    var downloadQualityInput by remember { mutableStateOf(config.downloadQuality) }
    
    var isPremiumToggle by remember { mutableStateOf(config.isPremium) }
    var isAdsEnabledToggle by remember { mutableStateOf(config.isAdsEnabled) }
    var isWelcomeEmailToggle by remember { mutableStateOf(config.isWelcomeEmailEnabled) }
    var isNotificationToggle by remember { mutableStateOf(config.isNotificationEnabled) }
    var isMaintenanceToggle by remember { mutableStateOf(config.isMaintenanceMode) }
    var analyticsToggle by remember { mutableStateOf(config.analyticsEnabled) }
    var feedbackToggle by remember { mutableStateOf(config.feedbackEnabled) }

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
            // Header with save button
            TopAppBar(
                title = {
                    Text(
                        text = "Admin Panel",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            // Persist changes to SharedPreferences
                            viewModel.adminConfig.updateConfig { current ->
                                current.copy(
                                    appName = appNameInput,
                                    watermarkText = watermarkInput,
                                    watermarkSize = watermarkSizeInput,
                                    watermarkAlpha = watermarkAlphaInput,
                                    watermarkPosition = watermarkPositionInput,
                                    aiModel = aiModelInput,
                                    apiKey = apiKeyInput,
                                    rewardTimeSec = rewardTimeInput.toIntOrNull() ?: 20,
                                    downloadQuality = downloadQualityInput,
                                    isPremium = isPremiumToggle,
                                    isAdsEnabled = isAdsEnabledToggle,
                                    isWelcomeEmailEnabled = isWelcomeEmailToggle,
                                    isNotificationEnabled = isNotificationToggle,
                                    isMaintenanceMode = isMaintenanceToggle,
                                    analyticsEnabled = analyticsToggle,
                                    feedbackEnabled = feedbackToggle
                                )
                            }
                            Toast.makeText(context, "Configurations saved successfully!", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save Configurations", tint = CosmicAccentEmerald)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CosmicSlateDark)
            )

            // Scrollable Forms
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Warning note
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CosmicPurpleMain.copy(alpha = 0.15f)),
                    border = BorderStroke(1.dp, CosmicPurpleMain)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = CosmicAccentTeal)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Admin changes apply globally instantly without requiring app updates.",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // 1. Branding Settings
                AdminSection(title = "App Branding Settings", icon = Icons.Default.Palette) {
                    // App Name
                    OutlinedTextField(
                        value = appNameInput,
                        onValueChange = { appNameInput = it },
                        label = { Text("App Name", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = CosmicAccentTeal, unfocusedBorderColor = Color.Gray
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_app_name")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Watermark Text
                    OutlinedTextField(
                        value = watermarkInput,
                        onValueChange = { watermarkInput = it },
                        label = { Text("Watermark Text", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = CosmicAccentTeal, unfocusedBorderColor = Color.Gray
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Watermark Size Slider
                    Text(text = "Watermark Text Size: ${watermarkSizeInput.toInt()} sp", color = Color.LightGray, fontSize = 12.sp)
                    Slider(
                        value = watermarkSizeInput,
                        onValueChange = { watermarkSizeInput = it },
                        valueRange = 8f..24f,
                        colors = SliderDefaults.colors(thumbColor = CosmicAccentEmerald, activeTrackColor = CosmicAccentEmerald)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Watermark Alpha Slider
                    Text(text = "Watermark Transparency: ${(watermarkAlphaInput * 100).toInt()}%", color = Color.LightGray, fontSize = 12.sp)
                    Slider(
                        value = watermarkAlphaInput,
                        onValueChange = { watermarkAlphaInput = it },
                        valueRange = 0.1f..1.0f,
                        colors = SliderDefaults.colors(thumbColor = CosmicAccentTeal, activeTrackColor = CosmicAccentTeal)
                    )
                }

                // 2. AI & Gemini API Config
                AdminSection(title = "AI Engine Configuration", icon = Icons.Default.Memory) {
                    // Selector for AI Model
                    Text(text = "Select Model alias", color = Color.LightGray, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("gemini-3.5-flash", "gemini-3.1-pro-preview").forEach { model ->
                            val isSelected = aiModelInput == model
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) CosmicAccentTeal else CosmicSlateDark)
                                    .clickable { aiModelInput = model }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = if (model.contains("flash")) "Flash (Basic)" else "Pro (Advanced)",
                                    color = if (isSelected) CosmicDeepSpace else Color.LightGray,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // API Key Field
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("Custom Gemini API Key Override", color = Color.Gray) },
                        placeholder = { Text("Leave empty to use default BuildConfig key", color = Color.DarkGray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = CosmicAccentTeal, unfocusedBorderColor = Color.Gray
                        ),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("admin_api_key")
                    )
                }

                // 3. Monetization & Advertisements
                AdminSection(title = "Ads & Premium Status", icon = Icons.Default.MonetizationOn) {
                    // Premium Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Premium Active Plan", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Simulates paid upgrade status", color = Color.Gray, fontSize = 11.sp)
                        }
                        Switch(
                            checked = isPremiumToggle,
                            onCheckedChange = { isPremiumToggle = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CosmicAccentTeal)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Ads System Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Enable Reward Advertisements", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Triggers 20s delay before generating videos", color = Color.Gray, fontSize = 11.sp)
                        }
                        Switch(
                            checked = isAdsEnabledToggle,
                            onCheckedChange = { isAdsEnabledToggle = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = CosmicAccentEmerald)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Countdown seconds
                    OutlinedTextField(
                        value = rewardTimeInput,
                        onValueChange = { rewardTimeInput = it },
                        label = { Text("Reward Ad Time Limit (Seconds)", color = Color.Gray) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                            focusedBorderColor = CosmicAccentTeal, unfocusedBorderColor = Color.Gray
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 4. App Operations settings
                AdminSection(title = "App Operations & Telemetry", icon = Icons.Default.Settings) {
                    // Welcome Email Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Automatic Gmail Welcome Email", color = Color.White, fontSize = 14.sp)
                        Switch(checked = isWelcomeEmailToggle, onCheckedChange = { isWelcomeEmailToggle = it })
                    }

                    HorizontalDivider(color = GlassBorderDark, modifier = Modifier.padding(vertical = 10.dp))

                    // Notifications Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Enable App Notifications", color = Color.White, fontSize = 14.sp)
                        Switch(checked = isNotificationToggle, onCheckedChange = { isNotificationToggle = it })
                    }

                    HorizontalDivider(color = GlassBorderDark, modifier = Modifier.padding(vertical = 10.dp))

                    // Maintenance Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Maintenance Mode Active", color = Color.White, fontSize = 14.sp)
                        Switch(checked = isMaintenanceToggle, onCheckedChange = { isMaintenanceToggle = it })
                    }

                    HorizontalDivider(color = GlassBorderDark, modifier = Modifier.padding(vertical = 10.dp))

                    // Analytics Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Analytics telemetry logger", color = Color.White, fontSize = 14.sp)
                        Switch(checked = analyticsToggle, onCheckedChange = { analyticsToggle = it })
                    }

                    HorizontalDivider(color = GlassBorderDark, modifier = Modifier.padding(vertical = 10.dp))

                    // Feedback Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Feedback reporting systems", color = Color.White, fontSize = 14.sp)
                        Switch(checked = feedbackToggle, onCheckedChange = { feedbackToggle = it })
                    }
                    
                    HorizontalDivider(color = GlassBorderDark, modifier = Modifier.padding(vertical = 10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Server Health Node", color = Color.Gray, fontSize = 13.sp)
                        Text(text = config.serverStatus, color = CosmicAccentEmerald, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun AdminSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CosmicSlateDark),
        border = BorderStroke(1.dp, GlassBorderDark)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(CosmicAccentTeal.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = CosmicAccentTeal, modifier = Modifier.size(16.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            content()
        }
    }
}
