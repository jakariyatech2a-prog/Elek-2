package com.example.data

import kotlinx.coroutines.flow.Flow

class ElekRepository(
    private val chatDao: ChatDao,
    private val imageHistoryDao: ImageHistoryDao,
    private val videoHistoryDao: VideoHistoryDao
) {
    // --- Chat Session Operations ---
    val allSessions: Flow<List<ChatSession>> = chatDao.getAllSessions()

    suspend fun insertSession(session: ChatSession) {
        chatDao.insertSession(session)
    }

    suspend fun deleteSession(sessionId: String) {
        chatDao.deleteSessionById(sessionId)
        chatDao.deleteMessagesForSession(sessionId)
    }

    // --- Message Operations ---
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>> {
        return chatDao.getMessagesForSession(sessionId)
    }

    suspend fun insertMessage(message: ChatMessage) {
        chatDao.insertMessage(message)
    }

    // --- Image History Operations ---
    val allImages: Flow<List<ImageHistory>> = imageHistoryDao.getAllImages()

    suspend fun insertImage(image: ImageHistory) {
        imageHistoryDao.insertImage(image)
    }

    suspend fun deleteImage(id: Int) {
        imageHistoryDao.deleteImageById(id)
    }

    // --- Video History Operations ---
    val allVideos: Flow<List<VideoHistory>> = videoHistoryDao.getAllVideos()

    suspend fun insertVideo(video: VideoHistory) {
        videoHistoryDao.insertVideo(video)
    }

    suspend fun deleteVideo(id: Int) {
        videoHistoryDao.deleteVideoById(id)
    }
}
