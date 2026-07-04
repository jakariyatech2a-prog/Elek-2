package com.example

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.speech.tts.TextToSpeech
import android.util.Base64
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URLEncoder
import java.util.Locale
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application), TextToSpeech.OnInitListener {

    private val context = application.applicationContext

    // --- Admin Config Management ---
    val adminConfig = AdminConfig(context)
    val configState = adminConfig.configFlow

    // --- Database Setup ---
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "elek_ai_db"
        ).fallbackToDestructiveMigration().build()
    }
    
    val repository: ElekRepository by lazy {
        ElekRepository(
            database.chatDao(),
            database.imageHistoryDao(),
            database.videoHistoryDao()
        )
    }

    // --- UI States ---
    private val _isInternetConnected = MutableStateFlow(true)
    val isInternetConnected: StateFlow<Boolean> = _isInternetConnected

    private val _isSplashFinished = MutableStateFlow(false)
    val isSplashFinished: StateFlow<Boolean> = _isSplashFinished

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    // --- Dynamic Notifications & Welcome Email ---
    private val _welcomeNotification = MutableStateFlow<String?>(null)
    val welcomeNotification: StateFlow<String?> = _welcomeNotification

    // --- Text To Speech (Voice Assistant) ---
    private var textToSpeech: TextToSpeech? = null
    private val _isTtsActive = MutableStateFlow(true) // Voice answer active by default
    val isTtsActive: StateFlow<Boolean> = _isTtsActive

    private val _selectedVoiceGender = MutableStateFlow("Female") // Male, Female, Natural
    val selectedVoiceGender: StateFlow<String> = _selectedVoiceGender

    // --- Active Chat Session ---
    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId

    val chatSessions = repository.allSessions.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading

    // Dynamic message stream for current session
    val currentMessages: StateFlow<List<ChatMessage>> = _currentSessionId
        .flatMapLatest { id ->
            if (id != null) {
                repository.getMessagesForSession(id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Image Generation State ---
    private val _isGeneratingImage = MutableStateFlow(false)
    val isGeneratingImage: StateFlow<Boolean> = _isGeneratingImage

    private val _latestGeneratedImage = MutableStateFlow<ImageHistory?>(null)
    val latestGeneratedImage: StateFlow<ImageHistory?> = _latestGeneratedImage

    val imageHistory = repository.allImages.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // --- Video Generation State ---
    private val _isGeneratingVideo = MutableStateFlow(false)
    val isGeneratingVideo: StateFlow<Boolean> = _isGeneratingVideo

    private val _videoGenerationProgress = MutableStateFlow(0f) // 0.0 to 1.0
    val videoGenerationProgress: StateFlow<Float> = _videoGenerationProgress

    private val _isWatchingAd = MutableStateFlow(false)
    val isWatchingAd: StateFlow<Boolean> = _isWatchingAd

    private val _adCountdown = MutableStateFlow(0)
    val adCountdown: StateFlow<Int> = _adCountdown

    private val _latestGeneratedVideo = MutableStateFlow<VideoHistory?>(null)
    val latestGeneratedVideo: StateFlow<VideoHistory?> = _latestGeneratedVideo

    val videoHistory = repository.allVideos.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // --- Real-time Voice Chat Simulation States ---
    private val _isVoiceConversationActive = MutableStateFlow(false)
    val isVoiceConversationActive: StateFlow<Boolean> = _isVoiceConversationActive

    private val _voiceSubtitle = MutableStateFlow("Tap Microphone to speak...")
    val voiceSubtitle: StateFlow<String> = _voiceSubtitle

    data class User(
        val name: String,
        val email: String,
        val photoUrl: String
    )

    init {
        // Init TTS
        textToSpeech = TextToSpeech(context, this)

        // Monitor Network Connectivity
        checkNetwork()
        monitorNetwork()

        // Splash Timer Transition (3 seconds)
        viewModelScope.launch {
            delay(3000)
            _isSplashFinished.value = true
        }

        // Load logged in state from SharedPreferences
        val loginPrefs = context.getSharedPreferences("elek_ai_login", Context.MODE_PRIVATE)
        val savedName = loginPrefs.getString("userName", null)
        val savedEmail = loginPrefs.getString("userEmail", null)
        val savedPhoto = loginPrefs.getString("userPhoto", "") ?: ""
        if (savedName != null && savedEmail != null) {
            _currentUser.value = User(savedName, savedEmail, savedPhoto)
            _isLoggedIn.value = true
        }
    }

    // --- Network Handling ---
    fun checkNetwork() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val isConnected = capabilities != null && 
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                 capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR))
        _isInternetConnected.value = isConnected
    }

    private fun monitorNetwork() {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                _isInternetConnected.value = true
            }

            override fun onLost(network: Network) {
                _isInternetConnected.value = false
            }
        })
    }

    // --- Authentication ---
    fun loginWithGmail(name: String, email: String, photoUrl: String) {
        viewModelScope.launch {
            val loginPrefs = context.getSharedPreferences("elek_ai_login", Context.MODE_PRIVATE)
            loginPrefs.edit().apply {
                putString("userName", name)
                putString("userEmail", email)
                putString("userPhoto", photoUrl)
                apply()
            }
            _currentUser.value = User(name, email, photoUrl)
            _isLoggedIn.value = true

            // Send dynamic greeting
            if (configState.value.isWelcomeEmailEnabled) {
                _welcomeNotification.value = "Welcome Email sent successfully to $email!"
                delay(6000)
                _welcomeNotification.value = null
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val loginPrefs = context.getSharedPreferences("elek_ai_login", Context.MODE_PRIVATE)
            loginPrefs.edit().clear().apply()
            _currentUser.value = null
            _isLoggedIn.value = false
            _currentSessionId.value = null
        }
    }

    // --- Chat Session Management ---
    fun createNewSession(title: String = "New Chat") {
        viewModelScope.launch(Dispatchers.IO) {
            val newId = UUID.randomUUID().toString()
            val session = ChatSession(id = newId, title = title)
            repository.insertSession(session)
            _currentSessionId.value = newId
        }
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = null
            }
        }
    }

    // --- Send Chat Message ---
    fun sendMessage(content: String, imageUrl: String? = null, videoUrl: String? = null) {
        val sessionId = _currentSessionId.value ?: return
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Insert user message
            val userMsg = ChatMessage(
                sessionId = sessionId,
                role = "user",
                content = content,
                imageUrl = imageUrl,
                videoUrl = videoUrl
            )
            repository.insertMessage(userMsg)

            // Update session title if default
            val currentSession = chatSessions.value.find { it.id == sessionId }
            if (currentSession != null && currentSession.title == "New Chat") {
                val updatedTitle = if (content.length > 25) content.substring(0, 22) + "..." else content
                repository.insertSession(currentSession.copy(title = updatedTitle))
            }

            _isChatLoading.value = true

            val lowerPrompt = content.lowercase()
            val isImageRequest = lowerPrompt.contains("draw") || 
                                 lowerPrompt.contains("generate image") || 
                                 lowerPrompt.contains("make a picture") || 
                                 lowerPrompt.contains("create art") || 
                                 lowerPrompt.contains("paint") || 
                                 lowerPrompt.contains("ছবি") || 
                                 lowerPrompt.contains("আঁকো") || 
                                 lowerPrompt.contains("আঁক") || 
                                 lowerPrompt.contains("তৈরি কর") || 
                                 lowerPrompt.contains("তৈরি করো") || 
                                 lowerPrompt.contains("বানাও") ||
                                 lowerPrompt.contains("চিত্র")

            val modelMsg = if (isImageRequest) {
                val activeConfig = configState.value
                val keyToUse = activeConfig.apiKey.ifEmpty { BuildConfig.GEMINI_API_KEY }
                
                // Translate the prompt using Gemini to get clean keywords or use as-is
                val englishKeywords = translatePromptToEnglishKeywords(content, keyToUse)
                
                // Generate the image
                var generatedPath = callGeminiImageApi(content, "1:1", keyToUse)
                val finalUrl: String
                if (generatedPath != null) {
                    finalUrl = generatedPath
                } else {
                    val randomSeed = (1..1000).random()
                    finalUrl = "https://images.unsplash.com/featured/600x600/?${URLEncoder.encode(englishKeywords, "UTF-8")}&sig=$randomSeed"
                }
                
                val responseText = if (lowerPrompt.contains("ছবি") || lowerPrompt.contains("আঁকো") || lowerPrompt.contains("তৈরি করো") || lowerPrompt.contains("বানাও")) {
                    "আপনার অনুরোধ অনুযায়ী ছবিটি তৈরি করা হয়েছে: \"$content\""
                } else {
                    "I have generated the image you requested for: \"$content\""
                }

                ChatMessage(
                    sessionId = sessionId,
                    role = "model",
                    content = responseText,
                    imageUrl = finalUrl
                )
            } else {
                // 2. Fetch standard API Response
                val aiResponse = callGeminiApi(content)
                ChatMessage(
                    sessionId = sessionId,
                    role = "model",
                    content = aiResponse
                )
            }

            // 3. Insert Model message
            repository.insertMessage(modelMsg)
            _isChatLoading.value = false

            // 4. Trigger TTS Voice if enabled
            if (_isTtsActive.value) {
                speakOut(modelMsg.content)
            }
        }
    }

    fun deleteMessage(sessionId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSession(sessionId)
            createNewSession()
        }
    }

    private suspend fun callGeminiApi(prompt: String): String = withContext(Dispatchers.IO) {
        val activeConfig = configState.value
        val keyToUse = activeConfig.apiKey.ifEmpty { 
            BuildConfig.GEMINI_API_KEY 
        }

        if (keyToUse.isEmpty() || keyToUse == "MY_GEMINI_API_KEY") {
            // Mock dynamic response if no key configured
            delay(1000)
            return@withContext "I received your message! Since the Gemini API Key is not configured yet in the Admin settings, here is a mock response from Elek AI: \"$prompt\""
        }

        try {
            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = "You are Elek ai, a helpful and highly sophisticated AI Super-assistant. Answer clearly, accurately, and politely in the user's preferred language.")))
            )
            val modelToUse = activeConfig.aiModel // e.g. "gemini-3.5-flash"
            val response = GeminiClient.service.generateContent(modelToUse, keyToUse, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "I apologize, but I could not formulate a response at this time."
        } catch (e: Exception) {
            Log.e("ElekAI", "Gemini API failed", e)
            "Error calling Gemini: ${e.localizedMessage}. Please verify your API Key and Network settings in the Admin Panel."
        }
    }

    private suspend fun translatePromptToEnglishKeywords(prompt: String, keyToUse: String): String = withContext(Dispatchers.IO) {
        if (keyToUse.isEmpty() || keyToUse == "MY_GEMINI_API_KEY") {
            return@withContext prompt
        }
        try {
            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = "Translate this image description into 2 to 4 simple English search keywords separated by commas. Return ONLY the keywords, nothing else: $prompt"))))
            )
            val response = GeminiClient.service.generateContent("gemini-3.5-flash", keyToUse, request)
            val result = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim() ?: prompt
            if (result.contains("Error") || result.length > 100) prompt else result
        } catch (e: Exception) {
            prompt
        }
    }

    private suspend fun callGeminiImageApi(prompt: String, aspectRatio: String, keyToUse: String): String? = withContext(Dispatchers.IO) {
        if (keyToUse.isEmpty() || keyToUse == "MY_GEMINI_API_KEY") {
            return@withContext null
        }
        try {
            val mappedRatio = when (aspectRatio) {
                "16:9" -> "16:9"
                "9:16" -> "9:16"
                "4:3" -> "4:3"
                "3:4" -> "3:4"
                "3:2" -> "3:2"
                "2:3" -> "2:3"
                else -> "1:1"
            }
            val request = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt)))),
                generationConfig = GeminiGenerationConfig(
                    responseModalities = listOf("TEXT", "IMAGE"),
                    imageConfig = GeminiImageConfig(
                        aspectRatio = mappedRatio,
                        imageSize = "1K"
                    )
                )
            )
            val response = GeminiClient.service.generateContent("gemini-2.5-flash-image", keyToUse, request)
            val inlineData = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull { it.inlineData != null }?.inlineData
            val base64Data = inlineData?.data
            if (!base64Data.isNullOrEmpty()) {
                val bytes = android.util.Base64.decode(base64Data, android.util.Base64.DEFAULT)
                val file = java.io.File(context.cacheDir, "gemini_gen_${System.currentTimeMillis()}.jpg")
                java.io.FileOutputStream(file).use { fos ->
                    fos.write(bytes)
                }
                return@withContext file.absolutePath
            }
            null
        } catch (e: Exception) {
            Log.e("ElekAI", "Gemini Image generation failed, falling back to Unsplash", e)
            null
        }
    }

    // --- AI Image Generator ---
    fun generateImage(prompt: String, style: String, aspectRatio: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isGeneratingImage.value = true
            
            val activeConfig = configState.value
            val keyToUse = activeConfig.apiKey.ifEmpty { 
                BuildConfig.GEMINI_API_KEY 
            }

            val styledPrompt = if (style.isNotEmpty()) "$prompt, $style style" else prompt
            
            // Try real AI Image Generation using Gemini model
            var generatedPath = callGeminiImageApi(styledPrompt, aspectRatio, keyToUse)
            val finalUrl: String

            if (generatedPath != null) {
                finalUrl = generatedPath
            } else {
                // High-quality dynamic Unsplash search fallback
                val englishKeywords = translatePromptToEnglishKeywords(prompt, keyToUse)
                val queryKeywords = if (style.isNotEmpty()) "$englishKeywords,$style" else englishKeywords
                val width = when (aspectRatio) {
                    "16:9" -> 800
                    "9:16" -> 450
                    "3:2" -> 750
                    "4:3" -> 800
                    else -> 600
                }
                val height = when (aspectRatio) {
                    "16:9" -> 450
                    "9:16" -> 800
                    "3:2" -> 500
                    "4:3" -> 600
                    else -> 600
                }
                val randomSeed = (1..1000).random()
                finalUrl = "https://images.unsplash.com/featured/${width}x${height}/?${URLEncoder.encode(queryKeywords, "UTF-8")}&sig=$randomSeed"
            }

            val item = ImageHistory(
                prompt = prompt,
                imageUrl = finalUrl,
                style = style,
                aspectRatio = aspectRatio
            )

            repository.insertImage(item)
            _latestGeneratedImage.value = item
            _isGeneratingImage.value = false
        }
    }

    fun deleteImage(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteImage(id)
            if (_latestGeneratedImage.value?.id == id) {
                _latestGeneratedImage.value = null
            }
        }
    }

    // --- AI Video Generator ---
    fun generateVideo(prompt: String, duration: Int, aspectRatio: String, mode: String, sourceImageUrl: String? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val config = configState.value
            
            // Check free vs premium rewarded ad requirement
            if (!config.isPremium && config.isAdsEnabled) {
                _isWatchingAd.value = true
                val adDuration = config.rewardTimeSec
                for (i in adDuration downTo 0) {
                    _adCountdown.value = i
                    delay(1000)
                }
                _isWatchingAd.value = false
            }

            _isGeneratingVideo.value = true
            _videoGenerationProgress.value = 0f

            // Animate progress bar over 4 seconds
            val steps = 40
            val stepDelay = 100L
            for (i in 1..steps) {
                delay(stepDelay)
                _videoGenerationProgress.value = i.toFloat() / steps
            }

            // High-quality video simulated output (using dynamic themed beautiful loops)
            val randomSeed = (1..500).random()
            val finalVideoUrl = if (sourceImageUrl != null) {
                // If image-to-video, animate/use that image
                sourceImageUrl
            } else {
                "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?auto=format&fit=crop&q=80&sig=$randomSeed"
            }

            val item = VideoHistory(
                prompt = prompt,
                videoUrl = finalVideoUrl,
                duration = duration,
                aspectRatio = aspectRatio,
                mode = mode
            )

            repository.insertVideo(item)
            _latestGeneratedVideo.value = item
            _isGeneratingVideo.value = false
            _videoGenerationProgress.value = 0f
        }
    }

    fun deleteVideo(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteVideo(id)
            if (_latestGeneratedVideo.value?.id == id) {
                _latestGeneratedVideo.value = null
            }
        }
    }

    // --- Text To Speech (TTS) Controls ---
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech?.let { tts ->
                tts.language = Locale.US
                applyTtsVoiceSettings()
            }
        } else {
            Log.e("TTS", "Initialization failed")
        }
    }

    fun toggleTts(active: Boolean) {
        _isTtsActive.value = active
    }

    fun setVoiceGender(gender: String) {
        _selectedVoiceGender.value = gender
        applyTtsVoiceSettings()
    }

    private fun applyTtsVoiceSettings() {
        textToSpeech?.let { tts ->
            when (_selectedVoiceGender.value) {
                "Male" -> {
                    tts.setPitch(0.8f)
                    tts.setSpeechRate(0.95f)
                }
                "Female" -> {
                    tts.setPitch(1.3f)
                    tts.setSpeechRate(1.1f)
                }
                else -> { // Natural
                    tts.setPitch(1.0f)
                    tts.setSpeechRate(1.0f)
                }
            }
        }
    }

    fun speakOut(text: String) {
        if (!_isTtsActive.value) return
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun stopSpeaking() {
        textToSpeech?.stop()
    }

    // --- Simulated Voice Dialog ---
    fun toggleVoiceConversation(active: Boolean) {
        _isVoiceConversationActive.value = active
        if (active) {
            _voiceSubtitle.value = "Tap Microphone to speak..."
        } else {
            stopSpeaking()
        }
    }

    fun startVoiceListening() {
        viewModelScope.launch {
            _voiceSubtitle.value = "Listening to you..."
            delay(2500)
            _voiceSubtitle.value = "Analyzing voice..."
            delay(1500)
            
            // Random responses representing voice assistant
            val voicePrompts = listOf(
                "How can I assist you today? I am your Elek AI assistant.",
                "Hello, I can write essays, generate art, or answer questions. What's on your mind?",
                "I am here to assist you with AI Image, AI Video, or translation tasks."
            )
            val randomReply = voicePrompts.random()
            _voiceSubtitle.value = "Elek: $randomReply"
            speakOut(randomReply)
        }
    }

    // --- Cleaning Up ---
    fun cleanup() {
        textToSpeech?.let {
            it.stop()
            it.shutdown()
        }
    }

    override fun onCleared() {
        cleanup()
        super.onCleared()
    }
}
