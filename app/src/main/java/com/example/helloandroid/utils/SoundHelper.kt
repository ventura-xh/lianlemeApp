// utils/SoundHelper.kt

package com.example.helloandroid.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator

object SoundHelper {

    private var mediaPlayer: MediaPlayer? = null
    private var toneGenerator: ToneGenerator? = null

    // ✅ 音效类型
    enum class SoundType {
        NOTIFICATION,   // 系统通知音
        TONE,           // 提示音（ToneGenerator）
        BEEP,          // 短促提示音
        TIMER_END,     // 倒计时结束音
        CUSTOM         // 自定义音效
    }

    /**
     * 播放音效
     */
    fun playSound(
        context: Context,
        type: SoundType,
        customResId: Int? = null,
        vibrate: Boolean = true
    ) {
        when (type) {
            SoundType.NOTIFICATION -> playNotificationSound(context)
            SoundType.TONE -> playTone()
            SoundType.BEEP -> playBeep()
            SoundType.TIMER_END -> playTimerEnd()
            SoundType.CUSTOM -> customResId?.let { playCustomSound(context, it) }
        }
        if (vibrate) {
            vibrate(context)
        }
    }

    /**
     * 播放系统默认提示音
     */
    fun playNotificationSound(context: Context) {
        try {
            // 释放之前的资源
            mediaPlayer?.release()

            // 使用系统默认通知音
            val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setDataSource(context, notificationUri)
                prepare()
                start()
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // 如果失败，使用 ToneGenerator
            playTone()
        }
    }

    /**
     * 播放提示音（ToneGenerator）
     */
    fun playTone() {
        try {
            toneGenerator?.release()
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100).apply {
                startTone(ToneGenerator.TONE_PROP_BEEP2, 1000)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 播放滴答声（短促）
     */
    fun playBeep() {
        try {
            toneGenerator?.release()
            toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100).apply {
                startTone(ToneGenerator.TONE_PROP_BEEP, 200)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 播放倒计时结束音（长音）
     */
    fun playTimerEnd() {
        try {
            toneGenerator?.release()
            toneGenerator = ToneGenerator(AudioManager.STREAM_ALARM, 100).apply {
                startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 1500)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 播放自定义音效
     */
    fun playCustomSound(context: Context, resId: Int) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer.create(context, resId)?.apply {
                start()
                setOnCompletionListener {
                    it.release()
                    mediaPlayer = null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 震动 + 声音
     */
    fun playRestComplete(context: Context) {
        // 播放声音
        playSound(context, SoundType.TONE)
        // 震动
        vibrate(context, 500)
    }

    /**
     * 震动
     */
    fun vibrate(context: Context, duration: Long = 500) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        duration,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(duration)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        mediaPlayer?.release()
        mediaPlayer = null
        toneGenerator?.release()
        toneGenerator = null
    }
}