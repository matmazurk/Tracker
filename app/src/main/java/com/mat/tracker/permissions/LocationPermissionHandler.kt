package com.mat.tracker.permissions

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.karumi.dexter.Dexter
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.single.PermissionListener
import com.mat.tracker.hasPermission
import org.koin.core.component.KoinApiExtension
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

@KoinApiExtension
class LocationPermissionHandler(
    private val activity: AppCompatActivity
) : KoinComponent {
    private val dataStore: PermissionsDataStore by inject()

    private val backgroundLocationPermissionRequest =
        activity.registerForActivityResult(RequestPermission()) { granted ->
            if (granted) {
                backgroundPermissionGrantedInvokeBlock.invoke()
            } else {
                dataStore.incrementCounterAndFillLiveData()
            }
        }

    private val navigateToSettingsPageAndCheckBackgroundLocationPermission =
        activity.registerForActivityResult(StartActivityForResult()) {
            if (activity.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                backgroundPermissionGrantedInvokeBlock.invoke()
            }
        }

    private val settingsPageIntent: Intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)

    init {
        val uri: Uri = Uri.fromParts("package", activity.packageName, null)
        settingsPageIntent.data = uri
        dataStore.denials.observe(activity) { denials ->
            if (denials > 2) {
                //rationale
                navigateToSettingsPageAndCheckBackgroundLocationPermission.launch(settingsPageIntent)
            }
        }
    }

    fun checkFineLocationPermissionsAndRun(block: () -> Unit)
    {
        val listener = object : PermissionListener {

            override fun onPermissionGranted(report: PermissionGrantedResponse?) {
                block()
            }

            override fun onPermissionDenied(report: PermissionDeniedResponse?) {
                report?.let {
                    if (report.isPermanentlyDenied) {
                        showLocationPermissionsRationaleDialog(
                                activity, {
                                    activity.startActivity(settingsPageIntent)
                                },
                                {})
                    }
                }
            }

            override fun onPermissionRationaleShouldBeShown(
                request: PermissionRequest?,
                token: PermissionToken?
            ) {
                showLocationPermissionsRationaleDialog(
                        activity,
                        {
                            token?.continuePermissionRequest()
                        },
                        {
                            token?.cancelPermissionRequest()
                        })
            }
        }
        Dexter.withContext(activity)
            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            .withListener(listener)
            .check()
    }

    fun checkBackgroundLocationAndRun(block: () -> Unit) {
        backgroundPermissionGrantedInvokeBlock = block
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (activity.hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                backgroundPermissionGrantedInvokeBlock.invoke()
            } else {
                showBackgroundLocationPermissionsRationaleDialog(activity) {
                    backgroundLocationPermissionRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                }
            }
        } else {
            backgroundPermissionGrantedInvokeBlock.invoke()
        }
    }

    private fun showLocationPermissionsRationaleDialog(
        context: Context,
        acceptBlock: () -> Unit,
        rejectBlock: () -> Unit
    )
    {
        AlertDialog.Builder(context)
            .setTitle("Permission needed")
            .setMessage("Application needs location permissions to be able to track user position")
            .setPositiveButton("Ok") { dialog, _ ->
                acceptBlock()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                rejectBlock()
                dialog.dismiss()
            }
            .show()
    }

    private fun showBackgroundLocationPermissionsRationaleDialog(
        context: Context,
        block: () -> Unit
    )
    {
        val messageBuilder = StringBuilder()
        messageBuilder.append(
                "Background location permission is needed to allow you to use other apps while recording route."
        )
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            messageBuilder.append("To continue with application select ${activity.packageManager.backgroundPermissionOptionLabel} in permissions page")
        }
        AlertDialog.Builder(context)
            .setTitle("Permission needed")
            .setMessage(messageBuilder.toString())
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.dismiss()
                block()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    companion object {
        private lateinit var backgroundPermissionGrantedInvokeBlock: () -> Unit
    }
}