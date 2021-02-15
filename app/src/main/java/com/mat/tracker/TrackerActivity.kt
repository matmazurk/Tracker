package com.mat.tracker

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mat.tracker.databinding.ActivityTrackerBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class TrackerActivity : AppCompatActivity(), RemainingPointsDialog.Callbacks {

    private val viewModel: LocationsViewModel by viewModel()
    private val state: LiveData<State> by lazy {
        viewModel.state
    }
    private lateinit var binding: ActivityTrackerBinding
    private lateinit var permissionHandler: LocationPermissionHandler
    private lateinit var recyclerView: RecyclerView
    private var locations: List<LocationData> = listOf()
    private lateinit var recordsAdapter: RecordsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        observeAppState()
        observeLocations()
        permissionHandler = LocationPermissionHandler(this)
        prepareRecyclerView()
        setFabOnClickListener()
        observeNewFileEvent()
        observeFiles()
        observeTimer()
        observeMenuItemClick()
        observeAnyFileSelected()
    }

    override fun onPause() {
        super.onPause()
        if (!hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
            viewModel.stopTracking()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopFileObserver()
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

                }
                State.NOT_TRACING -> {
                    binding.fabTracking.setImageResource(R.drawable.ic_terrain_24)
                }
            }
        }
    }

    private fun observeNewFileEvent() {
        viewModel.newFileEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { filename ->
                viewModel.clearSelections()
                Toast.makeText(this, "$filename created!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeFiles() {
        viewModel.startFileObserver()
        viewModel.files.observe(this) { files ->
            recordsAdapter.setFiles(files)
            if (viewModel.files.value.isNullOrEmpty()) {
                binding.tvNoFiles.visibility = View.VISIBLE
            } else {
                binding.tvNoFiles.visibility = View.INVISIBLE
            }
        }
    }

    private fun observeLocations() {
        viewModel.locations.observe(this) {
            locations = it
        }
    }

    private fun prepareRecyclerView() {
        recordsAdapter = RecordsAdapter(this, viewModel)
        recyclerView = binding.rvRecords
        recyclerView.adapter = recordsAdapter
        recyclerView.layoutManager = GridLayoutManager(this, 3)
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

    private fun observeTimer() {
        viewModel.passedTimeString.observe(this) { formattedTime ->
            if (formattedTime != null) {
                binding.tvTrackingTime.apply {
                    visibility = View.VISIBLE
                    text = formattedTime
                }
            } else {
                binding.tvTrackingTime.visibility = View.INVISIBLE
            }
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

    private fun observeMenuItemClick() {
        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.background_enhance -> {
                    permissionHandler.checkBackgroundLocationAndRun {}
                    true
                }
                R.id.search -> {

                    true
                }
                R.id.options -> {

                    true
                }
                R.id.share -> {

                    true
                }
                else -> false
            }
        }
    }

    private fun observeAnyFileSelected() {
        viewModel.isAnyFileSelected.observe(this) { isAnySelected ->
            binding.bottomAppBar.menu.getItem(2).isVisible = isAnySelected
            binding.bottomAppBar.menu.getItem(3).isVisible = isAnySelected
        }
    }

    enum class State {
        TRACING,
        NOT_TRACING
    }

}
