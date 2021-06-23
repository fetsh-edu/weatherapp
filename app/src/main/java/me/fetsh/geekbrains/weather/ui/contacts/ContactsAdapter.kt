package me.fetsh.geekbrains.weather.ui.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import me.fetsh.geekbrains.weather.databinding.HistoryFragmentRecyclerItemBinding

class ContactsAdapter : RecyclerView.Adapter<ContactsAdapter.RecyclerItemViewHolder>() {

    private var data: List<String> = arrayListOf()

    fun setData(data: List<String>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerItemViewHolder {
        val itemBinding = HistoryFragmentRecyclerItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return RecyclerItemViewHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: RecyclerItemViewHolder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int {
        return data.size
    }

    inner class RecyclerItemViewHolder(private val itemBinding: HistoryFragmentRecyclerItemBinding) : RecyclerView.ViewHolder(itemBinding.root) {
        fun bind(data: String) {
            if (layoutPosition != RecyclerView.NO_POSITION) {
                itemBinding.recyclerViewItem.text = data
            }
        }
    }
}