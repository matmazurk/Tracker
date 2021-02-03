package com.mat.tracker

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.mat.tracker.databinding.ActivityTrackerBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*
import java.util.concurrent.TimeUnit

class TrackerActivity : AppCompatActivity() {

    private val viewModel: LocationsViewModel by viewModel()
    private val state: LiveData<State> by lazy {
        viewModel.state
    }
    private lateinit var binding: ActivityTrackerBinding
    private lateinit var permissionHandler: LocationPermissionHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeAppState()
        permissionHandler = LocationPermissionHandler(this)
        setFabOnClickListener()
    }

    override fun onPause() {
        super.onPause()
        if (!hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            viewModel.stopTracking()
        }
    }

    enum class State {
        TRACING,
        NOT_TRACING
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

    private fun setFabOnClickListener() {
        binding.fabTracking.setOnClickListener {
            when (state.value) {
                State.TRACING -> {
                    viewModel.stopTracking()
                }
                State.NOT_TRACING -> {
                    permissionHandler.checkFineLocationPermissionsAndRun {
                        viewModel.startTracking()
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
                    "${TimeUnit.MILLISECONDS.toSeconds(difference) % 60}"
            binding.tvTrackingTime.text = formattedDifference
            delay(1000)
        }
    }

}