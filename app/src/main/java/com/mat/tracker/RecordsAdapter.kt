package com.mat.tracker

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import org.koin.androidx.viewmodel.compat.SharedViewModelCompat.sharedViewModel
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RecordsAdapter(
    private val context: Context,
    private val viewModel: LocationsViewModel
) : RecyclerView.Adapter<RecordsAdapter.ViewHolder>(), KoinComponent {

    private var files: List<String> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
                .inflate(R.layout.recyclerview_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val selected = viewModel.selectedPositions.contains(position)
        holder.fileNameTextView.text = files[position]
        holder.itemView.isSelected = selected
        val typedValue = TypedValue()
        if (selected) {
            context.theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
        } else {
            context.theme.resolveAttribute(R.attr.colorOnPrimary, typedValue, true)
        }
        val backgroundColor = typedValue.data
        val background = holder.layout.background as GradientDrawable
        background.setColor(backgroundColor)

        holder.layout.setOnClickListener {
            if (selected) {
                viewModel.removeSelectedFile(position)
            } else {
                viewModel.addSelectedFile(position)
            }
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = files.size

    fun setFiles(files: List<String>) {
        this.files = files
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout: ConstraintLayout = view.findViewById(R.id.rv_item_layout)
        val fileNameTextView: MaterialTextView = view.findViewById(R.id.tv_filename)
    }
}