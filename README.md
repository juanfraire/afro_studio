# AfroStudio App Analysis and Refactoring Plan

After analyzing the codebase, I (Claude Sonnet 3.7) can see this is a music creation application
focused on African rhythms. The app requires significant modernization as it uses outdated patterns
and technologies.

        ## Key Issues Identified

1. **AsyncTask Usage**: The app heavily relies on `AsyncTask` which has been deprecated since
   Android API 30
2. **Tightly Coupled Architecture**: Business logic, UI, and network calls are all mixed in the
   Activity
3. **No Architecture Pattern**: Lacks modern architecture patterns like MVVM or MVC
4. **Thread Management**: Direct thread handling instead of modern concurrency solutions
5. **View Binding**: Partial use of ViewBinding but mixed with findViewById
6. **Permissions Handling**: Old permission request pattern
7. **Network Operations**: Direct network calls on background threads without proper APIs
8. **Memory Management**: Potential memory leaks with long-running operations
9. **Error Handling**: Basic error handling with try/catch blocks

## Refactoring Plan (Prioritized)

### Phase 1: Essential Modernization (High Impact, Lower Effort)


1. COMPLETED! **Replace AsyncTask with Kotlin Coroutines**
    - Convert `PlayRithmTask` and `ConnectServerTask` to use coroutines
    - Implement proper scopes (viewModelScope, lifecycleScope)
    - Add proper cancellation handling when activity/fragment is destroyed
1.a. Add more tests for server connections, including these improvements:
    - Switch all tests to use JUnit5 if reasonable
   
    - Extract common test utilities:  
      Create a shared test fixture for common test data
      Use a TestRule to manage dispatcher setup/teardown

- Add coverage for network handling:  
Test reconnection logic if applicable
Test behavior with slow/intermittent connections

- Improve mocking approach:  
Use a more sophisticated mocking strategy for HttpURLConnection
Consider a fake server implementation for more realistic tests


2. **Implement ViewModel**
    - Create `EnsembleViewModel` to hold ensemble state and operations
    - Move business logic out of Activity into ViewModel
    - Utilize LiveData or StateFlow for UI updates

3. **Complete ViewBinding Implementation**
    - Standardize view access using ViewBinding throughout the app
    - Remove all findViewById calls

4. **Modernize Network Calls**
    - Replace direct URL connections with Retrofit
    - Implement proper error handling
    - Add timeout handling

### Phase 2: Structural Improvements (Medium Impact, Medium Effort)

5. **Implement Repository Pattern**
    - Create repositories for data access (EnsembleRepository, AudioRepository)
    - Abstract data sources (local storage, network)

6. **Improve File Management**
    - Use modern file access APIs like Storage Access Framework
    - Implement content provider for sharing files
    - Remove direct file path manipulations

7. **Update Permission Handling**
    - Implement runtime permission requests using modern APIs
    - Add proper permission checks and fallbacks

8. **Extract UI Components**
    - Break down MainActivity into smaller fragments
    - Create reusable custom views for ensemble elements
    - Extract dialog handling into separate classes

### Phase 3: Advanced Improvements (High Impact, Higher Effort)

9. **Migrate to Jetpack Compose**
    - Gradually replace XML layouts with Compose UI
    - Implement modern UI patterns and animations

10. **Implement Room Database**
    - Store ensemble data in structured database
    - Enable offline capabilities
    - Implement proper data migration strategies

11. **Add Unit and UI Testing**
    - Create test suite for core functionality
    - Implement UI tests for critical user journeys

12. **Dependency Injection**
    - Implement Hilt or Koin for dependency management
    - Make components more testable through DI

## Implementation Steps for Phase 1

        1. **AsyncTask Replacement**:
        ```kotlin

// In EnsembleViewModel
fun playRhythm(params: String = "") {
viewModelScope.launch {
withContext(Dispatchers.IO) {
// Implementation from PlayRithmTask.doInBackground
}
// Update UI on main thread
}
}

   ```

           2. **ViewModel Implementation**:
        ```kotlin
class EnsembleViewModel : ViewModel() {
    private val _ensemble = MutableStateFlow(Ensemble())
    val ensemble: StateFlow<Ensemble> = _ensemble

    // Functions to manipulate ensemble data
}
   ```

           3. **Repository Pattern**:
        ```kotlin

class EnsembleRepository(private val api: AfroStudioApi) {
suspend fun getEnsembleList(username: String): List<String> {
// Implementation
}

    suspend fun saveEnsemble(ensemble: Ensemble, username: String): Result<Unit> {
        // Implementation
    }

}

   ```

These changes would significantly modernize the app while keeping its core functionality intact, making it more maintainable and enhancing its performance.


MVVM: Model-View-ViewModel
https://medium.com/@jecky999/mvvm-architecture-in-android-using-kotlin-a-practical-guide-73f8de1d9c58




// LINKS
// Metronome with AudioTrack: http://masterex.github.io/archive/2012/05/28/android-audio-synthesis.html
// Wav to PCM: http://mindtherobot.com/blog/580/android-audio-play-a-wav-file-on-an-audiotrack/
// Wav File: http://www.codeproject.com/Articles/29676/CWave-A-Simple-C-Class-to-Manipulate-WAV-Files
// Why not a bit byte[] array http://mindtherobot.com/blog/633/android-performance-be-careful-with-byte/
// Java is pass-by-value (pointer) http://stackoverflow.com/questions/40480/is-java-pass-by-reference-or-pass-by-value
// SplashScreen http://stackoverflow.com/questions/1979524/android-splashscreen
// JSON: http://www.javacodegeeks.com/2013/10/android-json-tutorial-create-and-parse-json-data.html
// Layouts https://thinkandroid.wordpress.com/2010/01/14/how-to-position-views-properly-in-layouts/
// Activity Lifecicle: http://developer.android.com/reference/android/app/Activity.html
// ScrollView Bug http://blog.pivotal.io/labs/labs/centering-a-view-within-a-scrollview (bug in Android where setting layout_gravity=”center” on a ScrollView’s child)
// ActionBar http://developer.android.com/guide/topics/ui/actionbar.html

// Usage Flow:
// - Initial Load: Load saved / New Empty Rithm -> Set bars and instrument sizes
// - Edit/Playback Mode: Edit beats, add/remove instrument, add/remove bars, modify tempo
//
// ToDos:
// - The problem is it stops playing when the screen locks
// - Disabling the Balet in the main screen via 'Instrument Optiens' dialog let crash the App.
// - So, for the next update, is it possible to add new time signatures like 5/8 & 7/8? (http://www.rhythm-in-music.com/lesson-20-odd-meter-eighth-note-beat-time-signatures.html)
// - Fix: issue de trailing sounds en loop
// - Golpes especiales - Dialogo (apoyatura, tresillos (drum roll),  claps and muffled strokes, cuatrillos)
// - Otros instrumentos: balafons diatonique et pentatonique, Cora/n'goni
// - Mover compás en el tiempo?

// TODO alex29:
// - refactor to make the code manageable
// - add tests (preferably before refactoring, but seems impossible so far)
// - convert weird 4x tempo display to normal bpm
// - add 3/4 and 6/8 time signatures along with 4/4 so that the user can add triplets in the 4/4 ensemble (3/4 over 4/4, or the other way around)
// - add a new instrument, bells separately (maybe different 3 sizes/tones)
// - adjust tempo on the fly easily (slider?) so that it can be done while playing quickly with one hand, or maybe gradually in steps with one click (e.g. +10% in the next 10 seconds)
