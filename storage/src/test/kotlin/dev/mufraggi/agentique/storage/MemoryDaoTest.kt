package dev.mufraggi.agentique.storage

import android.content.Context
import android.content.ContextWrapper
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Test

/** Runs on the JVM against the bundled SQLite: no emulator, no Robolectric. */
class MemoryDaoTest {
    // An in-memory database never touches the context beyond asking for the application one.
    private val hostContext: Context = object : ContextWrapper(null) {
        override fun getApplicationContext(): Context = this
    }

    private val db = Room.inMemoryDatabaseBuilder(hostContext, AgentiqueDatabase::class.java)
        .setDriver(BundledSQLiteDriver())
        .build()

    @After
    fun closeDb() = db.close()

    @Test
    fun `returns inserted memories oldest first`() = runTest {
        val later = MemoryEntity("m2", "Travaille à Paris", createdAtEpochMillis = 2_000, runId = "r1")
        val earlier = MemoryEntity("m1", "Habite à Lyon", createdAtEpochMillis = 1_000, runId = "r1")

        db.memoryDao().insert(later)
        db.memoryDao().insert(earlier)

        assertEquals(listOf(earlier, later), db.memoryDao().all())
    }
}
