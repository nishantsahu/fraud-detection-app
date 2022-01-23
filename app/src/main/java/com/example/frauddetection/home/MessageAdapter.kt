package com.example.frauddetection.home

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.frauddetection.databinding.MessageItemBinding
import com.example.frauddetection.home.model.MessageModel
import com.example.frauddetection.utils.FraudDetection

class MessageAdapter(val context: Context, val list: List<MessageModel>): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    class MessageViewHolder(val binding: MessageItemBinding):
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val holder = MessageItemBinding.inflate(LayoutInflater.from(context), parent, false)
        return MessageViewHolder(holder)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val data = list.get(position)
        holder.binding.title = data.title
        holder.binding.content = data.body
        holder.binding.status = data.status
    }

    override fun getItemCount(): Int {
        return list.size
    }
}