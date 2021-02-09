package com.mat.tracker

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.mat.tracker.databinding.DialogRemainingPointsBinding

class RemainingPointsDialog(
        private val handler: Callbacks,
        private val appendButtonVisible: Boolean = true
) : DialogFragment() {

    private lateinit var binding: DialogRemainingPointsBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogRemainingPointsBinding.inflate(inflater, container, false)
        if (!appendButtonVisible) {
            binding.btAppend.visibility = View.INVISIBLE
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.btSave.setOnClickListener {
            handler.onDialogSaveButtonSelected()
            dismiss()
        }
        binding.btAppend.setOnClickListener {
            handler.onDialogAppendButtonSelected()
            dismiss()
        }
        binding.btDiscard.setOnClickListener {
            handler.onDialogDiscardButtonSelected()
            dismiss()
        }
        binding.btCancel.setOnClickListener {
            dismiss()
        }
    }

    interface Callbacks {
        fun onDialogSaveButtonSelected()
        fun onDialogAppendButtonSelected()
        fun onDialogDiscardButtonSelected()
    }
}
