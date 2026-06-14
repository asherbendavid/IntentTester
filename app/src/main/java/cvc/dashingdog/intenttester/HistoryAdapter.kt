package cvc.dashingdog.intenttester

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter(
    private val onClick: (SavedIntent) -> Unit,
    private val onLongClick: (SavedIntent) -> Unit
) : ListAdapter<SavedIntent, HistoryAdapter.ViewHolder>(DiffCallback) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        private val tvSubtitle: TextView = view.findViewById(R.id.tvSubtitle)

        fun bind(item: SavedIntent) {
            tvTitle.text = item.displayName()
            tvSubtitle.text = buildSubtitle(item)
            itemView.setOnClickListener { onClick(item) }
            itemView.setOnLongClickListener {
                onLongClick(item)
                true
            }
        }

        private fun buildSubtitle(item: SavedIntent): String {
            val type = formatDispatchType(item.dispatchType)
            val extra = when {
                item.label.isNotBlank() -> item.action.ifBlank { item.component }
                item.action.isNotBlank() -> item.component
                else -> ""
            }
            return if (extra.isNotBlank()) "$type  ·  $extra" else type
        }

        private fun formatDispatchType(type: DispatchType): String =
            type.name
                .replace("_", " ")
                .lowercase()
                .replaceFirstChar { it.uppercase() }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SavedIntent>() {
        override fun areItemsTheSame(old: SavedIntent, new: SavedIntent) = old.id == new.id
        override fun areContentsTheSame(old: SavedIntent, new: SavedIntent) = old == new
    }
}