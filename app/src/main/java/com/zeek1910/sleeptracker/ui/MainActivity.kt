package com.zeek1910.sleeptracker.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zeek1910.sleeptracker.R
import com.zeek1910.sleeptracker.db.AppDatabase
import com.zeek1910.sleeptracker.worker.StartSleepTrackingWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private val requestActivityRecognitionPermission =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                rescheduleSleepTracking()
            } else {
                Toast.makeText(
                    this,
                    "Permission denied. App can't work with out this permission",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<Button>(R.id.showSleepRecordsButton).setOnClickListener {
            startActivity(Intent(this, SleepRecordsActivity::class.java))
        }

        findViewById<Button>(R.id.showRawDataButton).setOnClickListener {
            startActivity(Intent(this, RawDataActivity::class.java))
        }

        val logAdapter = LogAdapter()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = logAdapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        AppDatabase.getDatabase(this).logEventDao().getEventsFlow()
            .onEach { events -> logAdapter.setItems(events) }
            .launchIn(lifecycleScope)

        findViewById<Button>(R.id.wipeDataButton).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Wipe data")
                .setMessage("Are you sure you want to wipe all data?")
                .setPositiveButton("Yes") { _, _ ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        AppDatabase.getDatabase(this@MainActivity).clearAllTables()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@MainActivity, "Data wiped", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }
                .setNegativeButton("No") { _, _ -> }
                .create()
                .show()
        }

        rescheduleSleepTracking()
        checkBatteryOptimization()
    }

    private fun rescheduleSleepTracking() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestActivityRecognitionPermission.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            return
        }
        StartSleepTrackingWorker.scheduleRestartSleepTracking(this)
    }

    @SuppressLint("BatteryLife")
    private fun checkBatteryOptimization() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            val intentSettings = Intent(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            with(intentSettings) {
                data = Uri.fromParts("package", packageName, null)
                addCategory(Intent.CATEGORY_DEFAULT)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            }
            startActivity(intentSettings)
        }
    }
}