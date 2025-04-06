package com.yaray.afrostudio

import android.app.Application
import android.content.Context
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import android.widget.ViewAnimator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Vector

class EnsembleViewModel(application: Application) : AndroidViewModel(application) {
    private val appContext = application.applicationContext
    private val settings = appContext.getSharedPreferences("AfroStudioPrefs", Context.MODE_PRIVATE)

    // Ensemble state
    private val _ensemble = MutableLiveData<Ensemble>()
    val ensemble: LiveData<Ensemble> = _ensemble

    // UI update events
    private val _uiEvent = MutableLiveData<UiEvent>()
    val uiEvent: LiveData<UiEvent> = _uiEvent

    // Load/save status
    private val _loadState = MutableLiveData<LoadState>()
    val loadState: LiveData<LoadState> = _loadState

    init {
        // Initialize ensemble
        _ensemble.value = Ensemble(appContext)
    }

    fun getEnsemble(): Ensemble? {
        return _ensemble.value
    }

    fun createNewEnsemble(beatsPerBar: Int, numBar: Int, emptyInstruments: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            _ensemble.value?.setVectorsFromEmpty(beatsPerBar, numBar, emptyInstruments, appContext)
            withContext(Dispatchers.Main) {
                _uiEvent.value = UiEvent.EnsembleUpdated
            }
        }
    }

    fun saveEnsemble(ensembleLayout: LinearLayout, iconScale: Float, viewAnimator: ViewAnimator, doServerUpload: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            val ensemble = _ensemble.value ?: return@launch

            // Update the ensemble from GUI before saving
            EnsembleUtils.setEnsembleFromGui(ensembleLayout, ensemble)

            // Save ensemble
            val userEmail = settings.getString("user", "undefined@undefined") ?: "undefined@undefined"
            EnsembleUtils.saveEnsemble(ensemble, appContext, true, doServerUpload, viewAnimator, userEmail)

            withContext(Dispatchers.Main) {
                _uiEvent.value = UiEvent.EnsembleSaved
            }
        }
    }

    fun loadEnsemble(fileName: String?, horizontalScrollView: FrameLayout) {
        if (fileName == null) {
            _uiEvent.value = UiEvent.ShowToast(R.string.toast_no_file_chosen)
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val ensemble = _ensemble.value ?: return@launch

            if (fileName.contains(".afr")) { // Local file
                loadLocalEnsemble(fileName, ensemble)
            } else { // Server file
                parseServerFileName(fileName, ensemble)
            }

            withContext(Dispatchers.Main) {
                _uiEvent.value = UiEvent.EnsembleLoaded
            }
        }
    }

    private fun loadLocalEnsemble(fileName: String, ensemble: Ensemble) {
        try {
            val file = File(appContext.getExternalFilesDir(null), fileName)
            val inputStream = FileInputStream(file)
            val scanner = java.util.Scanner(inputStream).useDelimiter("\\A")
            val jsonString = if (scanner.hasNext()) scanner.next() else ""
            ensemble.setVectorsFromJSON(jsonString)
            _loadState.postValue(LoadState.Success)
        } catch (e: IOException) {
            _loadState.postValue(LoadState.Error("Error reading file: ${e.message}"))
        }
    }

    private fun parseServerFileName(fileName: String, ensemble: Ensemble) {
        val ensembleName = fileName.substring(0, fileName.indexOf("_by_"))
        val ensembleAuthor = fileName.substring(fileName.indexOf("_by_") + 4, fileName.indexOf("_u_"))
        val ensembleUser = fileName.substring(fileName.indexOf("_u_") + 3, fileName.length)

        // For now, just post a message - server loading would be handled by ServerViewModel
        _loadState.postValue(LoadState.Loading(ensembleName, ensembleAuthor, ensembleUser))
    }

    fun updateEnsembleFromServerJson(jsonString: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val ensemble = _ensemble.value ?: return@launch
            ensemble.setVectorsFromJSON(jsonString)
            withContext(Dispatchers.Main) {
                _uiEvent.value = UiEvent.EnsembleLoaded
            }
        }
    }

    fun setEnsembleName(name: String, author: String) {
        val ensemble = _ensemble.value ?: return
        ensemble.ensembleName = name
        ensemble.ensembleAuthor = author
        _uiEvent.value = UiEvent.NameUpdated
    }

    fun resetRepetition(thisBar: Int, barCount: Int, countTotal: Int) {
        val ensemble = _ensemble.value ?: return

        ensemble.repetitions.get(thisBar)[0] = barCount // barCount
        ensemble.repetitions.get(thisBar)[1] = countTotal // total
        ensemble.repetitions.get(thisBar)[2] = countTotal // current

        if (ensemble.onPlay)
            ensemble.flagEnsembleUpdate = true
        else
            _uiEvent.value = UiEvent.EnsembleUpdated
    }

    fun clearBar(barPos: Int) {
        val ensemble = _ensemble.value ?: return

        for (i in 0 until ensemble.djembeVector.size)
            for (j in ensemble.getBeatsPerBar() * barPos until ensemble.getBeatsPerBar() * (barPos + 1))
                ensemble.djembeVector.get(i).setElementAt(0, j)
        for (i in 0 until ensemble.shekVector.size)
            for (j in ensemble.getBeatsPerBar() * barPos until ensemble.getBeatsPerBar() * (barPos + 1))
                ensemble.shekVector.get(i).setElementAt(0, j)
        for (i in 0 until ensemble.dunVector.size)
            for (j in ensemble.getBeatsPerBar() * barPos until ensemble.getBeatsPerBar() * (barPos + 1))
                ensemble.dunVector.get(i).setElementAt(0, j)
        for (i in 0 until ensemble.sagVector.size)
            for (j in ensemble.getBeatsPerBar() * barPos until ensemble.getBeatsPerBar() * (barPos + 1))
                ensemble.sagVector.get(i).setElementAt(0, j)
        for (i in 0 until ensemble.kenVector.size)
            for (j in ensemble.getBeatsPerBar() * barPos until ensemble.getBeatsPerBar() * (barPos + 1))
                ensemble.kenVector.get(i).setElementAt(0, j)
        for (i in 0 until ensemble.baletVector.size)
            for (j in ensemble.getBeatsPerBar() * barPos until ensemble.getBeatsPerBar() * (barPos + 1))
                ensemble.baletVector.get(i).setElementAt(0, j)

        _uiEvent.value = UiEvent.EnsembleUpdated
    }

    fun removeBar(barPos: Int) {
        val ensemble = _ensemble.value ?: return

        // Remove the bar data
        val beatsPerBar = ensemble.getBeatsPerBar()

        // Remove repetitions for this bar
        ensemble.repetitions.removeAt(barPos)

        if (ensemble.onPlay)
            ensemble.flagEnsembleUpdate = true
        else
            _uiEvent.value = UiEvent.BarRemoved(barPos)
    }

    fun setBarName(barPos: Int, name: String) {
        val ensemble = _ensemble.value ?: return
        ensemble.barName.setElementAt(name, barPos)
        _uiEvent.value = UiEvent.BarNameUpdated(barPos)
    }

    fun clearInstrument(instrumentLayout: LinearLayout) {
        CoroutineScope(Dispatchers.IO).launch {
            val ensemble = _ensemble.value ?: return@launch

            // Clear the instrument layout
            if (ensemble.onPlay) {
                ensemble.flagEnsembleUpdate = true
            } else {
                withContext(Dispatchers.Main) {
                    _uiEvent.value = UiEvent.InstrumentCleared
                }
            }
        }
    }

    fun removeInstrument(instrumentView: LinearLayout) {
        val ensemble = _ensemble.value ?: return

        if (ensemble.onPlay) {
            ensemble.flagEnsembleUpdate = true
        } else {
            _uiEvent.value = UiEvent.InstrumentRemoved
        }
    }

    fun updateEnsembleFromLayout(ensembleLayout: LinearLayout) {
        val ensemble = _ensemble.value ?: return
        EnsembleUtils.setEnsembleFromGui(ensembleLayout, ensemble)
    }

    fun setInstrumentVolume(instrumentType: String, index: Int, volume: Int) {
        val ensemble = _ensemble.value ?: return

        when (instrumentType) {
            "djembe" -> ensemble.djembeVolume.setElementAt(volume, index)
            "dun" -> ensemble.dunVolume.setElementAt(volume, index)
            "ken" -> ensemble.kenVolume.setElementAt(volume, index)
            "sag" -> ensemble.sagVolume.setElementAt(volume, index)
            "shek" -> ensemble.shekVolume.setElementAt(volume, index)
            "balet" -> ensemble.baletVolume.setElementAt(volume, index)
        }

        // Update UI
        _uiEvent.value = UiEvent.VolumeUpdated
    }

    // Event classes for UI updates
    sealed class UiEvent {
        object EnsembleUpdated : UiEvent()
        object EnsembleSaved : UiEvent()
        object EnsembleLoaded : UiEvent()
        object NameUpdated : UiEvent()
        object InstrumentCleared : UiEvent()
        object InstrumentRemoved : UiEvent()
        object VolumeUpdated : UiEvent()
        data class ShowToast(val messageResId: Int) : UiEvent()
        data class BarRemoved(val barPos: Int) : UiEvent()
        data class BarNameUpdated(val barPos: Int) : UiEvent()
    }

    // States for load operations
    sealed class LoadState {
        object Success : LoadState()
        data class Loading(val name: String, val author: String, val user: String) : LoadState()
        data class Error(val message: String) : LoadState()
    }
}
