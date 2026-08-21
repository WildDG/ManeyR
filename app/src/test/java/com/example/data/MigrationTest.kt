package com.example.data

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import androidx.test.core.app.ApplicationProvider
import com.example.data.db.AppDatabase
import androidx.room.Room

@RunWith(RobolectricTestRunner::class)
class MigrationTest {

    @Test
    fun testMigration() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        try {
            val db = AppDatabase.getDatabase(context)
            val dao = db.accountDao()
            dao.getAllAccounts()
            println("Migration SUCCESS!")
        } catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }
}
