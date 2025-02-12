package com.zeek1910.sleeptracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zeek1910.sleeptracker.R
import com.zeek1910.sleeptracker.Utils
import com.zeek1910.sleeptracker.db.SleepClassifyEventEntity

class RawDataAdapter : RecyclerView.Adapter<RawDataAdapter.RawDataViewHolder>() {

    private val items = mutableListOf<SleepClassifyEventEntity>()

    fun setItems(newItems: List<SleepClassifyEventEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RawDataViewHolder {
        return RawDataViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_raw_data, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RawDataViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class RawDataViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeStamp = itemView.findViewById<TextView>(R.id.timeStamp)
        private val confidence = itemView.findViewById<TextView>(R.id.confidence)

        fun bind(event: SleepClassifyEventEntity) {
            if (event.isProcessed) {
                itemView.setBackgroundColor(itemView.context.getColor(R.color.processed_item_bg))
            } else {
                itemView.setBackgroundColor(itemView.context.getColor(R.color.white))
            }
            timeStamp.text = "Time: ${Utils.formatDateTime(event.timestampMillis)}"
            confidence.text = "Confidence: ${event.confidence}"
        }
    }
}