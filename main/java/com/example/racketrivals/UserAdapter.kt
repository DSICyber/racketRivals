package com.example.racketrivals

import User
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter(private val items: List<User>) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textUserName: TextView = view.findViewById(R.id.textUserName)
        val textEloRating: TextView = view.findViewById(R.id.textUserElo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_ranking, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.textUserName.text = item.full_name.toString()
        holder.textEloRating.text = item.elo.toString()
    }

    override fun getItemCount() = items.size
}
