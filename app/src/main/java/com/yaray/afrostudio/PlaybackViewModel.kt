package com.yaray.afrostudio

import android.content.Context
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Random
import java.util.Vector
class PlaybackViewModel(application: Application) : AndroidViewModel(application) {
    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentBeat = MutableLiveData<Int>()
    val currentBeat: LiveData<Int> = _currentBeat

    private var playJob: Job? = null
    private var playbackJob: Job? = null
    private val appContext: Context = application.applicationContext
    private val userEmail: String = application.getSharedPreferences("AfroStudioPrefs", Context.MODE_PRIVATE)
        .getString("user", "undefined@undefined") ?: "undefined@undefined"
    private val soundBank by lazy { SoundBank() }

    fun startPlayback(
        ensemble: Ensemble,
        encoder: Encoder?,
        mode: String = "",
        onProgressUpdate: (Int) -> Unit,
        onCompleted: () -> Unit
    ) {
        playbackJob?.cancel()

        // Set isPlaying state
        _isPlaying.value = true

        // Initialize audio parameters
        val soundBank = ensemble.soundBank
        val byteBufferSizeInBytes = ensemble.byteBufferSizeInBytes
        var byteBuffer = ByteArray(byteBufferSizeInBytes)
        val djembeOffset = MutableList(ensemble.djembeVector.size) { 0 }

        // Initialize recorder if needed
        if (mode.contains("record")) {
            encoder?.init(appContext, ensemble, userEmail)
        }

        ensemble.onPlay = true
        ensemble.flagEnsembleUpdate = false

        // Start playback in a coroutine
        playbackJob = viewModelScope.launch(Dispatchers.Default) {
            ensemble.audioTrack.play()

            var currentBeat = 0
            while (currentBeat < (ensemble.getBeats() + 4) && ensemble.onPlay && isActive) {
                // Update UI on main thread
                withContext(Dispatchers.Main) {
                    _currentBeat.value = currentBeat
                    onProgressUpdate(currentBeat)
                }

                // Check if buffer size changed (tempo modification)
                if (byteBuffer.size != ensemble.byteBufferSizeInBytes) {
                    // Create new buffer with updated size
                    byteBuffer = ByteArray(ensemble.byteBufferSizeInBytes)
                }

                // Clear buffer
                clearBuffer(byteBuffer, ensemble.byteBufferSizeInBytes)

                // Process all instrument groups
                processInstrumentGroup("djembe", ensemble.djembeVector, ensemble.djembeStatus,
                    ensemble.djembeVolume, currentBeat, djembeOffset, byteBuffer, ensemble.byteBufferSizeInBytes, soundBank)

                // Check if playback was stopped
                if (!ensemble.onPlay || !isActive) break

                processInstrumentGroup("dun", ensemble.dunVector, ensemble.dunStatus,
                    ensemble.dunVolume, currentBeat, null, byteBuffer, ensemble.byteBufferSizeInBytes, soundBank)

                if (!ensemble.onPlay || !isActive) break

                processInstrumentGroup("ken", ensemble.kenVector, ensemble.kenStatus,
                    ensemble.kenVolume, currentBeat, null, byteBuffer, ensemble.byteBufferSizeInBytes, soundBank)

                if (!ensemble.onPlay || !isActive) break

                processInstrumentGroup("sag", ensemble.sagVector, ensemble.sagStatus,
                    ensemble.sagVolume, currentBeat, null, byteBuffer, ensemble.byteBufferSizeInBytes, soundBank)

                if (!ensemble.onPlay || !isActive) break

                processInstrumentGroup("balet", ensemble.baletVector, ensemble.baletStatus,
                    ensemble.baletVolume, currentBeat, null, byteBuffer, ensemble.byteBufferSizeInBytes, soundBank)

                if (!ensemble.onPlay || !isActive) break

                processInstrumentGroup("shek", ensemble.shekVector, ensemble.shekStatus,
                    ensemble.shekVolume, currentBeat, null, byteBuffer, ensemble.byteBufferSizeInBytes, soundBank)

                if (!ensemble.onPlay || !isActive) break

                // Handle audio output
                if (mode.contains("record")) {
                    encoder?.write(byteBuffer, 0, ensemble.byteBufferSizeInBytes, false)
                } else {
                    ensemble.audioTrack.write(byteBuffer, 0, ensemble.byteBufferSizeInBytes)
                }

                // Handle repetitions
                if ((currentBeat + 1) % ensemble.getBeatsPerBar() == 0) {
                    val currBar = (currentBeat + 1) / ensemble.getBeatsPerBar() - 1
                    if ((ensemble.repetitions[currBar][1] > 1) && (ensemble.repetitions[currBar][2] > 1)) {
                        ensemble.repetitions[currBar][2]-- // Decrease count
                        currentBeat = currentBeat - ensemble.getBeatsPerBar() * ensemble.repetitions[currBar][0]
                        if (currentBeat < -1) {
                            currentBeat = -1
                        }
                    }
                }

                // Loop ensemble
                if ((currentBeat == ensemble.getBeats() - 1) && ensemble.onLoop && !mode.contains("record")) {
                    // Reset ensemble repetitions
                    for (i in 0 until ensemble.repetitions.size) {
                        ensemble.repetitions[i][2] = ensemble.repetitions[i][1]
                    }
                    currentBeat = -1
                }

                currentBeat++
            }

            // Clean up when playback finishes
            withContext(Dispatchers.Main) {
                if (mode.contains("record")) {
                    encoder?.write(byteBuffer, 0, ensemble.byteBufferSizeInBytes, true)
                    encoder?.close()
                }

                ensemble.audioTrack.flush()
                ensemble.audioTrack.stop()
                ensemble.onPlay = false
                _isPlaying.value = false

                onCompleted()
            }
        }
    }

    private fun getSoundNameForCode(family: String, code: Int): String {
        val soundMap = HashMap<String, Map<Int, String>>()

        // Define mappings for djembe
        val djembeMap = hashMapOf(
            1 to "bass",
            2 to "tone",
            3 to "slap",
            4 to "bass_flam",
            5 to "tone_flam",
            6 to "slap_flam"
        )

        // Define mappings for dun
        val dunMap = hashMapOf(
            1 to "bass_bell",
            2 to "bell",
            3 to "bass_bell_mute"
        )

        // Define mappings for ken
        val kenMap = hashMapOf(
            1 to "bass_bell",
            2 to "bell",
            3 to "bass_bell_mute"
        )

        // Define mappings for sag
        val sagMap = hashMapOf(
            1 to "bass_bell",
            2 to "bell",
            3 to "bass_bell_mute"
        )

        // Define mappings for balet
        val baletMap = hashMapOf(
            1 to "dun",       // uses dun bass
            2 to "sag",       // uses sag bass
            3 to "ken",       // uses ken bass
            4 to "dun_mute",  // uses dun bass_mute
            5 to "sag_mute",  // uses sag bass_mute
            6 to "ken_mute",  // uses ken bass_mute
            7 to "ring"       // special ring sound
        )

        // Define mappings for shek
        val shekMap = hashMapOf(
            1 to "standard"
        )

        // Special sounds
        val specialMap = hashMapOf(
            1 to "silence",
            2 to "ring"
        )

        // Add all instrument families to the main map
        soundMap["djembe"] = djembeMap
        soundMap["dun"] = dunMap
        soundMap["ken"] = kenMap
        soundMap["sag"] = sagMap
        soundMap["balet"] = baletMap
        soundMap["shek"] = shekMap
        soundMap["special"] = specialMap

        return soundMap[family]?.get(code) ?: "silence"
    }

    private fun processInstrumentGroup(
        family: String,
        patterns: List<Vector<Int>>,
        status: List<Int>,
        volumes: List<Int>,
        currentBeat: Int,
        djembeOffset: MutableList<Int>?,
        byteBuffer: ByteArray,
        byteBufferSizeInBytes: Int,
        soundBank: SoundBank
    ) {
        for (i in patterns.indices) {
            if (status[i] == 1) {  // instrument is active
                val variant = if (family == "djembe") i % 3 else 0
                var offset = 0

                // Apply humanization only for djembe instruments
                if (family == "djembe" && djembeOffset != null) {
                    val pattern = patterns[i]
                    // Set new offset when a sound plays
                    if (pattern[currentBeat] != 0) {
                        val r = Random()
                        djembeOffset[i] = r.nextInt(200) * 2
                    }
                    offset = djembeOffset[i]
                }

                processInstrumentSound(
                    family,
                    null,
                    variant,
                    currentBeat,
                    patterns[i],
                    volumes[i],
                    offset,
                    byteBuffer,
                    byteBufferSizeInBytes,
                    soundBank
                )
            }
        }
    }

    private fun processInstrumentSound(
        family: String,
        soundType: String?,
        variant: Int,
        currentBeat: Int,
        instrumentPattern: Vector<Int>,
        volume: Int,
        offset: Int,
        byteBuffer: ByteArray,
        byteBufferSizeInBytes: Int,
        soundBank: SoundBank
    ) {
        val soundCode = instrumentPattern[currentBeat]

        if (soundCode > 0) {
            // Play the specific sound based on code with offset (offset = 0 for non-djembe instruments)
            val soundName = getSoundNameForCode(family, soundCode)
            addToBuffer(soundBank.getSound(family, soundName, variant), offset, volume, byteBuffer, byteBufferSizeInBytes)
        } else {
            // Check previous beats for trailing sounds with offset
            checkPreviousBeats(family, instrumentPattern, currentBeat, volume, variant, offset, byteBuffer, byteBufferSizeInBytes, soundBank)
        }
    }

    /**
     * 1. When a beat doesn't have a sound directly assigned to it (soundCode equals 0), this method looks back at previous beats.
     * 2. It searches up to 4 beats back to find the most recent beat that had a sound played.
     * 3. When it finds a previous beat with a sound, it adds that sound to the current buffer, but with an offset that corresponds to how many beats back it was.
     * 4. The offset ensures the trailing sound plays at the correct position within the current time slice.
     * 5. If no sound was played in the previous beats, it adds silence.
     */
    private fun checkPreviousBeats(
        family: String,
        pattern: Vector<Int>,
        currentBeat: Int,
        volume: Int,
        variant: Int,
        offset: Int,
        byteBuffer: ByteArray,
        byteBufferSizeInBytes: Int,
        soundBank: SoundBank
    ) {
        // Check up to MAX_BEATS_BACK for trailing sound
        val MAX_BEATS_BACK = 4

        for (beatsBack in 1..MAX_BEATS_BACK) {
            val prevBeat = currentBeat - beatsBack
            if (prevBeat >= 0) {
                val prevSoundCode = pattern[prevBeat]
                if (prevSoundCode > 0) {
                    val soundName = getSoundNameForCode(family, prevSoundCode)
                    // Add offset to the buffer timing
                    addToBuffer(
                        soundBank.getSound(family, soundName, variant),
                        byteBufferSizeInBytes * beatsBack + offset,
                        volume,
                        byteBuffer,
                        byteBufferSizeInBytes
                    )
                    return
                }
            }
        }

        // No previous sound found, play silence
        addToBuffer(soundBank.getSound("special", "silence"), 0, volume, byteBuffer, byteBufferSizeInBytes)
    }

    private fun addToBuffer(
        byteBufferIn: ByteArray,
        byteBufferOffset: Int,
        volume: Int,
        byteBuffer: ByteArray,
        byteBufferSizeInBytes: Int
    ) {
        val r = Random() // Humanization in volume
        var newVolume = volume + r.nextInt(30)
        if (newVolume > 100) newVolume = 100

        for (i in 0 until byteBufferSizeInBytes step 2) {
            // in 16 bit wav PCM, first byte is the low order byte
            if (byteBufferOffset + i + 1 < byteBufferIn.size) { //Otherwise do not add nothing
                val currentVal1 = (byteBuffer[i].toInt() and 0xFF).toShort()
                val currentVal2 = ((byteBuffer[i + 1].toInt() and 0xFF) shl 8).toShort()
                val currentVal = (currentVal1 + currentVal2).toShort()
                val bufferInVal1 = (byteBufferIn[byteBufferOffset + i].toInt() and 0xFF).toShort()
                val bufferInVal2 = ((byteBufferIn[byteBufferOffset + i + 1].toInt() and 0xFF) shl 8).toShort()
                var bufferInVal = (bufferInVal1 + bufferInVal2).toShort()

                bufferInVal = (bufferInVal * (newVolume.toFloat() / 50)).toInt().toShort() //100 - //volume 0 to 100 (50% volume boost)

                // If Im am on the last part, copy new buffer,and attenuate last trailing zeroes
                if (byteBufferOffset == byteBufferSizeInBytes * 4) // last trailing zeroes
                    bufferInVal = (bufferInVal - (bufferInVal * i.toFloat() / (byteBufferSizeInBytes - 2).toFloat()).toInt()).toShort()

                val newVal = (currentVal + bufferInVal).toShort()
                byteBuffer[i] = (newVal.toInt() and 0xFF).toByte()
                byteBuffer[i + 1] = ((newVal.toInt() and 0xFF00) ushr 8).toByte() //>>> unsigned (add zeroes)
            } else {
                break
            }
        }
    }

    private fun clearBuffer(byteBuffer: ByteArray, size: Int) {
        for (i in 0 until size) {
            byteBuffer[i] = 0
        }
    }

    fun stopPlayback(ensemble: Ensemble) {
        ensemble.onPlay = false
        playJob?.cancel()
        playbackJob?.cancel()
        playJob = null
        playbackJob = null
        _isPlaying.value = false
    }

    override fun onCleared() {
        super.onCleared()
        playJob?.cancel()
        playbackJob?.cancel()
    }
}
