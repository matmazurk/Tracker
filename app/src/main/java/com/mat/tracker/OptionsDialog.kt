package com.mat.tracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.mat.tracker.databinding.DialogOptionsBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.core.component.KoinComponent

class OptionsDialog : DialogFragment(), KoinComponent {

    private var binding: DialogOptionsBinding? = null
    private val optionsDataStore: OptionsDataStore by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DialogOptionsBinding.inflate(inflater, container, false)
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding?.apply {
            lifecycleScope.launch {
                with(optionsDataStore) {
                    authorNameFlow.collect { name ->
                        tietAuthorName.setText(name)
                    }
                    recordingDescriptionFlow.collect { description ->
                        tietRecordingDescription.setText(description)
                    }
                    accuracyThresholdFlow.collect { accuracy ->
                        tietAccuracyThreshold.setText(accuracy.toString())
                    }
                }
            }
            tvBackgroundEnhanced.text =
                if (context?.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) == true)
                    getString(R.string.background_enhanced, getString(R.string.enabled))
                else
                    getString(R.string.background_enhanced, getString(R.string.disabled))
            btSave.setOnClickListener {
                lifecycleScope.launch {
                    with(optionsDataStore) {
                        saveAuthorName(tietAuthorName.text.toString())
                        saveRecordingDescription(tietRecordingDescription.text.toString())
                        saveAccuracyThreshold(tietAccuracyThreshold.text.toString().toInt())
                    }
                }
                dismiss()
            }
            btCancel.setOnClickListener {
                dismiss()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}