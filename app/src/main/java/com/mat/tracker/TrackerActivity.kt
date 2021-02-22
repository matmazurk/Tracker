package com.mat.tracker

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mat.tracker.databinding.ActivityTrackerBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class TrackerActivity : AppCompatActivity(), RemainingPointsDialog.Callbacks {

    private val viewModel: LocationsViewModel by viewModel()
    private val state: LiveData<State> by lazy {
        viewModel.state
    }
    private lateinit var binding: ActivityTrackerBinding
    private lateinit var permissionHandler: LocationPermissionHandler
    private lateinit var recyclerView: RecyclerView
    private lateinit var recordsAdapter: RecordsAdapter
    private val resolutionForResult = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
        run {
            if (it.resultCode == Activity.RESULT_OK) {
                checkPermissionsAndStartTracking()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackerBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        setContentView(binding.root)
        observeAppState()
        permissionHandler = LocationPermissionHandler(this)
        prepareRecyclerView()
        observeNewFileEvent()
        observeFiles()
        observeFileCreationFailure()
        observeMenuItemClick()
        observeAnyFileSelected()
    }

    override fun onResume() {
        super.onResume()
        setFabOnClickListener()
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
                    binding.isTracing = true
                }
                State.NOT_TRACING -> {
                    binding.fabTracking.setImageResource(R.drawable.ic_terrain_24)
                    binding.isTracing = false
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

    private fun observeFileCreationFailure() {
        viewModel.fileCreationFailure.observe(this) { failureEvent ->
            val failureText = failureEvent.getContentIfNotHandled()
            Toast.makeText(this, failureText, Toast.LENGTH_LONG).show()
        }
    }

    private fun observeFiles() {
        viewModel.startFileObserver()
        viewModel.files.observe(this) { files ->
            recordsAdapter.setFiles(files)
            binding.anyRecordedFiles = !viewModel.files.value.isNullOrEmpty()
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
                    lifecycleScope.launch {
                        if (viewModel.isAnyLocationRecorded()) {
                            val dialog = RemainingPointsDialog(this@TrackerActivity, false)
                            dialog.show(supportFragmentManager, "tracking stopped dialog")
                        }
                    }
                }
                State.NOT_TRACING -> {
                    lifecycleScope.launch {
                        if (viewModel.isAnyLocationRecorded()) {
                            val dialog = RemainingPointsDialog(this@TrackerActivity)
                            dialog.show(supportFragmentManager, "remaining points dialog")
                        } else {
                            checkSettingsAndRun {
                                run {
                                    checkPermissionsAndStartTracking()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun checkSettingsAndRun(block: () -> Unit) {
        val locationReq = LocationRequest.create().apply {
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        val builder = LocationSettingsRequest.Builder().apply {
            addLocationRequest(locationReq)
        }
        val result = LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
        result.addOnSuccessListener { response ->
            val states = response.locationSettingsStates
            if (states.isLocationPresent) {
                block()
            }
        }
        result.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    resolutionForResult.launch(intentSenderRequest)
                } catch(sendEx: IntentSender.SendIntentException) {

                }
            }
        }

    }

    private fun checkPermissionsAndStartTracking() {
        if (this::permissionHandler.isInitialized) {
            permissionHandler.checkFineLocationPermissionsAndRun {
                viewModel.startTracking()
            }
        }
    }

    private fun popFilenameDialog() {
        val input = EditText(this)
        val lp = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        input.layoutParams = lp
        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Filename")
            .setMessage("Provide file name:")
            .setView(input)
            .setPositiveButton("Ok") { dialog, _ ->
                if (input.text.isNotEmpty()) {
                    viewModel.saveLocationsToFile(input.text.toString())
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
                R.id.options -> {

                    true
                }
                R.id.share -> {
                    triggerShareDialog()
                    true
                }
                R.id.delete -> {
                    viewModel.removeSelectedFiles()
                    true
                }
                else -> false
            }
        }
    }

    private fun triggerShareDialog() {
        val files = arrayListOf<Uri>().apply {
            addAll(viewModel.selectedPositions)
        }
        val intent = Intent.createChooser(
            Intent().apply {
                action = Intent.ACTION_SEND_MULTIPLE
                type = "text/xml"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, files)
            },
            "Share"
        )
        val resInfoList: List<ResolveInfo> = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        for (resolveInfo in resInfoList) {
            val packageName = resolveInfo.activityInfo.packageName
            files.forEach {
                grantUriPermission(packageName, it, Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
        startActivity(intent)
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
