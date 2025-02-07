package com.zeek1910.sleeptracker.ui

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
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
import com.zeek1910.sleeptracker.AppSettings
import com.zeek1910.sleeptracker.R
import com.zeek1910.sleeptracker.db.AppDatabase
import com.zeek1910.sleeptracker.receiver.SleepReceiver
import com.zeek1910.sleeptracker.worker.StartSleepTrackingWorker
import com.zeek1910.sleeptracker.worker.StopSleepTrackerWorker
import kotlinx.coroutines.Dispatchers
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

        val appSettings = AppSettings.getInstance(this)

        findViewById<Button>(R.id.showSleepRecordsButton).setOnClickListener {
            startActivity(Intent(this, SleepRecordsActivity::class.java))
        }

        findViewById<Button>(R.id.showRawDataButton).setOnClickListener {
            startActivity(Intent(this, RawDataActivity::class.java))
        }

        findViewById<Button>(R.id.showLogButton).setOnClickListener {
            startActivity(Intent(this, LogActivity::class.java))
        }

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

        val awakeTimeButton = findViewById<Button>(R.id.awakeTime)
        val sleepTimeButton = findViewById<Button>(R.id.sleepTime)
        updateButtonTime(awakeTimeButton, appSettings.awakeTime)
        updateButtonTime(sleepTimeButton, appSettings.sleepTime)

        awakeTimeButton.setOnClickListener {
            val awakeTime = appSettings.awakeTime
            val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
                val newAwakeTime = Pair(hourOfDay, minute)
                appSettings.awakeTime = newAwakeTime
                rescheduleStopSleepTracking()
                updateButtonTime(awakeTimeButton, newAwakeTime)
            }, awakeTime.first, awakeTime.second, true)
            timePickerDialog.show()
        }

        sleepTimeButton.setOnClickListener {
            val sleepTime = appSettings.sleepTime
            val timePickerDialog = TimePickerDialog(this, { _, hourOfDay, minute ->
                val newSleepTime = Pair(hourOfDay, minute)
                appSettings.sleepTime = newSleepTime
                rescheduleStartSleepTracking()
                updateButtonTime(sleepTimeButton, appSettings.sleepTime)
            }, sleepTime.first, sleepTime.second, true)
            timePickerDialog.show()
        }

        rescheduleSleepTracking()
    }

    override fun onResume() {
        super.onResume()
        SleepReceiver.checkAndValidateLastSleep()
    }

    private fun updateButtonTime(button: Button, time: Pair<Int, Int>) {
        val h = time.first.toString().padStart(2, '0')
        val m = time.second.toString().padStart(2, '0')
        button.text = "$h:$m"
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
        rescheduleStartSleepTracking()
        rescheduleStopSleepTracking()
    }

    private fun rescheduleStartSleepTracking() {
        val sleepTime = AppSettings.getInstance(this).sleepTime
        StartSleepTrackingWorker.scheduleStartSleepTracking(this, sleepTime.first, sleepTime.second)
    }

    private fun rescheduleStopSleepTracking() {
        val awakeTime = AppSettings.getInstance(this).awakeTime
        StopSleepTrackerWorker.scheduleStopSleepTracking(this, awakeTime.first, awakeTime.second)
    }
}