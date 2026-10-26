package dev.mufraggi.agentique.storage

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase

@Database(entities = [MemoryEntity::class], version = 1)
internal abstract class AgentiqueDatabase : RoomDatabase() {
    abstract fun memoryDao(): MemoryDao
}

/** Row of a memory; parsed back into a domain `Memory` on read (never trusted as-is). */
@Entity(tableName = "memory")
internal data class MemoryEntity(
    @PrimaryKey val id: String,
    val text: String,
    val createdAtEpochMillis: Long,
    val runId: String,
)

@Dao
internal interface MemoryDao {
    @Insert
    suspend fun insert(memory: MemoryEntity)

    @Query("SELECT * FROM memory ORDER BY createdAtEpochMillis")
    suspend fun all(): List<MemoryEntity>
}
