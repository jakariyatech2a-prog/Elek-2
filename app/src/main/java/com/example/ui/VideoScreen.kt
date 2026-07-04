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
import com.example.data.Translation
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoScreen(
    viewModel: MainViewModel,
    passedImageUrl: String?, // Pre-filled image if coming from Image screen
    passedPrompt: String?, // Pre-filled prompt if coming from Image screen
    onClearPassedParams: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isGenerating by viewModel.isGeneratingVideo.collectAsState()
    val generationProgress by viewModel.videoGenerationProgress.collectAsState()
    
    val isWatchingAd by viewModel.isWatchingAd.collectAsState()
    val adCountdown by viewModel.adCountdown.collectAsState()
    
    val latestVideo by viewModel.latestGeneratedVideo.collectAsState()
    val config by viewModel.configState.collectAsState()

    val appLanguage = config.appLanguage

    var modeState by remember { mutableStateOf("Text to Video") } // "Text to Video" or "Image to Video"
    var promptInput by remember { mutableStateOf("") }
    var selectedDuration by remember { mutableStateOf(6) } // 6, 8, 10
    var selectedRatio by remember { mutableStateOf("16:9") } // "16:9", "9:16"
    var isRecordingPrompt by remember { mutableStateOf(false) }
    
    var selectedDownloadQuality by remember { mutableStateOf("720p") } // 720p, 1080p, 2K, 4K
    var showDownloadDialog by remember { mutableStateOf(false) }

    // Synchronize inputs if arguments were passed from the Image screen
    LaunchedEffect(passedImageUrl, passedPrompt) {
        if (passedImageUrl != null) {
            modeState = "Image to Video"
            promptInput = passedPrompt ?: "Animate this visual artwork with cinematic smoke"
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
            // Header
            TopAppBar(
                title = {
                    Text(
                        text = Translation.translate("ai_video", appLanguage),
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CosmicSlateDark
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Mode Switcher (Image-to-Video vs Text-to-Video)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CosmicSlateDark)
                        .padding(4.dp)
                ) {
                    listOf("Text to Video", "Image to Video").forEach { mode ->
                        val isSelected = modeState == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CosmicAccentEmerald else Color.Transparent)
                                .clickable { modeState = mode }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode,
                                color = if (isSelected) CosmicDeepSpace else Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Image to Video input thumbnail if active
                if (modeState == "Image to Video") {
                    Text(
                        text = "Source Artwork To Animate",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (passedImageUrl != null) {
                        // Thumbnail
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, CosmicAccentTeal, RoundedCornerShape(16.dp))
                        ) {
                            AsyncImage(
                                model = passedImageUrl,
                                contentDescription = "Active image to animate",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            IconButton(
                                onClick = { onClearPassedParams() },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                    .size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear Image", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    } else {
                        // Empty choice placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(CosmicSlateDark)
                                .border(1.dp, GlassBorderDark, RoundedCornerShape(16.dp))
                                .clickable {
                                    Toast.makeText(context, "Tip: Generate an image in 'AI Image' first, then click 'Make Video'!", Toast.LENGTH_LONG).show()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "No image selected. Tap for directions",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }

                // Prompt Input Box with Voice dictation
                Text(
                    text = "Video Motion Prompt",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = { 
                        Text(
                            Translation.translate("enter_prompt", appLanguage), 
                            color = Color.Gray,
                            fontSize = 14.sp
                        ) 
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = CosmicSlateDark,
                        unfocusedContainerColor = CosmicSlateDark,
                        focusedBorderColor = CosmicAccentTeal,
                        unfocusedBorderColor = GlassBorderDark
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("video_prompt_input"),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                isRecordingPrompt = true
                                coroutineScope.launch {
                                    delay(2000)
                                    promptInput = "Cinematic slow motion water splash with reflective neon particles"
                                    isRecordingPrompt = false
                                    Toast.makeText(context, "Voice prompt recorded!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Dictate prompt",
                                tint = if (isRecordingPrompt) CosmicAccentEmerald else CosmicAccentTeal,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                )

                if (isRecordingPrompt) {
                    Text(
                        text = "🎤 " + Translation.translate("mic_listening", appLanguage),
                        color = CosmicAccentEmerald,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Select Duration Grid
                Text(
                    text = Translation.translate("select_duration", appLanguage),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf(6, 8, 10).forEach { dur ->
                        val isSelected = selectedDuration == dur
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) CosmicPurpleMain else CosmicSlateDark)
                                .border(1.dp, if (isSelected) Color.Transparent else GlassBorderDark, RoundedCornerShape(14.dp))
                                .clickable { selectedDuration = dur }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$dur Seconds",
                                color = if (isSelected) Color.White else Color.LightGray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Select Ratio Grid
                Text(
                    text = Translation.translate("select_ratio", appLanguage),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf("16:9", "9:16").forEach { ratio ->
                        val isSelected = selectedRatio == ratio
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) CosmicAccentTeal else CosmicSlateDark)
                                .border(1.dp, if (isSelected) Color.Transparent else GlassBorderDark, RoundedCornerShape(14.dp))
                                .clickable { selectedRatio = ratio }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (ratio == "16:9") Icons.Default.CropLandscape else Icons.Default.CropPortrait,
                                    contentDescription = null,
                                    tint = if (isSelected) CosmicDeepSpace else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = ratio,
                                    color = if (isSelected) CosmicDeepSpace else Color.LightGray,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Generate Button
                Button(
                    onClick = {
                        if (promptInput.isBlank()) {
                            Toast.makeText(context, "Please write a motion description first!", Toast.LENGTH_SHORT).show()
                        } else if (modeState == "Image to Video" && passedImageUrl == null) {
                            Toast.makeText(context, "Please select an artwork to animate first!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.generateVideo(
                                prompt = promptInput,
                                duration = selectedDuration,
                                aspectRatio = selectedRatio,
                                mode = modeState,
                                sourceImageUrl = passedImageUrl
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("generate_video_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CosmicAccentEmerald,
                        contentColor = CosmicDeepSpace
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isGenerating && !isWatchingAd
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = Translation.translate("generate", appLanguage),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Ad Countdown Overlay Screen
                AnimatedVisibility(visible = isWatchingAd) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, CosmicAccentTeal, RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(containerColor = CosmicSlateDark),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = CosmicAccentTeal)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "📽️ Watching Advertisement...",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Free Users must watch this ad. Generating will start automatically in $adCountdown seconds.",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Tip: Upgrade to Premium inside the Profile tab to disable all ads!",
                                color = CosmicAccentEmerald,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Video Progress Screen OR Result Display
                AnimatedVisibility(visible = isGenerating || latestVideo != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GlassBorderDark, RoundedCornerShape(24.dp)),
                        colors = CardDefaults.cardColors(containerColor = CosmicSlateDark),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (isGenerating) {
                                // Progress Meter
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "Rendering Frames: ${(generationProgress * 100).toInt()}%",
                                        color = CosmicAccentTeal,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    LinearProgressIndicator(
                                        progress = { generationProgress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(CircleShape),
                                        color = CosmicAccentEmerald,
                                        trackColor = CosmicSlateMedium,
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Synthesizing optical flows & stabilizing visual depth filters...",
                                        color = Color.Gray,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            } else if (latestVideo != null) {
                                // Success! Display Animated Result
                                val aspectNum = if (latestVideo!!.aspectRatio == "16:9") 16f / 9f else 9f / 16f
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(aspectNum)
                                        .clip(RoundedCornerShape(16.dp))
                                        .testTag("video_player_frame")
                                ) {
                                    // Visual Animation
                                    AsyncImage(
                                        model = latestVideo!!.videoUrl,
                                        contentDescription = "AI Generated Video",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Dynamic Animated Glowing play-indicator overlay
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.Center)
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.5f))
                                            .border(1.dp, Color.White, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Play Video", tint = Color.White, modifier = Modifier.size(24.dp))
                                    }

                                    // Custom Waterproof branding "Elek" placed in the bottom right corner
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(8.dp)
                                    ) {
                                        Text(
                                            text = config.watermarkText,
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Light,
                                            letterSpacing = 1.sp
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Information details
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Duration: ${latestVideo!!.duration}s",
                                        color = Color.LightGray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Ratio: ${latestVideo!!.aspectRatio}",
                                        color = Color.LightGray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = latestVideo!!.prompt,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Download Trigger Button
                                Button(
                                    onClick = { showDownloadDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CosmicPurpleMain,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = Translation.translate("download", appLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Resolution Quality Selector Dialog
        if (showDownloadDialog) {
            AlertDialog(
                onDismissRequest = { showDownloadDialog = false },
                title = { Text("Select Download Quality") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("720p", "1080p", "2K", "4K", "8K").forEach { quality ->
                            val isSelected = selectedDownloadQuality == quality
                            val requiresPremium = quality != "720p"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) CosmicSlateMedium else Color.Transparent)
                                    .clickable { selectedDownloadQuality = quality }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { selectedDownloadQuality = quality }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = quality, fontWeight = FontWeight.Bold, color = Color.White)
                                }

                                if (requiresPremium) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(CosmicAccentEmerald.copy(alpha = 0.2f))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "PRO / AD",
                                            color = CosmicAccentEmerald,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "FREE",
                                        color = Color.Gray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDownloadDialog = false
                            val isQualityFree = selectedDownloadQuality == "720p"
                            if (isQualityFree || config.isPremium) {
                                Toast.makeText(context, "Downloading video in $selectedDownloadQuality to gallery!", Toast.LENGTH_LONG).show()
                            } else {
                                // Simulate loading rewarded ad first
                                Toast.makeText(context, "Quality requires PRO. Simulating ad first...", Toast.LENGTH_SHORT).show()
                                coroutineScope.launch {
                                    Toast.makeText(context, "Saved $selectedDownloadQuality video to gallery successfully!", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    ) {
                        Text("Download", color = CosmicAccentTeal)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDownloadDialog = false }) {
                        Text("Cancel", color = Color.Gray)
                    }
                },
                containerColor = CosmicSlateDark
            )
        }
    }
}
