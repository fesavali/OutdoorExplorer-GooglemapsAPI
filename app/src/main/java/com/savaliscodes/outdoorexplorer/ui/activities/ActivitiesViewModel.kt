package com.savaliscodes.outdoorexplorer.ui.activities

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.savaliscodes.outdoorexplorer.data.OutdoorRepository
import com.savaliscodes.outdoorexplorer.data.OutdoorRoomDatabase
import com.savaliscodes.outdoorexplorer.data.OutdoorRoomRepository

class ActivitiesViewModel(application: Application) : AndroidViewModel(application) {
    private val outdoorRepository: OutdoorRepository

    init {
        val outdoorDao = OutdoorRoomDatabase.getInstance(application).outdoorDao()
        outdoorRepository = OutdoorRoomRepository(outdoorDao)
    }

    val allActivities = outdoorRepository.getAllActivities()

    fun toggleGeofencing(id: Int) = outdoorRepository.toggleActivityGeofence(id)
}