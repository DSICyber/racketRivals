package com.example.racketrivals

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import kotlinx.coroutines.CoroutineScope


//this class takes the list and binds the data to each of the rows
class CourtListAdapter(private val clickListener: OnCourtClickListener,private val scope: CoroutineScope) : ListAdapter<RetrievedPotentialCourt, CourtViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourtViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_item, parent, false)
        return CourtViewHolder(view,scope)
    }

    override fun onBindViewHolder(holder: CourtViewHolder, position: Int) {
        val court = getItem(position)
        holder.bind(court,clickListener)
    }

    class DiffCallback : DiffUtil.ItemCallback<RetrievedPotentialCourt>() {
        override fun areItemsTheSame(oldItem: RetrievedPotentialCourt, newItem: RetrievedPotentialCourt): Boolean {
            return oldItem.possible_court_location_id == newItem.possible_court_location_id
        }

        override fun areContentsTheSame(oldItem: RetrievedPotentialCourt, newItem: RetrievedPotentialCourt): Boolean {
            return oldItem == newItem
        }
    }
}
