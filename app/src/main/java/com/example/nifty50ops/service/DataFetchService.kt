package com.example.nifty50ops.service

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.nifty50ops.MainActivity
import com.example.nifty50ops.R
import com.example.nifty50ops.controller.OptionsController
import com.example.nifty50ops.controller.StockController
import com.example.nifty50ops.controller.MarketController
import com.example.nifty50ops.database.MarketDatabase
import com.example.nifty50ops.repository.MarketRepository
import com.example.nifty50ops.repository.OptionsRepository
import com.example.nifty50ops.repository.StockRepository
import com.example.nifty50ops.utils.readJwtToken
import kotlinx.coroutines.*
import java.util.*

class DataFetchService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var optionsController: OptionsController
    private lateinit var stockController: StockController
    private lateinit var marketController: MarketController
    private lateinit var wakeLock: PowerManager.WakeLock

    override fun onCreate() {
        super.onCreate()
        println("Service created")
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "DataFetchService::WakelockTag"
        )
        wakeLock.acquire()
        readJwtToken(this)

//        val db1 = StockDatabase.getDatabase(applicationContext)
//        val stockRepo = StockRepository(db1.stockDao())
//        val db2 = OptionsDatabase.getDatabase(applicationContext)
//        val optionRepo = OptionsRepository(db2.optionsDao())
//        val db3 = MarketDatabase.getDatabase(applicationContext)
//        val marketRepo = MarketRepository(db3.marketDao())

        val db = MarketDatabase.getDatabase(applicationContext)
        val optionRepo = OptionsRepository(db.marketDao())
        val stockRepo = StockRepository(db.marketDao())
        val marketRepo = MarketRepository(db.marketDao())

        optionsController = OptionsController(optionRepo)
        stockController = StockController(stockRepo)
        marketController = MarketController(marketRepo)

        startForegroundService()
        startFetchingLoop()
    }

    private fun startForegroundService() {
        val channelId = "DataFetchServiceChannel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Live Data Fetcher",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows market data fetch status"
                enableLights(true)
                enableVibration(true)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }

        // 🔗 Intent to open MainActivity
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("📊 Live Market Data")
            .setContentText("Fetching every minute...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)
    }

    private fun startFetchingLoop() {
        serviceScope.launch {
            while (true) {
                if (isMarketTime()) {
                    println("📊 Fetching data...")
                    try {
                        marketController.fetchMarketData(applicationContext)
                        stockController.fetchStockData(applicationContext)
                        optionsController.fetchOptionsData(applicationContext)
                    } catch (e: Exception) {
                        println("❌ Error fetching: ${e.localizedMessage}")
                    }
                } else {
                    println("⏰ Outside market hours, skipping fetch")
                }
                delay(60_000) // 1 minute
            }
        }
    }

    private fun isMarketTime(): Boolean {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        val isWeekday = dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY
        val isMarketHours = (hour > 9 || (hour == 9 && minute >= 15)) && (hour < 15 || (hour == 15 && minute <= 30))

//        return isWeekday && isMarketHours
        return true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("Service onStartCommand called")
        return START_STICKY // Important to restart service if killed
    }


    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        if (wakeLock.isHeld) wakeLock.release()
    }

}
