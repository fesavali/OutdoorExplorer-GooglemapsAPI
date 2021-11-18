package com.savaliscodes.outdoorexplorer.ui.location

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.savaliscodes.outdoorexplorer.data.OutdoorRepository
import com.savaliscodes.outdoorexplorer.data.OutdoorRoomDatabase
import com.savaliscodes.outdoorexplorer.data.OutdoorRoomRepository

class LocationViewModel(application: Application) : AndroidViewModel(application) {
    private val outdoorRepository: OutdoorRepository

    init {
        val outdoorDao = OutdoorRoomDatabase.getInstance(application).outdoorDao()
        outdoorRepository = OutdoorRoomRepository(outdoorDao)
    }

    fun getLocation(locationId: Int) =
        outdoorRepository.getLocationWithActivities(locationId)
}