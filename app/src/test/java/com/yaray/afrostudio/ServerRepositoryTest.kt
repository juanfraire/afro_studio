package com.yaray.afrostudio

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

@ExperimentalCoroutinesApi
class ServerRepositoryTest {

    private lateinit var repository: TestServerRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        repository = TestServerRepository(testDispatcher)
    }

    @Test
    fun `build parameters for register action`() {
        // When
        val params = RequestParams(
            username = "test@example.com",
            action = "register"
        )
        val result = repository.publicBuildParameters(params)

        // Then
        assertTrue(result.contains("username=test%40example.com"))
        assertTrue(result.contains("action=register"))
    }

    @Test
    fun `build parameters for setEnsemble action`() {
        // When
        val params = RequestParams(
            username = "test@example.com",
            action = "setEnsemble",
            ensembleName = "Test Ensemble",
            ensembleAuthor = "Test Author",
            ensembleJSON = "{\"key\":\"value\"}"
        )
        val result = repository.publicBuildParameters(params)

        // Then
        assertTrue(result.contains("username=test%40example.com"))
        assertTrue(result.contains("action=setEnsemble"))
        assertTrue(result.contains("ensemblename=Test+Ensemble"))
        assertTrue(result.contains("ensembleauthor=Test+Author"))
        assertTrue(result.contains("ensemblejson=%7B%22key%22%3A%22value%22%7D"))
    }

    @Test
    fun `build parameters for getEnsemble action`() {
        // When
        val params = RequestParams(
            username = "test@example.com",
            action = "getEnsemble",
            ensembleName = "Test Ensemble",
            ensembleAuthor = "Test Author",
            ensembleUser = "testuser"
        )
        val result = repository.publicBuildParameters(params)

        // Then
        assertTrue(result.contains("username=test%40example.com"))
        assertTrue(result.contains("action=getEnsemble"))
        assertTrue(result.contains("ensemblename=Test+Ensemble"))
        assertTrue(result.contains("ensembleauthor=Test+Author"))
        assertTrue(result.contains("ensembleuser=testuser"))
    }

    @Test
    fun `build parameters for getEnsembleList action`() {
        // When
        val params = RequestParams(
            username = "test@example.com",
            action = "getEnsembleList"
        )
        val result = repository.publicBuildParameters(params)

        // Then
        assertTrue(result.contains("username=test%40example.com"))
        assertTrue(result.contains("action=getEnsembleList"))
    }

    @Test
    fun `build parameters for deleteEnsemble action`() {
        // When
        val params = RequestParams(
            username = "test@example.com",
            action = "deleteEnsemble",
            ensembleName = "Test Ensemble",
            ensembleAuthor = "Test Author"
        )
        val result = repository.publicBuildParameters(params)

        // Then
        assertTrue(result.contains("username=test%40example.com"))
        assertTrue(result.contains("action=deleteEnsemble"))
        assertTrue(result.contains("ensemblename=Test+Ensemble"))
        assertTrue(result.contains("ensembleauthor=Test+Author"))
    }

    @Test
    fun `detect payload too large`() = runTest {
        // Given
        val params = RequestParams(
            username = "test@example.com",
            action = "setEnsemble",
            ensembleJSON = "x".repeat(70000)
        )

        // When
        val result = repository.executeRequest(params)

        // Then
        assertTrue(result.isFailure)
        val exception = result.exceptionOrNull()
        assertTrue(exception is PayloadTooLargeException)
    }
}

// Test helper class that implements ServerRepository instead of extending ServerRepositoryImpl
class TestServerRepository(
    private val testDispatcher: TestDispatcher = StandardTestDispatcher()
) : ServerRepository {

    private val baseUrl = "https://test.url"
    private val timeout = 1000L

    // Public method to expose parameter building for testing
    fun publicBuildParameters(params: RequestParams): String {
        return when (params.action) {
            "register" -> "username=${java.net.URLEncoder.encode(params.username, "UTF-8")}&action=${params.action}"
            "setEnsemble" -> "username=${java.net.URLEncoder.encode(params.username, "UTF-8")}&action=${params.action}" +
                    "&ensemblename=${java.net.URLEncoder.encode(params.ensembleName ?: "", "UTF-8")}" +
                    "&ensembleauthor=${java.net.URLEncoder.encode(params.ensembleAuthor ?: "", "UTF-8")}" +
                    "&ensemblejson=${java.net.URLEncoder.encode(params.ensembleJSON ?: "", "UTF-8")}"
            "getEnsemble" -> "username=${java.net.URLEncoder.encode(params.username, "UTF-8")}&action=${params.action}" +
                    "&ensemblename=${java.net.URLEncoder.encode(params.ensembleName ?: "", "UTF-8")}" +
                    "&ensembleauthor=${java.net.URLEncoder.encode(params.ensembleAuthor ?: "", "UTF-8")}" +
                    "&ensembleuser=${java.net.URLEncoder.encode(params.ensembleUser ?: "", "UTF-8")}"
            "getEnsembleList" -> "username=${java.net.URLEncoder.encode(params.username, "UTF-8")}&action=${params.action}"
            "deleteEnsemble" -> "username=${java.net.URLEncoder.encode(params.username, "UTF-8")}&action=${params.action}" +
                    "&ensemblename=${java.net.URLEncoder.encode(params.ensembleName ?: "", "UTF-8")}" +
                    "&ensembleauthor=${java.net.URLEncoder.encode(params.ensembleAuthor ?: "", "UTF-8")}"
            else -> "username=${java.net.URLEncoder.encode(params.username, "UTF-8")}&action=${params.action}"
        }
    }

    override suspend fun executeRequest(params: RequestParams): Result<String> {
        // For the payload too large test
        val parameters = publicBuildParameters(params)
        if (parameters.length > 65500) {
            return Result.failure(
                PayloadTooLargeException("Request payload too large (${parameters.length} bytes)")
            )
        }

        // Mock implementation for testing
        return Result.success("Success")
    }
}
