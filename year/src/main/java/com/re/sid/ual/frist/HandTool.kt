package com.re.sid.ual.frist

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import bef.ice.TelMe
import com.re.sid.ual.ben.GetZenbox
import com.re.sid.ual.up.DaoMe
import com.re.sid.ual.up.befor.BeForTool
import com.re.sid.ual.song.FirstStartSerivce
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

object HandTool {

    private const val LOOP_WORK_NAME = "mmcdc"
    private const val PERIODIC_WORK_NAME = "ppoec"

    var ssJob: Job? = null
    
    fun subscribe() {
        InitBus.collect { p ->
            val ctx = InitBus.ctx() ?: return@collect
            when (p) {
                InitBus.P_SERVICE -> loopStart(ctx)
                InitBus.P_FIREBASE -> FirstStartSerivce.firebaseShowFun()
                InitBus.P_REF -> BeForTool().launchRefData()
                InitBus.P_WORK -> startKeepAliveWork(ctx)
                InitBus.P_SESSION -> seecan()
            }
        }
    }

    class LoopKeepAliveWorker(
        context: Context,
        params: WorkerParameters
    ) : Worker(context, params) {
        
        override fun doWork(): Result {
            try {
                Thread.sleep(1000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return Result.success()
        }
    }


    class PeriodicKeepAliveWorker(
        context: Context,
        params: WorkerParameters
    ) : Worker(context, params) {
        
        override fun doWork(): Result {
            try {
                Thread.sleep(1000)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            
            return Result.success()
        }
    }


    fun startKeepAliveWork(context: Context) {
        startLoopWork(context)
        startPeriodicWork(context)
    }


    private fun startLoopWork(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<LoopKeepAliveWorker>()
            .setConstraints(
                Constraints.Builder()
                    .build()
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                LOOP_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                workRequest
            )
    }


    private fun startPeriodicWork(context: Context) {
        val periodicWorkRequest = PeriodicWorkRequestBuilder<PeriodicKeepAliveWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .build()
            )
            .build()

        WorkManager.getInstance(context)
            .enqueueUniquePeriodicWork(
                PERIODIC_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicWorkRequest
            )
    }



    fun seecan() {
        ssJob?.cancel()
        ssJob= CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                DaoMe.upPoint(false, "session")
                delay(1000*60*15)
            }
        }
    }

    fun loopStart(context: Context) {
        if (GetZenbox.soCanFell) return
        try {
            DataTool.showLog("startForegroundService loopStart")

            ContextCompat.startForegroundService(
                context,
                Intent(context, TelMe::class.java)
            )
        } catch (e: Exception) {
            DataTool.showLog("startForegroundService error: ${e.message}")
        }
    }
}