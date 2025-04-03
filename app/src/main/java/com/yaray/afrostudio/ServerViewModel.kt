package com.yaray.afrostudio

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.UnknownHostException
import java.util.regex.PatternSyntaxException

private const val TAG = "ServerViewModel"
private const val NETWORK_TIMEOUT = 15000L // 15 seconds

class ServerViewModel : ViewModel() {

    private val _serverResponse = MutableLiveData<ServerResponse>()
    val serverResponse: LiveData<ServerResponse> = _serverResponse

    // Represents the current state of server operations
    data class ServerResponse(
        val action: String = "",
        val responseData: String? = null,
        val isSuccess: Boolean = false,
        val ensembleList: List<String> = emptyList(),
        val errorType: ErrorType = ErrorType.NONE,
        val errorMessage: String? = null
    )

    enum class ErrorType {
        NONE,
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
                val result = withContext(Dispatchers.IO) {
                    withTimeoutOrNull(NETWORK_TIMEOUT) {
                        executeRequest(username, action, ensembleName, ensembleAuthor, ensembleJSON, ensembleUser)
                    }
                }

                if (result == null) {
                    // Timeout occurred
                    _serverResponse.value = ServerResponse(
                        action = action,
                        isSuccess = false,
                        errorType = ErrorType.TIMEOUT,
                        errorMessage = "Request timed out after $NETWORK_TIMEOUT ms"
                    )
                } else {
                    processResponse(action, result)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Unhandled exception in connectToServer: ${e.message}", e)
                _serverResponse.value = ServerResponse(
                    action = action,
                    isSuccess = false,
                    errorType = ErrorType.UNKNOWN,
                    errorMessage = "Unexpected error: ${e.message}"
                )
            }
        }
    }

    private fun executeRequest(
        username: String,
        action: String,
        ensembleName: String?,
        ensembleAuthor: String?,
        ensembleJSON: String?,
        ensembleUser: String?
    ): RequestResult {
        return try {
            val parameters = buildParameters(
                username, action, ensembleName, ensembleAuthor, ensembleJSON, ensembleUser
            )

            // Check if parameters are too long
            if (parameters.length > 65500) {
                return RequestResult.Error(
                    ErrorType.PAYLOAD_TOO_LARGE,
                    "Request payload too large (${parameters.length} bytes)"
                )
            }

            val url = URL("https://new.gis.dp.ua/share_afrostudio.php")
            val connection = url.openConnection() as HttpURLConnection
            try {
                connection.connectTimeout = NETWORK_TIMEOUT.toInt()
                connection.readTimeout = NETWORK_TIMEOUT.toInt()
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.requestMethod = "POST"

                val request = OutputStreamWriter(connection.outputStream)
                request.write(parameters)
                request.flush()
                request.close()

                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return RequestResult.Error(
                        ErrorType.SERVER_ERROR,
                        "Server returned error code: $responseCode"
                    )
                }

                val isr = InputStreamReader(connection.inputStream)
                val reader = BufferedReader(isr)
                val sb = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    sb.append(line).append("\n")
                }
                reader.close()
                isr.close()

                RequestResult.Success(sb.toString())
            } finally {
                connection.disconnect()
            }
        } catch (e: UnknownHostException) {
            Log.e(TAG, "Network unavailable: ${e.message}", e)
            RequestResult.Error(ErrorType.NETWORK_UNAVAILABLE, "Network unavailable")
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Connection timed out: ${e.message}", e)
            RequestResult.Error(ErrorType.TIMEOUT, "Connection timed out")
        } catch (e: IOException) {
            Log.e(TAG, "IO Exception: ${e.message}", e)
            RequestResult.Error(ErrorType.SERVER_ERROR, "Server error: ${e.message}")
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error: ${e.message}", e)
            RequestResult.Error(ErrorType.UNKNOWN, "Unexpected error: ${e.message}")
        }
    }

    private sealed class RequestResult {
        data class Success(val data: String) : RequestResult()
        data class Error(val type: ErrorType, val message: String) : RequestResult()
    }

    private fun buildParameters(
        username: String,
        action: String,
        ensembleName: String?,
        ensembleAuthor: String?,
        ensembleJSON: String?,
        ensembleUser: String?
    ): String {
        return when (action) {
            "register" -> "username=$username&action=$action"
            "setEnsemble" -> "username=$username&action=$action&ensemblename=$ensembleName&ensembleauthor=$ensembleAuthor&ensemblejson=$ensembleJSON"
            "getEnsemble" -> "username=$username&action=$action&ensemblename=$ensembleName&ensembleauthor=$ensembleAuthor&ensembleuser=$ensembleUser"
            "getEnsembleList" -> "username=$username&action=$action"
            "deleteEnsemble" -> "username=$username&action=$action&ensemblename=$ensembleName&ensembleauthor=$ensembleAuthor"
            else -> "username=$username&action=$action"
        }
    }

    private fun processResponse(action: String, result: RequestResult) {
        when (result) {
            is RequestResult.Success -> {
                when (action) {
                    "register" -> {
                        _serverResponse.value = ServerResponse(action, result.data, true)
                    }
                    "setEnsemble" -> {
                        val isSuccess = result.data.contains("setEnsemble_ok")
                        _serverResponse.value = ServerResponse(
                            action = action,
                            responseData = result.data,
                            isSuccess = isSuccess,
                            errorType = if (isSuccess) ErrorType.NONE else ErrorType.SERVER_ERROR,
                            errorMessage = if (isSuccess) null else "Server did not return success confirmation"
                        )
                    }
                    "getEnsemble" -> {
                        val isSuccess = result.data.isNotBlank() && !result.data.contains("error")
                        _serverResponse.value = ServerResponse(
                            action = action,
                            responseData = result.data,
                            isSuccess = isSuccess,
                            errorType = if (isSuccess) ErrorType.NONE else ErrorType.SERVER_ERROR,
                            errorMessage = if (isSuccess) null else "Server returned empty or error response"
                        )
                    }
                    "getEnsembleList", "deleteEnsemble" -> {
                        try {
                            val ensembleList = parseEnsembleList(result.data)
                            _serverResponse.value = ServerResponse(
                                action = action,
                                responseData = result.data,
                                isSuccess = true,
                                ensembleList = ensembleList
                            )
                            Log.d(TAG, "Ensemble list retrieved: ${ensembleList.size} items")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error parsing ensemble list: ${e.message}", e)
                            _serverResponse.value = ServerResponse(
                                action = action,
                                responseData = result.data,
                                isSuccess = false,
                                errorType = ErrorType.PARSE_ERROR,
                                errorMessage = "Failed to parse server response: ${e.message}"
                            )
                        }
                    }
                }
            }
            is RequestResult.Error -> {
                _serverResponse.value = ServerResponse(
                    action = action,
                    isSuccess = false,
                    errorType = result.type,
                    errorMessage = result.message
                )
            }
        }
    }

    private fun parseEnsembleList(response: String?): List<String> {
        val ensembleList = mutableListOf<String>()

        if (response != null && response.isNotBlank()) {
            try {
                val splitArray = response.split("+")
                for (item in splitArray) {
                    if (item.isNotBlank() && item.contains("_by_")) {
                        ensembleList.add(item)
                    }
                }
            } catch (ex: PatternSyntaxException) {
                Log.e(TAG, "Error parsing ensemble list: ${ex.message}", ex)
                throw ex
            }
        }

        return ensembleList
    }
}
