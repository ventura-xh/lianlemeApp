package com.example.helloandroid.utils

import android.graphics.Bitmap
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.core.graphics.createBitmap

@Composable
fun rememberScreenCapture() = ScreenCapture()

class ScreenCapture {

    @Composable
    fun getView(): View {
        return LocalView.current
    }

    suspend fun capture(view: View): Bitmap? {
        return withContext(Dispatchers.Main) {
            try {
                // 确保 View 已绘制完成
                view.measure(
                    View.MeasureSpec.makeMeasureSpec(view.width, View.MeasureSpec.EXACTLY),
                    View.MeasureSpec.makeMeasureSpec(view.height, View.MeasureSpec.EXACTLY)
                )
                view.layout(0, 0, view.width, view.height)

                val bitmap = createBitmap(view.width, view.height)
                val canvas = android.graphics.Canvas(bitmap)
                view.draw(canvas)
                bitmap
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}