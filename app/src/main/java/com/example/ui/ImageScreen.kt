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
fun ImageScreen(
    viewModel: MainViewModel,
    onMakeVideoFromImage: (String, String) -> Unit // triggers conversion to Video Tab
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val isGenerating by viewModel.isGeneratingImage.collectAsState()
    val latestImage by viewModel.latestGeneratedImage.collectAsState()
    val config by viewModel.configState.collectAsState()

    val appLanguage = config.appLanguage

    var promptInput by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("Anime") }
    var selectedRatio by remember { mutableStateOf("1:1") }
    var isRecordingPrompt by remember { mutableStateOf(false) }
    var showDownloadDialog by remember { mutableStateOf(false) }
    var selectedDownloadQuality by remember { mutableStateOf("720p") }

    val stylesList = listOf(
        "Realistic", "Pixar", "Anime", "Cartoon", "Cinematic", 
        "Fantasy", "3D", "Logo", "Poster", "Thumbnail", "Sticker"
    )

    val ratiosList = listOf("1:1", "16:9", "9:16")

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
            // Elegant Header
            TopAppBar(
                title = {
                    Text(
                        text = Translation.translate("ai_image", appLanguage),
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
                // Prompt Text area with Microphone
                Text(
                    text = "What would you like to create?",
                    color = Color.White,
                    fontSize = 16.sp,
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
                        .height(110.dp)
                        .testTag("image_prompt_input"),
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                isRecordingPrompt = true
                                coroutineScope.launch {
                                    delay(2000)
                                    promptInput = "A futuristic cyberpunk metropolis glowing in neon green"
                                    isRecordingPrompt = false
                                    Toast.makeText(context, "Voice transcribed successfully!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice Prompt dictation",
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
                        modifier = Modifier.padding(top = 4.dp, start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Select Style Option
                Text(
                    text = Translation.translate("select_style", appLanguage),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                // Styles horizontal list
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    stylesList.forEach { style ->
                        val isSelected = selectedStyle == style
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) CosmicAccentTeal else CosmicSlateDark)
                                .border(1.dp, if (isSelected) Color.Transparent else GlassBorderDark, RoundedCornerShape(14.dp))
                                .clickable { selectedStyle = style }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = style,
                                color = if (isSelected) CosmicDeepSpace else Color.LightGray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Select Ratio option
                Text(
                    text = Translation.translate("select_ratio", appLanguage),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                // Ratio selection horizontal list
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ratiosList.forEach { ratio ->
                        val isSelected = selectedRatio == ratio
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) CosmicPurpleMain else CosmicSlateDark)
                                .border(1.dp, if (isSelected) Color.Transparent else GlassBorderDark, RoundedCornerShape(14.dp))
                                .clickable { selectedRatio = ratio }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = when (ratio) {
                                        "16:9" -> Icons.Default.CropLandscape
                                        "9:16" -> Icons.Default.CropPortrait
                                        else -> Icons.Default.CropSquare
                                    },
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else Color.Gray,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = ratio,
                                    color = if (isSelected) Color.White else Color.LightGray,
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
                        if (promptInput.isNotBlank()) {
                            viewModel.generateImage(promptInput, selectedStyle, selectedRatio)
                        } else {
                            Toast.makeText(context, "Please enter a description first!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("generate_image_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CosmicAccentEmerald,
                        contentColor = CosmicDeepSpace
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isGenerating
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(color = CosmicDeepSpace, modifier = Modifier.size(24.dp))
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Translation.translate("generate", appLanguage),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Display Area for Generated Image Result
                AnimatedVisibility(
                    visible = isGenerating || latestImage != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, GlassBorderDark, RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = CosmicSlateDark)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            if (isGenerating) {
                                // Dynamic Processing Loading Screen
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(220.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    CircularProgressIndicator(color = CosmicAccentTeal)
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "Rendering pixels with high-fidelity digital art filters...",
                                        color = Color.LightGray,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else if (latestImage != null) {
                                // Successfully generated image frame
                                val imageAspectRatio = when (latestImage!!.aspectRatio) {
                                    "16:9" -> 16f / 9f
                                    "9:16" -> 9f / 16f
                                    else -> 1f
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(imageAspectRatio)
                                        .clip(RoundedCornerShape(16.dp))
                                        .testTag("generated_image_frame")
                                ) {
                                    AsyncImage(
                                        model = latestImage!!.imageUrl,
                                        contentDescription = "AI Generated Art",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Dynamic thin Watermark "Elek" placed in the bottom right corner
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

                                // Image Prompts summary
                                Text(
                                    text = latestImage!!.prompt,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Action Buttons Row (Make Video & Download)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Make Video Button
                                    Button(
                                        onClick = {
                                            onMakeVideoFromImage(latestImage!!.imageUrl, latestImage!!.prompt)
                                        },
                                        modifier = Modifier.weight(1.5f),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CosmicPurpleMain,
                                            contentColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.VideoCall, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = Translation.translate("make_video", appLanguage),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Download Button
                                    IconButton(
                                        onClick = {
                                            showDownloadDialog = true
                                        },
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(CosmicSlateMedium)
                                            .size(48.dp)
                                    ) {
                                        Icon(Icons.Default.Download, contentDescription = "Download Art", tint = CosmicAccentTeal)
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }

        // Resolution Quality Selector Dialog for Image
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
                                Toast.makeText(context, "Saved $selectedDownloadQuality Image to gallery successfully!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Quality requires PRO. Simulating ad first...", Toast.LENGTH_SHORT).show()
                                coroutineScope.launch {
                                    delay(2000)
                                    Toast.makeText(context, "Saved $selectedDownloadQuality Image to gallery successfully!", Toast.LENGTH_LONG).show()
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
