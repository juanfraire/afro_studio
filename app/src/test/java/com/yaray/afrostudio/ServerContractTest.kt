package com.yaray.afrostudio
// uses JUnit 5
import androidx.arch.core.executor.ArchTaskExecutor
import androidx.arch.core.executor.TaskExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext

/**
 * JUnit 5 extension to replace InstantTaskExecutorRule for LiveData testing
 */
class InstantTaskExecutorExtension : BeforeEachCallback, AfterEachCallback {
    override fun beforeEach(context: ExtensionContext) {
        ArchTaskExecutor.getInstance().setDelegate(object : TaskExecutor() {
            override fun executeOnDiskIO(runnable: Runnable) = runnable.run()
            override fun postToMainThread(runnable: Runnable) = runnable.run()
            override fun isMainThread(): Boolean = true
        })
    }

    override fun afterEach(context: ExtensionContext) {
        ArchTaskExecutor.getInstance().setDelegate(null)
    }
}

/**
 * Contract tests to verify consistent behavior between ServerRepository and ServerViewModel.
 *
 * These tests ensure that repository results are properly transformed into ViewModel states.
 */
@ExtendWith(InstantTaskExecutorExtension::class)
@ExperimentalCoroutinesApi
class ServerContractTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var mockRepository: MockServerRepository
    private lateinit var viewModel: ServerViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockServerRepository()
        viewModel = ServerViewModel(mockRepository)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `repository success is properly mapped to ViewModel success state`() = runTest {
        // Given - Repository returns success
        mockRepository.responseToReturn = Result.success("Test response")

        // When - ViewModel uses repository
        viewModel.connectToServer("test@example.com", "register")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - ViewModel state reflects repository result
        val response = viewModel.serverResponse.value
        assertTrue(response is ServerViewModel.ServerResponse.Success)
        assertEquals("Test response", response?.getResponseData())
    }

    @Test
    fun `repository network error is properly mapped to ViewModel error state`() = runTest {
        // Given - Repository returns network error
        mockRepository.responseToReturn = Result.failure(NetworkUnavailableException("Network down"))

        // When - ViewModel uses repository
        viewModel.connectToServer("test@example.com", "register")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - ViewModel state reflects repository error
        val response = viewModel.serverResponse.value
        assertTrue(response is ServerViewModel.ServerResponse.Error)
        assertEquals(ServerViewModel.ErrorType.NETWORK_UNAVAILABLE,
            (response as ServerViewModel.ServerResponse.Error).errorType)
    }

    @Test
    fun `repository timeout is properly mapped to ViewModel timeout error state`() = runTest {
        // Given - Repository returns timeout
        mockRepository.responseToReturn = Result.failure(TimeoutException("Request timed out"))

        // When - ViewModel uses repository
        viewModel.connectToServer("test@example.com", "register")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - ViewModel state reflects timeout
        val response = viewModel.serverResponse.value
        assertTrue(response is ServerViewModel.ServerResponse.Error)
        assertEquals(ServerViewModel.ErrorType.TIMEOUT,
            (response as ServerViewModel.ServerResponse.Error).errorType)
    }

    @Test
    fun `repository payload too large is properly mapped to ViewModel error state`() = runTest {
        // Given - Repository returns payload too large
        mockRepository.responseToReturn = Result.failure(PayloadTooLargeException("Request too large"))

        // When - ViewModel uses repository
        viewModel.connectToServer("test@example.com", "setEnsemble", ensembleJSON = "large_json")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - ViewModel state reflects correct error type
        val response = viewModel.serverResponse.value
        assertTrue(response is ServerViewModel.ServerResponse.Error)
        assertEquals(ServerViewModel.ErrorType.PAYLOAD_TOO_LARGE,
            (response as ServerViewModel.ServerResponse.Error).errorType)
    }

    @Test
    fun `getEnsembleList success from repository properly transforms to ViewModel state`() = runTest {
        // Given - Repository returns ensemble list
        mockRepository.responseToReturn = Result.success("Ensemble1_by_Author1_u_user1+Ensemble2_by_Author2_u_user2+")

        // When - ViewModel uses repository
        viewModel.connectToServer("test@example.com", "getEnsembleList")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - ViewModel state contains parsed ensemble list
        val response = viewModel.serverResponse.value as? ServerViewModel.ServerResponse.Success
        assertNotNull(response)
        assertEquals(2, response?.getEnsembleList()?.size)
        assertEquals(listOf("Ensemble1_by_Author1_u_user1", "Ensemble2_by_Author2_u_user2"),
            response?.getEnsembleList())
    }

    @Test
    fun `request params are correctly passed from ViewModel to repository`() = runTest {
        // Given
        mockRepository.responseToReturn = Result.success("Success")

        // When - ViewModel makes request with specific params
        viewModel.connectToServer(
            username = "test@example.com",
            action = "setEnsemble",
            ensembleName = "Test Ensemble",
            ensembleAuthor = "Test Author",
            ensembleJSON = "{\"data\":\"test\"}"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Repository receives exact same params
        val params = mockRepository.capturedParams
        assertEquals("test@example.com", params?.username)
        assertEquals("setEnsemble", params?.action)
        assertEquals("Test Ensemble", params?.ensembleName)
        assertEquals("Test Author", params?.ensembleAuthor)
        assertEquals("{\"data\":\"test\"}", params?.ensembleJSON)
    }

    @Test
    fun `setEnsemble with server error response properly maps to error state`() = runTest {
        // Given - Repository returns error message
        mockRepository.responseToReturn = Result.success("server error: missing data")

        // When - ViewModel processes this response
        viewModel.connectToServer(
            username = "test@example.com",
            action = "setEnsemble",
            ensembleName = "Test",
            ensembleAuthor = "Author"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - ViewModel maps it to error state
        val response = viewModel.serverResponse.value
        assertTrue(response is ServerViewModel.ServerResponse.Error)
        assertEquals(ServerViewModel.ErrorType.SERVER_ERROR,
            (response as ServerViewModel.ServerResponse.Error).errorType)
    }

    @Test
    fun `error responses in getEnsembleList are properly filtered`() = runTest {
        // Given - Repository returns list with error
        mockRepository.responseToReturn = Result.success("Ensemble1+error: server error+Ensemble2+")

        // When - ViewModel processes this response
        viewModel.connectToServer(
            username = "test@example.com",
            action = "getEnsembleList"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - ViewModel filters error entries
        val response = viewModel.serverResponse.value as? ServerViewModel.ServerResponse.Success
        assertNotNull(response)
        assertEquals(2, response?.getEnsembleList()?.size)
        assertEquals(listOf("Ensemble1", "Ensemble2"), response?.getEnsembleList())
    }
}
