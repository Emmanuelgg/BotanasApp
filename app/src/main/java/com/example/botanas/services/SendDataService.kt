package com.example.botanas.services

import android.app.Application
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.example.botanas.api.SalesApi
import com.example.botanas.db.MySqlHelper
import org.jetbrains.anko.db.select
import java.util.*

class SendDataService : Service() {
    private val mBinder = LocalBinder()
    private lateinit var handler: Handler
    private lateinit var mySqlHelper: MySqlHelper
    private lateinit var salesApi: SalesApi

    inner class LocalBinder : Binder() {
        val service: SendDataService
            get() = this@SendDataService
    }

    override fun onBind(intent: Intent): IBinder? {
        Log.i("ServiceRun", "Service bind")
        return mBinder
    }

    override fun onDestroy() {
        super.onDestroy()
        checkSalesToSync()
    }


    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        handler = Handler()
        mySqlHelper =  MySqlHelper(applicationContext)
        salesApi = SalesApi(applicationContext)
        handler.post {
            checkSalesToSync()
        }
        return START_STICKY
    }

    private fun checkSalesToSync() {
        val timer = Timer()
        var notificationSent = 0
        val hourlyTask = object : TimerTask() {
            override fun run() {
                try {
                    if (notificationSent > 0) {
                        val network = Network(applicationContext)
                        if (network.isConnected()) {
                            mySqlHelper.use {
                                select("requisition")
                                    .exec {
                                        if (this.count > 0)
                                            salesApi.requestPostSyncSales()
                                    }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                notificationSent++
            }
        }

        // schedule the task to run starting now and then every hour...
        timer.schedule(hourlyTask, 0L, 1000*1*60)
    }

}