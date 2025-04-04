package com.yaray.afrostudio
// uses JUnit 5
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExperimentalCoroutinesApi
class ServerIntegrationTest {

    // Using JUnit 5 extension instead of Rule
    @ExtendWith(InstantTaskExecutorExtension::class)

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModelFactory: ServerViewModelFactory
    private lateinit var mockRepository: MockServerRepository
    private lateinit var viewModel: ServerViewModel

    @BeforeEach
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = MockServerRepository()
        viewModelFactory = ServerViewModelFactory(mockRepository)
        viewModel = viewModelFactory.create(ServerViewModel::class.java)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `factory creates proper view model`() {
        assertTrue(viewModel is ServerViewModel)
    }

    @Test
    fun `complete register flow`() = runTest {
        // Given
        mockRepository.responseToReturn = Result.success("Registration successful")

        // When
        viewModel.connectToServer("test@example.com", "register")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val response = viewModel.serverResponse.value
        assertTrue(response is ServerViewModel.ServerResponse.Success)
        assertEquals("Registration successful", response?.getResponseData())
        assertEquals(1, mockRepository.callCount)
    }

    @Test
    fun `complete setEnsemble flow`() = runTest {
        // Given
        mockRepository.responseToReturn = Result.success("setEnsemble_ok")

        // When
        viewModel.connectToServer(
            username = "test@example.com",
            action = "setEnsemble",
            ensembleName = "Test Ensemble",
            ensembleAuthor = "Test Author",
            ensembleJSON = "{\"data\":\"test\"}"
        )
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val params = mockRepository.capturedParams
        assertEquals("test@example.com", params?.username)
        assertEquals("setEnsemble", params?.action)
        assertEquals("Test Ensemble", params?.ensembleName)
        assertEquals("Test Author", params?.ensembleAuthor)
        assertEquals("{\"data\":\"test\"}", params?.ensembleJSON)

        val response = viewModel.serverResponse.value
        assertTrue(response is ServerViewModel.ServerResponse.Success)
    }

    @Test
    fun `test error propagation`() = runTest {
        // Given
        mockRepository.responseToReturn = Result.failure(NetworkUnavailableException("No connection"))

        // When
        viewModel.connectToServer("test@example.com", "getEnsembleList")
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val response = viewModel.serverResponse.value as? ServerViewModel.ServerResponse.Error
        assertNotNull(response)
        assertEquals("getEnsembleList", response?.getAction())
        assertEquals(ServerViewModel.ErrorType.NETWORK_UNAVAILABLE, response?.errorType)
        assertEquals("No connection", response?.errorMessage)
    }
}
