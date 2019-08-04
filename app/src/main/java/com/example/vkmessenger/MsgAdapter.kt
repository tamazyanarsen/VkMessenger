package com.example.vkmessenger

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.msglayout.view.*

class MsgAdapter(private val items: ArrayList<String>, private val context: Context) :
    RecyclerView.Adapter<MsgAdapter.MsgHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MsgHolder {
        return MsgHolder(LayoutInflater.from(context).inflate(R.layout.msglayout, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MsgHolder, position: Int) {
        holder.msgText.text = items[position]
    }

    class MsgHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {
        private val view = v
        private val item: String? = null
        val msgText: TextView = v.msgTextView
        override fun onClick(p0: View?) {
            println("was click $p0")
        }
    }
}