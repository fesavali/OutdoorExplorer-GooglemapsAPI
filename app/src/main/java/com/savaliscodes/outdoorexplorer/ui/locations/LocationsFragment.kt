package com.savaliscodes.outdoorexplorer.ui.locations

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.style.EasyEditSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.savaliscodes.outdoorexplorer.R
import com.savaliscodes.outdoorexplorer.data.Location
import kotlinx.android.synthetic.main.fragment_locations.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

class LocationsFragment : Fragment(), LocationsAdapter.OnClickListener {
    private lateinit var adapter: LocationsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View =
        inflater.inflate(R.layout.fragment_locations, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val locationsViewModel = ViewModelProvider(this)
            .get(LocationsViewModel::class.java)

        adapter = LocationsAdapter(this)
        listLocations.adapter = adapter

        arguments?.let { bundle ->
            val passedArguments = LocationsFragmentArgs.fromBundle(bundle)
            if (passedArguments.activityId == 0) {
                locationsViewModel.allLocations.observe(viewLifecycleOwner, Observer {
                    adapter.setLocations(it)
                })
            } else {
                locationsViewModel.locationsWithActivity(passedArguments.activityId)
                    .observe(viewLifecycleOwner, Observer {
                        adapter.setLocations(it.locations)
                    })
            }
        }
        getCurrentLocation()
    }

    @SuppressLint("MissingPermission")
    @AfterPermissionGranted(RC_LOCATION)
    private fun getCurrentLocation() {
        if(EasyPermissions.hasPermissions(requireContext(),
            android.Manifest.permission.ACCESS_FINE_LOCATION)){
            val fusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location : android.location.Location? ->
                if(location != null){
                    adapter.setCurrentLocation(location)
                }
            }
        }else{
            Snackbar.make(
                requireView(),
                getString(R.string.snackbar),
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.ok){
                EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.rationale_text),
                    RC_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            }.show()
        }
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

    override fun onClick(id: Int) {
        val action = LocationsFragmentDirections
            .actionNavigationLocationsToNavigationLocation()
        action.locationId = id
        val navController = Navigation.findNavController(requireView())
        navController.navigate(action)
    }
    companion object{
        const val RC_LOCATION = 1
    }
}
