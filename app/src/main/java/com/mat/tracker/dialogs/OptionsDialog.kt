package com.mat.tracker.dialogs

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.mat.tracker.OptionsDataStore
import com.mat.tracker.R
import com.mat.tracker.databinding.DialogOptionsBinding
import com.mat.tracker.hasPermission
import kotlinx.coroutines.flow.collect
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
                with(optionsDataStore) {
                    lifecycleScope.launch {
                        saveAccuracyThreshold(Integer.parseInt(tietAccuracyThreshold.text.toString()))
                    }
                    lifecycleScope.launch {
                        saveAuthorName(tietAuthorName.text.toString())
                    }
                    lifecycleScope.launch {
                        saveRecordingDescription(tietRecordingDescription.text.toString())
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