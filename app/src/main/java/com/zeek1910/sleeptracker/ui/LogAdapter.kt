package com.zeek1910.sleeptracker.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.zeek1910.sleeptracker.R
import com.zeek1910.sleeptracker.Utils
import com.zeek1910.sleeptracker.db.LogEventEntity

class LogAdapter : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    private val items = mutableListOf<LogEventEntity>()

    fun setItems(newItems: List<LogEventEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        return LogViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_log, parent, false)
        )
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int {
        return items.size
    }

    class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeStamp = itemView.findViewById<TextView>(R.id.timeStamp)
        private val message = itemView.findViewById<TextView>(R.id.message)

        fun bind(event: LogEventEntity) {
            timeStamp.text = "Time: ${Utils.formatDateTime(event.timestamp)}"
            message.text = "Message: ${event.message}"
        }
    }
}