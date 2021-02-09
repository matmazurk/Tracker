package com.mat.tracker

import android.Manifest
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mat.tracker.databinding.ActivityTrackerBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import java.util.concurrent.TimeUnit


class TrackerActivity : AppCompatActivity(), RemainingPointsDialog.Callbacks {

    private val viewModel: LocationsViewModel by viewModel()
    private val state: LiveData<State> by lazy {
        viewModel.state
    }
    private lateinit var binding: ActivityTrackerBinding
    private lateinit var permissionHandler: LocationPermissionHandler
    private var locations: List<LocationData> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeAppState()
        observeLocations()
        permissionHandler = LocationPermissionHandler(this)
        setFabOnClickListener()
    }

    override fun onPause() {
        super.onPause()
        if (!hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            viewModel.stopTracking()
        }
    }

    override fun onDialogSaveButtonSelected() {
        popFilenameDialog()
    }

    override fun onDialogAppendButtonSelected() {
        viewModel.startTracking()
    }

    override fun onDialogDiscardButtonSelected() {
        viewModel.clearLocations()
    }

    private fun observeAppState() {
        state.observe(this) { state ->
            when (state) {
                State.TRACING -> {
                    binding.fabTracking.setImageResource(R.drawable.ic_stop_circle_24)
                    binding.tvTrackingTime.visibility = View.VISIBLE
                    runTimerCoroutine()
                }
                State.NOT_TRACING -> {
                    binding.fabTracking.setImageResource(R.drawable.ic_terrain_24)
                    binding.tvTrackingTime.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun observeLocations() {
        viewModel.locations.observe(this) {
            locations = it
        }
    }

    private fun setFabOnClickListener() {
        binding.fabTracking.setOnClickListener {
            when (state.value) {
                State.TRACING -> {
                    viewModel.stopTracking()
                    if (locations.isNotEmpty()) {
                        val dialog = RemainingPointsDialog(this, false)
                        dialog.show(supportFragmentManager, "tracking stopped dialog")
                    }
                }
                State.NOT_TRACING -> {
                    if (locations.isNotEmpty()) {
                        val dialog = RemainingPointsDialog(this)
                        dialog.show(supportFragmentManager, "remaining points dialog")
                    } else {
                        permissionHandler.checkFineLocationPermissionsAndRun {
                            viewModel.startTracking()
                        }
                    }
                }
            }
        }
    }

    private fun runTimerCoroutine() = lifecycleScope.launch {
        val startingDate = Date()
        while (state.value == State.TRACING) {
            val difference = Date().time - startingDate.time
            val formattedDifference =
                    "${TimeUnit.MILLISECONDS.toHours(difference)}:" +
                    "${TimeUnit.MILLISECONDS.toMinutes(difference) % 60}:" +
                    String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(difference) % 60)
            binding.tvTrackingTime.text = formattedDifference
            delay(1000)
        }
    }

    private fun popFilenameDialog() {
        val input = EditText(this)
        val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        input.layoutParams = lp
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Filename")
            .setMessage("Provide file name:")
            .setView(input)
            .setPositiveButton("Ok") { dialog, _ ->
                if (input.text.isNotEmpty()) {
                    viewModel.saveLocationsToFile(input.text.toString(), locations)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Filename can't be empty!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
        dialog.show()
    }

    enum class State {
        TRACING,
        NOT_TRACING
    }

}
