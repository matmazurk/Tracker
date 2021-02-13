package com.mat.tracker

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textview.MaterialTextView
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RecordsAdapter(
    private val context: Context,
) : RecyclerView.Adapter<RecordsAdapter.ViewHolder>(), KoinComponent {
    private var files: List<String> = listOf()
    private val viewModel: LocationsViewModel by inject()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater
            .from(parent.context)
                .inflate(R.layout.recyclerview_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.fileNameTextView.text = files[position]
        holder.itemView.isSelected = viewModel.rvSelectedPositions.contains(position)
        val typedValue = TypedValue()
        if (viewModel.rvSelectedPositions.contains(position)) {
            context.theme.resolveAttribute(R.attr.colorPrimary, typedValue, true)
        } else {
            context.theme.resolveAttribute(R.attr.colorOnPrimary, typedValue, true)
        }
        val backgroundColor = typedValue.data
        val background = holder.layout.background as GradientDrawable
        background.setColor(backgroundColor)

        holder.layout.setOnClickListener {
            if (viewModel.rvSelectedPositions.contains(position)) {
                viewModel.rvSelectedPositions.remove(position)
            } else {
                viewModel.rvSelectedPositions.add(position)
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