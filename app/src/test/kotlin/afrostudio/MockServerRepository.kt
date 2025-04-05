package com.yaray.afrostudio

class MockServerRepository : ServerRepository {
    var responseToReturn: Result<String> = Result.success("")
    var capturedParams: RequestParams? = null
    var callCount = 0

    override suspend fun executeRequest(params: RequestParams): Result<String> {
        capturedParams = params
        callCount++
        return responseToReturn
    }

    // Helper method for tests to verify calls
    fun verifyCalledWith(action: String): Boolean {
        return capturedParams?.action == action
    }

    // Reset for testing
    fun reset() {
        responseToReturn = Result.success("")
        capturedParams = null
        callCount = 0
    }
}
