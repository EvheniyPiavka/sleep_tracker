package com.zeek1910.sleeptracker.ui

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zeek1910.sleeptracker.Logger
import com.zeek1910.sleeptracker.R
import com.zeek1910.sleeptracker.db.AppDatabase
import com.zeek1910.sleeptracker.worker.ProcessSleepDataWorker
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class SleepRecordsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sleep_records)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sleepAdapter = SleepAdapter()
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.adapter = sleepAdapter
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        AppDatabase.getDatabase(this).sleepSegmentEventDao().getAllEventsFlow()
            .onEach { sleepEvents -> sleepAdapter.setItems(sleepEvents) }
            .launchIn(lifecycleScope)


    }

    override fun onResume() {
        super.onResume()
        ProcessSleepDataWorker.start(this)
            .onEach { Logger.log(it.toString()) }
            .launchIn(lifecycleScope)
    }
}