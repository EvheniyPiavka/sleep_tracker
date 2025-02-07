package com.zeek1910.sleeptracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zeek1910.sleeptracker.R
import com.zeek1910.sleeptracker.Utils
import com.zeek1910.sleeptracker.db.SleepSegmentEventEntity

class SleepAdapter : RecyclerView.Adapter<SleepAdapter.SleepViewHolder>() {

    private val items = mutableListOf<SleepSegmentEventEntity>()

    fun setItems(newItems: List<SleepSegmentEventEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SleepViewHolder {
        return SleepViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_sleep_event, parent, false)
        )
    }

    override fun onBindViewHolder(holder: SleepViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class SleepViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val startTime = itemView.findViewById<TextView>(R.id.startTime)
        private val endTime = itemView.findViewById<TextView>(R.id.endTime)
        private val duration = itemView.findViewById<TextView>(R.id.duration)

        fun bind(sleepEvent: SleepSegmentEventEntity) {
            startTime.text = "Start: ${Utils.formatDateTime(sleepEvent.startTime)}"
            endTime.text = "End: ${Utils.formatDateTime(sleepEvent.endTime)}"
            duration.text =
                "Duration: ${Utils.getDuration(sleepEvent.startTime, sleepEvent.endTime)}"
        }
    }
}