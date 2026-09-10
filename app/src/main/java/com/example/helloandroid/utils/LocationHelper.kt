// utils/LocationHelper.kt

package com.example.helloandroid.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object LocationHelper {

    /**
     * 获取当前城市名称
     */
    @SuppressLint("MissingPermission")
    suspend fun getCityName(context: Context): String? {
        return withContext(Dispatchers.IO) {
            try {
                // 检查权限
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    return@withContext null
                }

                val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

                // 尝试获取最后已知位置
                val location: Location? = try {
                    locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                } catch (e: Exception) {
                    null
                }

                if (location != null) {
                    // 使用 Geocoder 反向地理编码
                    val geocoder = Geocoder(context, Locale.getDefault())
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        // Android 13+ 异步方式
                        var city: String? = null
                        geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        ) { addresses ->
                            if (addresses.isNotEmpty()) {
                                city = addresses[0].locality
                                    ?: addresses[0].subAdminArea
                                            ?: addresses[0].adminArea
                            }
                        }
                        city
                    } else {
                        // Android 12 及以下同步方式
                        @Suppress("DEPRECATION")
                        val addresses = geocoder.getFromLocation(
                            location.latitude,
                            location.longitude,
                            1
                        )
                        if (!addresses.isNullOrEmpty()) {
                            addresses[0].locality
                                ?: addresses[0].subAdminArea
                                ?: addresses[0].adminArea
                        } else {
                            null
                        }
                    }
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}