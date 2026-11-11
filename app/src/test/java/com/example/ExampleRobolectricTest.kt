package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AppItem
import com.example.data.repository.AppRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Glass Launcher", appName)
  }

  @Test
  fun `app repository favorite toggle`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = AppRepository(context)

    val pkg = "com.test.app"
    val isFavFirst = repo.toggleFavorite(pkg)
    assertEquals(true, isFavFirst)

    val isFavSecond = repo.toggleFavorite(pkg)
    assertEquals(false, isFavSecond)
  }

  @Test
  fun `app repository ram boost stats`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val repo = AppRepository(context)
    val stats = repo.boostRam()
    assertNotNull(stats)
  }
}
