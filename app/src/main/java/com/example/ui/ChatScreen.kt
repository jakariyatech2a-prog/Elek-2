package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.MainViewModel
import com.example.data.ChatMessage
import com.example.data.Translation
import com.example.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: MainViewModel,
    onOpenDrawer: () -> Unit,
    onMakeVideoFromImage: (String, String) -> Unit // imageUrl, prompt -> triggers Video Generation flow
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val messages by viewModel.currentMessages.collectAsState()
    val isChatLoading by viewModel.isChatLoading.collectAsState()
    val config by viewModel.configState.collectAsState()
    val isTtsActive by viewModel.isTtsActive.collectAsState()

    var textInput by remember { mutableStateOf("") }
    var isListeningState by remember { mutableStateOf(false) }
    var showAttachmentMenu by remember { mutableStateOf(false) }

    val appLanguage = config.appLanguage

    // Auto scroll to bottom when messages list size changes
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Auto create session if none active
    LaunchedEffect(currentSessionId) {
        if (currentSessionId == null) {
            viewModel.createNewSession()
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
            // Elegant Header
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = Translation.translate("ai_chat", appLanguage),
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isChatLoading) Translation.translate("generating", appLanguage) else "Gemini Super Intelligence",
                            color = if (isChatLoading) CosmicAccentEmerald else Color.Gray,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    // New Chat option
                    IconButton(onClick = { viewModel.createNewSession() }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Chat",
                            tint = CosmicAccentEmerald
                        )
                    }
                    
                    // Voice playback enable / disable state indicator
                    IconButton(onClick = { viewModel.toggleTts(!isTtsActive) }) {
                        Icon(
                            imageVector = if (isTtsActive) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Toggle Speech",
                            tint = if (isTtsActive) CosmicAccentTeal else Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CosmicSlateDark
                )
            )

            // Message Board
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty() && !isChatLoading) {
                    // Empty Board: Welcome Prompts
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            tint = CosmicAccentTeal.copy(alpha = 0.3f),
                            modifier = Modifier.size(72.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Start a conversation with Elek",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "I support translations, direct image to video, natural speaking, and file attachments in 14+ languages.",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(messages) { message ->
                            ChatBubble(
                                message = message,
                                language = appLanguage,
                                onCopy = {
                                    clipboardManager.setText(AnnotatedString(message.content))
                                    Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                onShare = {
                                    val sendIntent: Intent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, message.content)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, null)
                                    context.startActivity(shareIntent)
                                },
                                onDelete = {
                                    // Simulated message deletion from repository
                                    Toast.makeText(context, "Deleted message", Toast.LENGTH_SHORT).show()
                                },
                                onSpeak = {
                                    viewModel.speakOut(message.content)
                                },
                                onMakeVideo = { imageUrl ->
                                    onMakeVideoFromImage(imageUrl, message.content)
                                }
                            )
                        }

                        if (isChatLoading) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = CosmicSlateMedium)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = CosmicAccentTeal,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = Translation.translate("generating", appLanguage),
                                                color = Color.LightGray,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Simulated Speech Mode Modal
            AnimatedVisibility(
                visible = isListeningState,
                enter = fadeIn() + slideInVertically { it / 2 },
                exit = fadeOut() + slideOutVertically { it / 2 }
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorderDark, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
                    color = CosmicSlateDark,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = Translation.translate("mic_listening", appLanguage),
                            color = CosmicAccentEmerald,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = Translation.translate("mic_speak", appLanguage),
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        // Animated pulsing circle
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(CosmicAccentEmerald.copy(alpha = 0.2f))
                                .clickable {
                                    isListeningState = false
                                    // Simulate STT input
                                    textInput = "Translate 'hello how are you' to bengali language"
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Active Mic",
                                tint = CosmicAccentEmerald,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Tap to Stop & Transcribe",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Simulated Attachment Menu
            AnimatedVisibility(
                visible = showAttachmentMenu,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, GlassBorderDark, RoundedCornerShape(16.dp))
                        .padding(8.dp),
                    color = CosmicSlateMedium,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        AttachmentOption(
                            title = Translation.translate("camera", appLanguage),
                            icon = Icons.Default.PhotoCamera,
                            color = CosmicAccentEmerald
                        ) {
                            showAttachmentMenu = false
                            // Add mock attachment text
                            textInput += " [Camera Frame attached] "
                        }
                        AttachmentOption(
                            title = Translation.translate("photo_upload", appLanguage),
                            icon = Icons.Default.PhotoLibrary,
                            color = CosmicAccentTeal
                        ) {
                            showAttachmentMenu = false
                            textInput += " [Photo uploaded] "
                        }
                        AttachmentOption(
                            title = Translation.translate("file", appLanguage),
                            icon = Icons.Default.AttachFile,
                            color = CosmicPurpleMain
                        ) {
                            showAttachmentMenu = false
                            textInput += " [Document attached] "
                        }
                    }
                }
            }

            // Input Row Section
            Surface(
                color = CosmicSlateDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // "+" Attachment Button
                    IconButton(
                        onClick = { showAttachmentMenu = !showAttachmentMenu },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CosmicSlateMedium)
                    ) {
                        Icon(
                            imageVector = if (showAttachmentMenu) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = "Attach",
                            tint = CosmicAccentTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Text Input Bar
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = { textInput = it },
                        placeholder = { 
                            Text(
                                Translation.translate("type_to_chat", appLanguage),
                                color = Color.Gray,
                                fontSize = 14.sp
                            ) 
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = CosmicSlateMedium,
                            unfocusedContainerColor = CosmicSlateMedium,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(max = 120.dp)
                            .testTag("chat_input_text"),
                        maxLines = 4,
                        trailingIcon = {
                            // Microphone Speech-to-Text icon
                            IconButton(onClick = { isListeningState = true }) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Speak Input",
                                    tint = CosmicAccentTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Send Button
                    IconButton(
                        onClick = {
                            if (textInput.isNotBlank()) {
                                viewModel.sendMessage(textInput)
                                textInput = ""
                            }
                        },
                        enabled = textInput.isNotBlank(),
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(if (textInput.isNotBlank()) CosmicAccentEmerald else CosmicSlateMedium)
                            .testTag("send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (textInput.isNotBlank()) CosmicDeepSpace else Color.Gray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(
    message: ChatMessage,
    language: String,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
    onSpeak: () -> Unit,
    onMakeVideo: (String) -> Unit
) {
    val isUser = message.role == "user"
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.fillMaxWidth(0.85f)
        ) {
            // Dialogue Bubble Box
            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) CosmicAccentTeal.copy(alpha = 0.2f) else CosmicSlateDark,
                border = BorderStroke(1.dp, if (isUser) CosmicAccentTeal.copy(alpha = 0.3f) else GlassBorderDark),
                modifier = Modifier
                    .combinedClickable(
                        onClick = { showMenu = !showMenu },
                        onLongClick = { showMenu = true }
                    )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = message.content,
                        color = Color.White,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )

                    // If message contains a generated image url (like from simulation or text)
                    if (message.imageUrl != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                        ) {
                            AsyncImage(
                                model = message.imageUrl,
                                contentDescription = "Generated Art",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Watermark
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Elek",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Light
                                )
                            }
                        }

                        // Make Video action link directly inside bubble
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { onMakeVideo(message.imageUrl) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CosmicAccentEmerald.copy(alpha = 0.2f),
                                contentColor = CosmicAccentEmerald
                            ),
                            border = BorderStroke(1.dp, CosmicAccentEmerald),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.VideoCall,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = Translation.translate("make_video", language),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action overlay menu for dialogue options (Copy, Share, Speak)
            AnimatedVisibility(visible = showMenu) {
                Row(
                    modifier = Modifier
                        .background(CosmicSlateMedium, RoundedCornerShape(12.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Copy
                    IconButton(onClick = {
                        onCopy()
                        showMenu = false
                    }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                    }
                    // Speak aloud (TTS)
                    IconButton(onClick = {
                        onSpeak()
                        showMenu = false
                    }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Speak", tint = CosmicAccentTeal, modifier = Modifier.size(14.dp))
                    }
                    // Share
                    IconButton(onClick = {
                        onShare()
                        showMenu = false
                    }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                    }
                    // Delete
                    IconButton(onClick = {
                        onDelete()
                        showMenu = false
                    }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AttachmentOption(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            color = Color.LightGray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
