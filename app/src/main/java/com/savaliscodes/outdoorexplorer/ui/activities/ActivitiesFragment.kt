package com.savaliscodes.outdoorexplorer.ui.activities

import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.savaliscodes.outdoorexplorer.GeofenceBroadcastReceiver
import com.savaliscodes.outdoorexplorer.R
import com.savaliscodes.outdoorexplorer.data.GeofencingChanges
import com.savaliscodes.outdoorexplorer.ui.map.MapFragment
import kotlinx.android.synthetic.main.fragment_activities.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class ActivitiesFragment : Fragment(), ActivitiesAdapter.OnClickListener {
    private lateinit var activitiesViewModel: ActivitiesViewModel
    private lateinit var geofencingClient: GeofencingClient
    private var geofencingChanges : GeofencingChanges? = null

    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(),
        GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(requireContext(), 0,
        intent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        inflater.inflate(R.layout.fragment_activities, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        activitiesViewModel = ViewModelProvider(this)
            .get(ActivitiesViewModel::class.java)

        geofencingClient = LocationServices.getGeofencingClient(requireActivity())

        val adapter = ActivitiesAdapter(this)
        listActivities.adapter = adapter

        activitiesViewModel.allActivities.observe(viewLifecycleOwner, Observer {
            adapter.setActivities(it)
            if (it.any { a -> a.geofenceEnabled } && checkPermissions().isEmpty()) {
                Snackbar.make(
                    requireView(),
                    getString(R.string.activities_background_reminder),
                    Snackbar.LENGTH_LONG
                )
                    .setAction(R.string.ok) {}
                    .show()
            }
        })
    }

    override fun onClick(id: Int, title: String) {
        val action = ActivitiesFragmentDirections
            .actionNavigationActivitiesToNavigationLocations()
        action.activityId = id
        action.title = "Locations with $title"
        val navController = Navigation.findNavController(requireView())
        navController.navigate(action)
    }

    override fun onGeofenceClick(id: Int) {
        geofencingChanges = activitiesViewModel.toggleGeofencing(id)
        handleGeofencing()
    }

    @SuppressLint("InlinedApi", "MissingPermission")
    @AfterPermissionGranted(RC_LOCATION)
    private fun handleGeofencing() {
        val neededPermissions:List<String> = checkPermissions()
        if(neededPermissions.contains(ACCESS_FINE_LOCATION)){
            requestPermission(
                R.string.activities_location_snackbar,
                R.string.locations_rationale,
                ACCESS_FINE_LOCATION)
        }else if(neededPermissions.contains(ACCESS_BACKGROUND_LOCATION)){
            requestPermission(
                R.string.activities_location_snackbar,
                R.string.locations_rationale,
                ACCESS_BACKGROUND_LOCATION)
        }else if(geofencingChanges != null){
            if(geofencingChanges!!.idsToRemove.isNotEmpty()){
                geofencingClient.removeGeofences(geofencingChanges!!.idsToRemove)
            }
            if(geofencingChanges!!.locationsToAdd.isNotEmpty()){
//                geofencingClient.addGeofences(geofencingChanges!!.locationsToAdd)
                val geofencingRequest: GeofencingRequest? =
                        GeofencingRequest.Builder().apply{
                            addGeofences(geofencingChanges!!.locationsToAdd)
                            setInitialTrigger(Geofence.GEOFENCE_TRANSITION_ENTER)
                }.build()

                geofencingClient.addGeofences(
                    geofencingRequest,
                    pendingIntent
                )
            }
        }
    }

    private fun requestPermission(@StringRes snackbarMessage: Int,
                    @StringRes rationalMessage: Int,
                        permission: String) {
        Snackbar.make(
            requireView(),
            getString(snackbarMessage),
            Snackbar.LENGTH_INDEFINITE
        ).setAction(R.string.ok){
            EasyPermissions.requestPermissions(
                this,
                getString(rationalMessage),
                RC_LOCATION,
                permission
            )
        }.show()
    }

    private fun checkPermissions(): List<String> {
        val permissionsNeeded = ArrayList<String>()
        if(!EasyPermissions.hasPermissions(requireContext(), ACCESS_FINE_LOCATION)){
            permissionsNeeded.add(ACCESS_FINE_LOCATION)
        }
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                !EasyPermissions.hasPermissions(requireContext(), ACCESS_BACKGROUND_LOCATION)){
            permissionsNeeded.add(ACCESS_BACKGROUND_LOCATION)
        }
        return permissionsNeeded
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(
            requestCode, permissions,
            grantResults, this
        )
    }
    companion object{
        const val RC_LOCATION = 1
    }
}
