package com.yaray.afrostudio

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.regex.PatternSyntaxException

class ServerViewModel(
    private val serverRepository: ServerRepository
) : ViewModel() {

    private val _serverResponse = MutableLiveData<ServerResponse>()
    val serverResponse: LiveData<ServerResponse> = _serverResponse

    // Represents the current state of server operations using a sealed class
    sealed class ServerResponse {
        abstract fun getAction(): String
        abstract fun getResponseData(): String
        abstract fun getEnsembleList(): List<String>
        abstract fun isSuccess(): Boolean

        data class Success(
            private val actionValue: String,
            private val responseDataValue: String,
            private val ensembleListValue: List<String> = emptyList()
        ) : ServerResponse() {
            override fun getAction(): String = actionValue
            override fun getResponseData(): String = responseDataValue
            override fun getEnsembleList(): List<String> = ensembleListValue
            override fun isSuccess(): Boolean = true
        }

        data class Error(
            private val actionValue: String,
            val errorType: ErrorType,
            val errorMessage: String
        ) : ServerResponse() {
            override fun getAction(): String = actionValue
            override fun getResponseData(): String = ""
            override fun getEnsembleList(): List<String> = emptyList()
            override fun isSuccess(): Boolean = false
        }
    }

    enum class ErrorType {
        NETWORK_UNAVAILABLE,
        TIMEOUT,
        SERVER_ERROR,
        PAYLOAD_TOO_LARGE,
        PARSE_ERROR,
        UNKNOWN
    }

    fun connectToServer(
        username: String,
        action: String,
        ensembleName: String? = null,
        ensembleAuthor: String? = null,
        ensembleJSON: String? = null,
        ensembleUser: String? = null
    ) {
        viewModelScope.launch {
            try {
                val params = RequestParams(
                    username = username,
                    action = action,
                    ensembleName = ensembleName,
                    ensembleAuthor = ensembleAuthor,
                    ensembleJSON = ensembleJSON,
                    ensembleUser = ensembleUser
                )

                val result = serverRepository.executeRequest(params)

                result.fold(
                    onSuccess = { responseData ->
                        processResponse(action, responseData)
                    },
                    onFailure = { exception ->
                        handleError(action, exception)
                    }
                )
            } catch (e: Exception) {
                handleError(action, e)
            }
        }
    }

    private fun handleError(action: String, exception: Throwable) {
        val errorType = when (exception) {
            is NetworkUnavailableException -> ErrorType.NETWORK_UNAVAILABLE
            is TimeoutException -> ErrorType.TIMEOUT
            is ServerException -> ErrorType.SERVER_ERROR
            is PayloadTooLargeException -> ErrorType.PAYLOAD_TOO_LARGE
            is ParseException -> ErrorType.PARSE_ERROR
            else -> ErrorType.UNKNOWN
        }

        _serverResponse.value = ServerResponse.Error(
            actionValue = action,
            errorType = errorType,
            errorMessage = exception.message ?: "Unknown error"
        )
    }

    private fun processResponse(action: String, result: String) {
        when (action) {
            "register" -> {
                _serverResponse.value = ServerResponse.Success(actionValue = action, responseDataValue = result)
            }
            "setEnsemble" -> {
                val isSuccess = result.contains("setEnsemble_ok")
                if (isSuccess) {
                    _serverResponse.value = ServerResponse.Success(actionValue = action, responseDataValue = result)
                } else {
                    _serverResponse.value = ServerResponse.Error(
                        actionValue = action,
                        errorType = ErrorType.SERVER_ERROR,
                        errorMessage = "Server error: $result"
                    )
                }
            }
            "getEnsemble" -> {
                val isSuccess = result.isNotBlank() && !result.contains("error")
                if (isSuccess) {
                    _serverResponse.value = ServerResponse.Success(actionValue = action, responseDataValue = result)
                } else {
                    _serverResponse.value = ServerResponse.Error(
                        actionValue = action,
                        errorType = ErrorType.SERVER_ERROR,
                        errorMessage = "Server error: $result"
                    )
                }
            }
            "getEnsembleList", "deleteEnsemble" -> {
                try {
                    val ensembleList = parseEnsembleList(result)
                    _serverResponse.value = ServerResponse.Success(
                        actionValue = action,
                        responseDataValue = result,
                        ensembleListValue = ensembleList
                    )
                } catch (e: Exception) {
                    _serverResponse.value = ServerResponse.Error(
                        actionValue = action,
                        errorType = ErrorType.PARSE_ERROR,
                        errorMessage = "Parse error: ${e.message}"
                    )
                }
            }
        }
    }

    private fun parseEnsembleList(response: String?): List<String> {
        val ensembleList = mutableListOf<String>()

        if (response != null && response.isNotBlank()) {
            try {
                val lines = response.split("+")
                for (line in lines) {
                    val trimmed = line.trim()
                    if (trimmed.isNotEmpty() && !trimmed.startsWith("error")) {
                        ensembleList.add(trimmed)
                    }
                }
            } catch (ex: PatternSyntaxException) {
                throw ParseException("Failed to parse ensemble list: ${ex.message}")
            }
        }

        return ensembleList
    }
}
