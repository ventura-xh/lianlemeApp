package com.example.helloandroid.service

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.helloandroid.R
import kotlinx.coroutines.*
import androidx.core.net.toUri
import com.example.helloandroid.MainActivity
import kotlin.time.Duration.Companion.milliseconds

class TrainingTimerService : Service() {

    // ============================================================
    // 协程作用域
    // ============================================================

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var timerJob: Job? = null
    private var restJob: Job? = null

    private var isTimerRunning = false
    private var isRestRunning = false

    // ============================================================
    // 悬浮窗相关
    // ============================================================

    private lateinit var windowManager: WindowManager
    private var overlayView: View? = null
    private var isOverlayShowing = false
    private val mainHandler = Handler(Looper.getMainLooper())

    // ============================================================
    // 单例模式 - 暴露 LiveData
    // ============================================================

    companion object {
        // ✅ 添加训练是否活跃的标志
        var isTrainingActive = false

        // ✅ 标记 App 是否在前台
        var isAppInForeground = true
        private val _elapsedTime = MutableLiveData(0L)
        val elapsedTime: LiveData<Long> = _elapsedTime

        private val _restTime = MutableLiveData(0)
        val restTime: LiveData<Int> = _restTime

        private const val NOTIFICATION_ID = 1001

        const val ACTION_SHOW_OVERLAY = "SHOW_OVERLAY"
        const val ACTION_HIDE_OVERLAY = "HIDE_OVERLAY"
        const val ACTION_START_TIMER = "START_TIMER"
        const val ACTION_STOP_TIMER = "STOP_TIMER"
        const val ACTION_START_REST = "START_REST"
        const val ACTION_STOP_REST = "STOP_REST"
        const val ACTION_STOP_TRAINING = "STOP_TRAINING"

        // ============================================================
        // 公开 API
        // ============================================================
        // ✅ 显示悬浮窗（如果需要在后台显示）
        fun showOverlayIfNeeded(context: Context) {
            // ✅ 只有训练活跃时才显示悬浮窗
            if (!isTrainingActive) {
                android.util.Log.d("TimerOverlay", "没有训练进行中，不显示悬浮窗")
                return
            }
            if (isAppInForeground) {
                android.util.Log.d("TimerOverlay", "App 在前台，不显示悬浮窗")
                return
            }
            context.startService(Intent(context, TrainingTimerService::class.java).apply {
                action = ACTION_SHOW_OVERLAY
            })
        }

        // ✅ 隐藏悬浮窗（如果需要）
        fun hideOverlayIfNeeded(context: Context) {
            if (isAppInForeground) {
                context.startService(Intent(context, TrainingTimerService::class.java).apply {
                    action = ACTION_HIDE_OVERLAY
                })
            }
        }

        fun showOverlay(context: Context) {
            context.startService(Intent(context, TrainingTimerService::class.java).apply {
                action = ACTION_SHOW_OVERLAY
            })
        }

        fun hideOverlay(context: Context) {
            context.startService(Intent(context, TrainingTimerService::class.java).apply {
                action = ACTION_HIDE_OVERLAY
            })
        }

        fun startTimer(context: Context) {
            context.startService(Intent(context, TrainingTimerService::class.java).apply {
                action = ACTION_START_TIMER
            })
        }

        fun stopTimer(context: Context) {
            context.startService(Intent(context, TrainingTimerService::class.java).apply {
                action = ACTION_STOP_TIMER
            })
        }

        fun startRest(context: Context, seconds: Int) {
            context.startService(Intent(context, TrainingTimerService::class.java).apply {
                action = ACTION_START_REST
                putExtra("rest_seconds", seconds)
            })
        }

        fun stopRest(context: Context) {
            context.startService(Intent(context, TrainingTimerService::class.java).apply {
                action = ACTION_STOP_REST
            })
        }

        fun stopTrainingCompletely(context: Context) {
            context.startService(Intent(context, TrainingTimerService::class.java).apply {
                action = ACTION_STOP_TRAINING
            })
        }

        // ✅ 重置时间（开始新训练时调用）
        fun resetTime() {
            _elapsedTime.postValue(0L)
            _restTime.postValue(0)
        }

        // ✅ 获取当前时间
        fun getCurrentElapsedTime(): Long {
            return _elapsedTime.value ?: 0L
        }

        fun getCurrentRestTime(): Int {
            return _restTime.value ?: 0
        }

        // ✅ 更新悬浮窗时间（外部调用）
        fun updateOverlayTime(context: Context, time: Long) {
            context.startService(Intent(context, TrainingTimerService::class.java).apply {
                action = "UPDATE_OVERLAY_TIME"
                putExtra("time", time)
            })
        }

        private fun formatTime(seconds: Long): String {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val secs = seconds % 60
            return if (hours > 0) {
                String.format("%02d:%02d:%02d", hours, minutes, secs)
            } else {
                String.format("%02d:%02d", minutes, secs)
            }
        }

        private fun vibrate(context: Context) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(android.os.VibrationEffect.createOneShot(500, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        }
    }

    // ============================================================
    // 生命周期
    // ============================================================

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_SHOW_OVERLAY -> showOverlay()
            ACTION_HIDE_OVERLAY -> hideOverlay()
            ACTION_START_TIMER -> startTimer()
            ACTION_STOP_TIMER -> stopTimer()
            ACTION_START_REST -> {
                val seconds = intent.getIntExtra("rest_seconds", 60)
                startRestTimer(seconds)
            }
            ACTION_STOP_REST -> stopRestTimer()
            "UPDATE_OVERLAY_TIME" -> {
                val time = intent.getLongExtra("time", 0)
                mainHandler.post {
                    updateOverlayTime(formatTime(time))
                }
            }
            ACTION_STOP_TRAINING -> stopTrainingCompletely()
        }
        return START_STICKY
    }

    // ============================================================
    // 计时器逻辑
    // ============================================================

    private fun startTimer() {
        if (isTimerRunning) return
        isTimerRunning = true

        // ✅ 开始前台服务
        startForeground(NOTIFICATION_ID, createNotification())

        // ✅ 启动计时，不自动显示悬浮窗（由生命周期控制）
        // 如果 App 已经在后台，显示悬浮窗
        if (!isAppInForeground) {
            showOverlayIfNeeded()
        }

        timerJob = serviceScope.launch {
            // ✅ 每次循环都从 _elapsedTime.value 读取最新值
            while (isTimerRunning) {
                delay(1000L.milliseconds)
                // ✅ 从 StateFlow/LiveData 读取当前值并 +1
                val currentValue = _elapsedTime.value ?: 0L
                val newValue = currentValue + 1
                _elapsedTime.postValue(newValue)
                mainHandler.post {
                    if (isOverlayShowing) {
                        updateOverlayTime(formatTime(newValue))
                    }
                }
            }
        }
    }

    private fun stopTimer() {
        isTimerRunning = false
        timerJob?.cancel()
        timerJob = null
    }

    private fun startRestTimer(seconds: Int) {
        if (isRestRunning) return
        isRestRunning = true
        _restTime.postValue(seconds)

        restJob = serviceScope.launch {
            var remaining = seconds
            while (isRestRunning && remaining > 0) {
                delay(1000L)
                remaining--
                _restTime.postValue(remaining)
                mainHandler.post {
                    updateOverlayRestTime(formatTime(remaining.toLong()))
                }
            }
            // 倒计时结束
            mainHandler.post {
                hideOverlayRestTime()
                vibrate(this@TrainingTimerService)
                // ✅ 恢复显示总时间
                val currentTime = _elapsedTime.value ?: 0L
                updateOverlayTime(formatTime(currentTime))
            }
            isRestRunning = false
        }
    }

    private fun stopRestTimer() {
        isRestRunning = false
        restJob?.cancel()
        restJob = null
        mainHandler.post {
            hideOverlayRestTime()
            val currentTime = _elapsedTime.value ?: 0L
            updateOverlayTime(formatTime(currentTime))
        }
    }

    // ============================================================
    // 悬浮窗管理
    // ============================================================
    // ✅ 当 App 进入后台时显示悬浮窗
    fun showOverlayIfNeeded() {
        if (isAppInForeground) {
            // App 在前台，不显示悬浮窗
            android.util.Log.d("TimerOverlay", "App 在前台，不显示悬浮窗")
            return
        }
        if (isOverlayShowing) return
        showOverlay()
    }

    // ✅ 当 App 回到前台时隐藏悬浮窗
    fun hideOverlayIfNeeded() {
        if (isAppInForeground) {
            hideOverlay()
        }
    }

    private fun showOverlay() {
        if (isOverlayShowing) return

        // ✅ 检查悬浮窗权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    "package:$packageName".toUri()
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
                return
            }
        }

        try {
            val inflater = LayoutInflater.from(this)
            overlayView = inflater.inflate(R.layout.layout_timer_overlay, null)

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                } else {
                    WindowManager.LayoutParams.TYPE_PHONE
                },
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 100
                y = 200
            }

            setupDrag(overlayView!!, params)
            windowManager.addView(overlayView, params)
            isOverlayShowing = true

            // ✅ 显示初始时间
            val currentTime = _elapsedTime.value ?: 0L
            updateOverlayTime(formatTime(currentTime))

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun hideOverlay() {
        if (!isOverlayShowing) return
        try {
            overlayView?.let {
                windowManager.removeView(it)
            }
        } catch (_: Exception) {
        }
        overlayView = null
        isOverlayShowing = false
    }

    // ✅ 真正停止训练和服务的方法
    fun stopTrainingCompletely() {
        stopTimer()
        stopRestTimer()
        hideOverlay() // 如果悬浮窗还在，也一并移除
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    // ✅ 悬浮窗拖动功能
    private fun setupDrag(view: View, params: WindowManager.LayoutParams) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isDragging = false

        view.setOnClickListener {
            android.util.Log.d("TimerOverlay", "悬浮窗被点击，回到 App")
            try {
                // ✅ 方式一：回到当前任务
                val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
                val tasks = activityManager.appTasks
                if (tasks.isNotEmpty()) {
                    tasks[0].moveToFront()
                    android.util.Log.d("TimerOverlay", "✅ 回到当前任务")
                    return@setOnClickListener
                }

                // ✅ 方式二：使用 Intent
                val intent = Intent(this, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                }
                startActivity(intent)
                android.util.Log.d("TimerOverlay", "✅ 回到 MainActivity")
            } catch (e: Exception) {
                android.util.Log.e("TimerOverlay", "回到 App 失败", e)
                // 最后尝试
                try {
                    val intent = packageManager.getLaunchIntentForPackage(packageName)
                    intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (intent != null) {
                        startActivity(intent)
                    }
                } catch (_: Exception) {
                    // 忽略
                }
            }
        }

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = event.rawX - initialTouchX
                    val deltaY = event.rawY - initialTouchY
                    if (kotlin.math.abs(deltaX) > 10 || kotlin.math.abs(deltaY) > 10) {
                        isDragging = true
                        params.x = initialX + deltaX.toInt()
                        params.y = initialY + deltaY.toInt()
                        windowManager.updateViewLayout(view, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        view.performClick()
                    }
                    true
                }
                else -> false
            }
        }

        view.isClickable = true
    }

    // ✅ 更新悬浮窗显示
    private fun updateOverlayTime(timeText: String) {
        overlayView?.findViewById<TextView>(R.id.tv_overlay_time)?.text = timeText
    }

    private fun updateOverlayRestTime(timeText: String) {
        overlayView?.findViewById<TextView>(R.id.tv_overlay_rest)?.apply {
            text = "⏱️ $timeText"
            visibility = View.VISIBLE
        }
    }

    private fun hideOverlayRestTime() {
        overlayView?.findViewById<TextView>(R.id.tv_overlay_rest)?.visibility = View.GONE
    }

    // ============================================================
    // 通知管理
    // ============================================================

    private fun createNotification(): Notification {
        val channelId = "training_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "训练计时器",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val currentTime = _elapsedTime.value ?: 0L
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("💪 训练进行中")
            .setContentText("总时间: ${formatTime(currentTime)}")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .build()
    }

    private fun updateNotification(contentText: String) {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    // ============================================================
    // 销毁
    // ============================================================

    override fun onDestroy() {
        super.onDestroy()
        timerJob?.cancel()
        restJob?.cancel()
        try {
            overlayView?.let { windowManager.removeView(it) }
        } catch (_: Exception) {
        }
        overlayView = null
        isOverlayShowing = false
        isTimerRunning = false
        isRestRunning = false
    }
}