package com.savaliscodes.outdoorexplorer.ui.locations

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.savaliscodes.outdoorexplorer.data.OutdoorRepository
import com.savaliscodes.outdoorexplorer.data.OutdoorRoomDatabase
import com.savaliscodes.outdoorexplorer.data.OutdoorRoomRepository

class LocationsViewModel(application: Application) : AndroidViewModel(application) {
    private val outdoorRepository: OutdoorRepository

    init {
        val outdoorDao = OutdoorRoomDatabase.getInstance(application).outdoorDao()
        outdoorRepository = OutdoorRoomRepository(outdoorDao)
    }

    val allLocations = outdoorRepository.getAllLocations()

    fun locationsWithActivity(activityId: Int) =
        outdoorRepository.getActivityWithLocations(activityId)
}