package com.example

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.model.Category
import com.example.ui.components.CustomPieChart
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CategoryShare
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val mockShares = listOf(
      CategoryShare(
        category = Category("makanan", "Makanan", "Fastfood", "PENGELUARAN", "#FF5722"),
        amount = 120000.0,
        percentage = 60.0
      ),
      CategoryShare(
        category = Category("belanja", "Belanja", "ShoppingCart", "PENGELUARAN", "#E91E63"),
        amount = 80000.0,
        percentage = 40.0
      )
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        CustomPieChart(
          shares = mockShares,
          modifier = Modifier.fillMaxSize()
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}
