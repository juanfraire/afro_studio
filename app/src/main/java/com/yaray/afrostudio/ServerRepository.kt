package com.yaray.afrostudio

import android.util.Log
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.net.URLEncoder
import java.net.UnknownHostException

// Define data classes for requests and responses
data class RequestParams(
    val username: String,
    val action: String,
    val ensembleName: String? = null,
    val ensembleAuthor: String? = null,
    val ensembleJSON: String? = null,
    val ensembleUser: String? = null
)

interface ServerRepository {
    suspend fun executeRequest(params: RequestParams): Result<String>
}

class ServerRepositoryImpl(
    private val baseUrl: String = "https://new.gis.dp.ua/share_afrostudio.php",
    private val timeout: Long = 15000L,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ServerRepository {

    override suspend fun executeRequest(params: RequestParams): Result<String> = withContext(dispatcher) {
        try {
            // Check if parameters are too long
            val parameters = buildParameters(params)
            if (parameters.length > 65500) {
                return@withContext Result.failure(
                    PayloadTooLargeException("Request payload too large (${parameters.length} bytes)")
                )
            }

            val url = URL(baseUrl)
            val connection = url.openConnection() as HttpURLConnection

            try {
                connection.connectTimeout = timeout.toInt()
                connection.readTimeout = timeout.toInt()
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.requestMethod = "POST"

                val request = OutputStreamWriter(connection.outputStream)
                request.write(parameters)
                request.flush()
                request.close()

                val responseCode = connection.responseCode
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return@withContext Result.failure(
                        ServerException("Server returned error code: $responseCode")
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

                return@withContext Result.success(sb.toString())
            } finally {
                connection.disconnect()
            }
        } catch (e: UnknownHostException) {
            Log.e("ServerRepository", "Network unavailable: ${e.message}", e)
            return@withContext Result.failure(NetworkUnavailableException("Network unavailable"))
        } catch (e: SocketTimeoutException) {
            Log.e("ServerRepository", "Connection timed out: ${e.message}", e)
            return@withContext Result.failure(TimeoutException("Connection timed out after $timeout ms"))
        } catch (e: IOException) {
            Log.e("ServerRepository", "IO Exception: ${e.message}", e)
            return@withContext Result.failure(ServerException("Server error: ${e.message}"))
        } catch (e: Exception) {
            Log.e("ServerRepository", "Unexpected error: ${e.message}", e)
            return@withContext Result.failure(e)
        }
    }

    private fun buildParameters(params: RequestParams): String {
        return when (params.action) {
            "register" -> "username=${URLEncoder.encode(params.username, "UTF-8")}&action=${params.action}"
            "setEnsemble" -> "username=${URLEncoder.encode(params.username, "UTF-8")}&action=${params.action}" +
                    "&ensemblename=${URLEncoder.encode(params.ensembleName ?: "", "UTF-8")}" +
                    "&ensembleauthor=${URLEncoder.encode(params.ensembleAuthor ?: "", "UTF-8")}" +
                    "&ensemblejson=${URLEncoder.encode(params.ensembleJSON ?: "", "UTF-8")}"
            "getEnsemble" -> "username=${URLEncoder.encode(params.username, "UTF-8")}&action=${params.action}" +
                    "&ensemblename=${URLEncoder.encode(params.ensembleName ?: "", "UTF-8")}" +
                    "&ensembleauthor=${URLEncoder.encode(params.ensembleAuthor ?: "", "UTF-8")}" +
                    "&ensembleuser=${URLEncoder.encode(params.ensembleUser ?: "", "UTF-8")}"
            "getEnsembleList" -> "username=${URLEncoder.encode(params.username, "UTF-8")}&action=${params.action}"
            "deleteEnsemble" -> "username=${URLEncoder.encode(params.username, "UTF-8")}&action=${params.action}" +
                    "&ensemblename=${URLEncoder.encode(params.ensembleName ?: "", "UTF-8")}" +
                    "&ensembleauthor=${URLEncoder.encode(params.ensembleAuthor ?: "", "UTF-8")}"
            else -> "username=${URLEncoder.encode(params.username, "UTF-8")}&action=${params.action}"
        }
    }
}

// Custom exceptions for better error handling
class NetworkUnavailableException(message: String) : IOException(message)
class TimeoutException(message: String) : IOException(message)
class ServerException(message: String) : IOException(message)
class PayloadTooLargeException(message: String) : IOException(message)
class ParseException(message: String) : Exception(message)
