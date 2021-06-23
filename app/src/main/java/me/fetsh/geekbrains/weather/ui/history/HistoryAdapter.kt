package me.fetsh.geekbrains.weather.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import me.fetsh.geekbrains.weather.databinding.HistoryFragmentRecyclerItemBinding
import me.fetsh.geekbrains.weather.room.HistoryEntity

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.RecyclerItemViewHolder>() {

    private var data: List<HistoryEntity> = arrayListOf()

    fun setData(data: List<HistoryEntity>) {
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

        fun bind(data: HistoryEntity) {
            if (layoutPosition != RecyclerView.NO_POSITION) {
                itemBinding.recyclerViewItem.text =
                    String.format("%s %d %s", data.city, data.temperature, data.condition)
                itemView.setOnClickListener {
                    Toast.makeText(
                        itemView.context,
                        "on click: ${data.city}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}