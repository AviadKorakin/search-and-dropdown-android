package com.aviadkorakin.search_and_dropdown

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class DropdownAdapter(
    private val onClick: (Map<String, Any>) -> Unit,
    private val displayField: String,
    private val itemTextColor: Int,
    private val itemBackgroundColor: Int,
    private val itemFontSizePx: Float,
    private val itemStrokeColor: Int,
    private val itemStrokeWidthPx: Int
) : ListAdapter<Map<String, Any>, DropdownAdapter.ViewHolder>(
    // inline the callback so we can reference `displayField`
    object : DiffUtil.ItemCallback<Map<String, Any>>() {
        override fun areItemsTheSame(
            old: Map<String, Any>,
            new: Map<String, Any>
        ): Boolean {
            return old[displayField] == new[displayField]
        }

        override fun areContentsTheSame(
            old: Map<String, Any>,
            new: Map<String, Any>
        ): Boolean {
            // quick size check
            if (old.size != new.size) return false
            // deep‐compare each entry
            for ((key, oldValue) in old) {
                val newValue = new[key]
                if (oldValue != newValue) return false
            }
            return true
        }
    }
) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val card: MaterialCardView = view.findViewById(R.id.dropdownCard)
        private val text: TextView            = view.findViewById(R.id.itemText)

        fun bind(item: Map<String, Any>) {
            // set stroke on the card
            card.strokeColor = itemStrokeColor
            card.strokeWidth = itemStrokeWidthPx

            // then populate your text
            val label = item[displayField]?.toString().orEmpty()
            text.text            = label
            text.setTextColor(itemTextColor)
            text.setBackgroundColor(itemBackgroundColor)
            text.setTextSize(TypedValue.COMPLEX_UNIT_PX, itemFontSizePx)

            // click
            itemView.setOnClickListener { onClick(item) }
        }

}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_dropdown, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}