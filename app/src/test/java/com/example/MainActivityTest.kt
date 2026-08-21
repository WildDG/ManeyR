package com.example

import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Robolectric
import org.robolectric.annotation.Config
import androidx.test.core.app.ApplicationProvider
import android.content.Intent

@RunWith(RobolectricTestRunner::class)
@Config(application = FinanceApplication::class)
class MainActivityTest {

    @Test
    fun testMainActivityStarts() {
        val intent = Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java)
        val activity = Robolectric.buildActivity(MainActivity::class.java, intent).create().start().resume().get()
        println("MainActivity started successfully!")
    }
}
