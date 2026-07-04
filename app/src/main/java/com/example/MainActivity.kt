package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.ui.*
import com.example.ui.theme.CosmicDeepSpace
import com.example.ui.theme.CosmicSlateDark
import com.example.ui.theme.CosmicSlateMedium
import com.example.ui.theme.GlassBorderDark
import com.example.ui.theme.CosmicAccentTeal
import com.example.ui.theme.CosmicAccentEmerald
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val isSplashFinished by viewModel.isSplashFinished.collectAsState()
                val currentUser by viewModel.currentUser.collectAsState()
                val isInternetConnected by viewModel.isInternetConnected.collectAsState()
                val config by viewModel.configState.collectAsState()
                
                // Active subscreen state: "home" | "voice" | "admin"
                var activeRoute by remember { mutableStateOf("home") }
                
                // Bottom tab index selection
                var selectedTabIndex by remember { mutableStateOf(0) }

                // Image -> Video flow parameter storage
                var passedImageUrl by remember { mutableStateOf<String?>(null) }
                var passedPrompt by remember { mutableStateOf<String?>(null) }

                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val coroutineScope = rememberCoroutineScope()
                
                val chatSessions by viewModel.chatSessions.collectAsState()
                val currentSessionId by viewModel.currentSessionId.collectAsState()

                if (!isSplashFinished) {
                    SplashScreen(viewModel = viewModel)
                } else if (currentUser == null) {
                    LoginScreen(viewModel = viewModel)
                } else if (!isInternetConnected) {
                    NoInternetScreen(viewModel = viewModel, appLanguage = config.appLanguage)
                } else {
                    // Main App Shell containing historical chat session drawer
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet(
                                drawerContainerColor = CosmicSlateDark,
                                drawerContentColor = Color.White,
                                modifier = Modifier.width(300.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = "Chat History",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = CosmicAccentTeal,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    // Create New Chat FAB
                                    Button(
                                        onClick = {
                                            viewModel.createNewSession()
                                            coroutineScope.launch { drawerState.close() }
                                            selectedTabIndex = 1 // AI Chat tab
                                            activeRoute = "home"
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 16.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = CosmicAccentEmerald),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = "New Session", tint = CosmicDeepSpace)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "New Chat", color = CosmicDeepSpace, fontWeight = FontWeight.Bold)
                                    }

                                    // List sessions
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .verticalScroll(rememberScrollState()),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        chatSessions.forEach { session ->
                                            val isSelected = session.id == currentSessionId
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(if (isSelected) CosmicSlateMedium else Color.Transparent)
                                                    .clickable {
                                                        viewModel.selectSession(session.id)
                                                        coroutineScope.launch { drawerState.close() }
                                                        selectedTabIndex = 1 // AI Chat
                                                        activeRoute = "home"
                                                    }
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Chat,
                                                    contentDescription = null,
                                                    tint = if (isSelected) CosmicAccentTeal else Color.Gray,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(12.dp))
                                                Text(
                                                    text = session.title,
                                                    color = if (isSelected) Color.White else Color.LightGray,
                                                    fontSize = 13.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    ) {
                        Scaffold(
                            bottomBar = {
                                if (activeRoute == "home") {
                                    NavigationBar(
                                        containerColor = CosmicSlateDark,
                                        modifier = Modifier.border(1.dp, GlassBorderDark, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                                    ) {
                                        // Tab 0: Home
                                        NavigationBarItem(
                                            selected = selectedTabIndex == 0,
                                            onClick = { selectedTabIndex = 0 },
                                            icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                                            label = { Text("Home", fontSize = 10.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = CosmicDeepSpace,
                                                selectedTextColor = CosmicAccentTeal,
                                                indicatorColor = CosmicAccentTeal,
                                                unselectedIconColor = Color.Gray,
                                                unselectedTextColor = Color.Gray
                                            )
                                        )

                                        // Tab 1: AI Chat
                                        NavigationBarItem(
                                            selected = selectedTabIndex == 1,
                                            onClick = { selectedTabIndex = 1 },
                                            icon = { Icon(Icons.Default.Chat, contentDescription = "AI Chat") },
                                            label = { Text("AI Chat", fontSize = 10.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = CosmicDeepSpace,
                                                selectedTextColor = CosmicAccentEmerald,
                                                indicatorColor = CosmicAccentEmerald,
                                                unselectedIconColor = Color.Gray,
                                                unselectedTextColor = Color.Gray
                                            )
                                        )

                                        // Tab 2: AI Image
                                        NavigationBarItem(
                                            selected = selectedTabIndex == 2,
                                            onClick = { selectedTabIndex = 2 },
                                            icon = { Icon(Icons.Default.Image, contentDescription = "AI Image") },
                                            label = { Text("AI Art", fontSize = 10.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = CosmicDeepSpace,
                                                selectedTextColor = CosmicAccentTeal,
                                                indicatorColor = CosmicAccentTeal,
                                                unselectedIconColor = Color.Gray,
                                                unselectedTextColor = Color.Gray
                                            )
                                        )

                                        // Tab 3: AI Video
                                        NavigationBarItem(
                                            selected = selectedTabIndex == 3,
                                            onClick = { selectedTabIndex = 3 },
                                            icon = { Icon(Icons.Default.VideoCall, contentDescription = "AI Video") },
                                            label = { Text("AI Video", fontSize = 10.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = Color.White,
                                                selectedTextColor = CosmicAccentEmerald,
                                                indicatorColor = CosmicAccentEmerald,
                                                unselectedIconColor = Color.Gray,
                                                unselectedTextColor = Color.Gray
                                            )
                                        )

                                        // Tab 4: Profile
                                        NavigationBarItem(
                                            selected = selectedTabIndex == 4,
                                            onClick = { selectedTabIndex = 4 },
                                            icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                                            label = { Text("Profile", fontSize = 10.sp) },
                                            colors = NavigationBarItemDefaults.colors(
                                                selectedIconColor = CosmicDeepSpace,
                                                selectedTextColor = CosmicAccentTeal,
                                                indicatorColor = CosmicAccentTeal,
                                                unselectedIconColor = Color.Gray,
                                                unselectedTextColor = Color.Gray
                                            )
                                        )
                                    }
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                when (activeRoute) {
                                    "voice" -> {
                                        VoiceScreen(
                                            viewModel = viewModel,
                                            onNavigateBack = { activeRoute = "home" }
                                        )
                                    }
                                    "admin" -> {
                                        AdminScreen(
                                            viewModel = viewModel,
                                            onNavigateBack = { activeRoute = "home" }
                                        )
                                    }
                                    else -> {
                                        when (selectedTabIndex) {
                                            0 -> {
                                                HomeScreen(
                                                    viewModel = viewModel,
                                                    onTabSelected = { selectedTabIndex = it },
                                                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                                                    onNavigateToVoice = { activeRoute = "voice" }
                                                )
                                            }
                                            1 -> {
                                                ChatScreen(
                                                    viewModel = viewModel,
                                                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                                                    onMakeVideoFromImage = { url, prompt ->
                                                        passedImageUrl = url
                                                        passedPrompt = prompt
                                                        selectedTabIndex = 3 // Route to Video screen
                                                    }
                                                )
                                            }
                                            2 -> {
                                                ImageScreen(
                                                    viewModel = viewModel,
                                                    onMakeVideoFromImage = { url, prompt ->
                                                        passedImageUrl = url
                                                        passedPrompt = prompt
                                                        selectedTabIndex = 3 // Switch to Video tab
                                                    }
                                                )
                                            }
                                            3 -> {
                                                VideoScreen(
                                                    viewModel = viewModel,
                                                    passedImageUrl = passedImageUrl,
                                                    passedPrompt = passedPrompt,
                                                    onClearPassedParams = {
                                                        passedImageUrl = null
                                                        passedPrompt = null
                                                    }
                                                )
                                            }
                                            else -> {
                                                ProfileScreen(
                                                    viewModel = viewModel,
                                                    onNavigateToAdmin = { activeRoute = "admin" },
                                                    onNavigateToChat = { sessionId ->
                                                        viewModel.selectSession(sessionId)
                                                        selectedTabIndex = 1 // AI Chat
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up TextToSpeech and network listeners
        viewModel.cleanup()
    }
}
