package com.yaray.afrostudio

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.regex.PatternSyntaxException

class ServerViewModel : ViewModel() {

    private val _serverResponse = MutableLiveData<ServerResponse>()
    val serverResponse: LiveData<ServerResponse> = _serverResponse

    // Represents the current state of server operations
    data class ServerResponse(
        val action: String = "",
        val responseData: String? = null,
        val isSuccess: Boolean = false,
        val ensembleList: List<String> = emptyList()
    )

    fun connectToServer(
        username: String,
        action: String,
        ensembleName: String? = null,
        ensembleAuthor: String? = null,
        ensembleJSON: String? = null,
        ensembleUser: String? = null
    ) {
        viewModelScope.launch {
            val response = withContext(Dispatchers.IO) {
                try {
                    val parameters = buildParameters(
                        username,
                        action,
                        ensembleName,
                        ensembleAuthor,
                        ensembleJSON,
                        ensembleUser
                    )

                    // Check if parameters are too long
                    if (parameters.length > 65500) {
                        return@withContext "PostTooLong"
                    }

                    val url = URL("https://new.gis.dp.ua/share_afrostudio.php")
                    val connection = url.openConnection() as HttpURLConnection
                    connection.doOutput = true
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                    connection.requestMethod = "POST"

                    val request = OutputStreamWriter(connection.outputStream)
                    request.write(parameters)
                    request.flush()
                    request.close()

                    val isr = InputStreamReader(connection.inputStream)
                    val reader = BufferedReader(isr)
                    val sb = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        sb.append(line).append("\n")
                    }
                    reader.close()
                    isr.close()

                    sb.toString()
                } catch (e: IOException) {
                    null
                }
            }

            processResponse(action, response)
        }
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

    private fun processResponse(action: String, response: String?) {
        when (action) {
            "register" -> {
                _serverResponse.value = ServerResponse(action, response, response != null)
            }
            "setEnsemble" -> {
                _serverResponse.value = ServerResponse(action, response, response != null)
            }
            "getEnsemble" -> {
                _serverResponse.value = ServerResponse(action, response, response != null)
            }
            "getEnsembleList", "deleteEnsemble" -> {
                val ensembleList = parseEnsembleList(response)
                _serverResponse.value = ServerResponse(action, response, response != null, ensembleList)
                if (response != null) {
                    Log.e(
                        "Ext",
                        response
                    )
                }
            }
        }
    }

    private fun parseEnsembleList(response: String?): List<String> {
        val ensembleList = mutableListOf<String>()

        if (response != null) {
            try {
                val splitArray = response.split("+")
                for (i in 0 until splitArray.size - 1) {
                    ensembleList.add(splitArray[i])
                }
            } catch (ex: PatternSyntaxException) {
                // Handle exception
                Log.e(
                    "Ext",
                    "bad enemble list"
                )

            }
        }

        return ensembleList
    }
}