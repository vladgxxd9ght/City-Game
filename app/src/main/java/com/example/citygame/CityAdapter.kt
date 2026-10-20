package com.example.citygame

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CityAdapter(private val messagesList: List<String>) : RecyclerView.Adapter<CityAdapter.CityViewHolder>() {

    companion object {
        private const val TYPE_BOT = 0
        private const val TYPE_USER = 1
    }

    class CityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewItem: TextView = itemView.findViewById(R.id.textViewListItem)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position > 0 && position % 2 != 0) {
            TYPE_USER
        } else {
            TYPE_BOT
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityViewHolder {
        val layoutId = if (viewType == TYPE_USER) {
            R.layout.list_item_user
        } else {
            R.layout.list_item
        }
        val view = LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
        return CityViewHolder(view)
    }

    override fun onBindViewHolder(holder: CityViewHolder, position: Int) {
        holder.textViewItem.text = messagesList[position]
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }
}