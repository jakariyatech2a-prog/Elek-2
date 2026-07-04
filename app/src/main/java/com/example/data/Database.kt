package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

// --- Room Entities ---

@androidx.room.Entity(tableName = "chat_sessions")
data class ChatSession(
    @androidx.room.PrimaryKey val id: String,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
)

@androidx.room.Entity(tableName = "chat_messages")
data class ChatMessage(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sessionId: String,
    val role: String, // "user", "model"
    val content: String,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

@androidx.room.Entity(tableName = "image_history")
data class ImageHistory(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Int = 0,
    val prompt: String,
    val imageUrl: String,
    val style: String,
    val aspectRatio: String,
    val timestamp: Long = System.currentTimeMillis()
)

@androidx.room.Entity(tableName = "video_history")
data class VideoHistory(
    @androidx.room.PrimaryKey(autoGenerate = true) val id: Int = 0,
    val prompt: String,
    val videoUrl: String,
    val duration: Int, // 6, 8, 10
    val aspectRatio: String, // "16:9", "9:16"
    val mode: String, // "Text to Video", "Image to Video"
    val timestamp: Long = System.currentTimeMillis()
)

// --- DAOs ---

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<ChatSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSession)

    @Query("DELETE FROM chat_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: String)

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessage)

    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)
}

@Dao
interface ImageHistoryDao {
    @Query("SELECT * FROM image_history ORDER BY timestamp DESC")
    fun getAllImages(): Flow<List<ImageHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: ImageHistory)

    @Query("DELETE FROM image_history WHERE id = :id")
    suspend fun deleteImageById(id: Int)
}

@Dao
interface VideoHistoryDao {
    @Query("SELECT * FROM video_history ORDER BY timestamp DESC")
    fun getAllVideos(): Flow<List<VideoHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: VideoHistory)

    @Query("DELETE FROM video_history WHERE id = :id")
    suspend fun deleteVideoById(id: Int)
}

// --- Database Class ---

@Database(
    entities = [ChatSession::class, ChatMessage::class, ImageHistory::class, VideoHistory::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
    abstract fun imageHistoryDao(): ImageHistoryDao
    abstract fun videoHistoryDao(): VideoHistoryDao
}
