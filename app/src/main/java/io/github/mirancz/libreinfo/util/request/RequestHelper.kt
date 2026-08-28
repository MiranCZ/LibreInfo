package io.github.mirancz.libreinfo.util.request

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import io.github.mirancz.libreinfo.BuildConfig
import io.github.mirancz.libreinfo.R
import io.github.mirancz.libreinfo.exception.AppException
import io.github.mirancz.libreinfo.exception.RequestException
import io.github.mirancz.libreinfo.parsing.types.dto.ReleaseInfoResponse
import io.github.mirancz.libreinfo.parsing.types.dto.StopDelaysResponse
import io.github.mirancz.libreinfo.parsing.types.response.ConnectionsResponse
import io.github.mirancz.libreinfo.parsing.types.response.DataInfoResponse
import io.github.mirancz.libreinfo.parsing.types.response.DiversionsResponse
import io.github.mirancz.libreinfo.parsing.types.response.EventsResponse
import io.github.mirancz.libreinfo.parsing.types.response.NewsResponse
import io.github.mirancz.libreinfo.parsing.types.response.VersionInfoResponse
import io.github.mirancz.libreinfo.parsing.types.response.RouteDelaysResponse
import io.github.mirancz.libreinfo.parsing.types.response.ServerDeparturesResponse
import io.github.mirancz.libreinfo.parsing.types.response.VehicleInfoResponse
import io.github.mirancz.libreinfo.parsing.types.response.VehiclesResponse
import io.github.mirancz.libreinfo.parsing.types.stop.Stop
import io.github.mirancz.libreinfo.parsing.types.stop.StopId
import io.github.mirancz.libreinfo.util.AppLog
import io.github.mirancz.libreinfo.util.IOUtil
import io.github.mirancz.libreinfo.util.Text
import io.github.mirancz.libreinfo.util.json
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.serializer
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL
import java.nio.charset.StandardCharsets

object RequestHelper {


    @Throws(AppException::class)
    @JvmStatic
    fun getLastStaticUpdate(context: Context): Long {
        try {
            val info = makeRequest<DataInfoResponse>(context, Endpoint.STATIC_GTFS.resolve("info"))

            return info.lastUpdated
        } catch (e: IOException) {
            throw AppException(
                Text.translatable(R.string.error_parse, Endpoint.STATIC_GTFS.name),
                e
            )
        }
    }

    @JvmStatic
    @Throws(RequestException::class)
    fun getData(context: Context): InputStream {
        return readUrl(context, Endpoint.STATIC_GTFS.resolve("data"))
    }


    @Throws(RequestException::class)
    fun getNews(context: Context): NewsResponse {
        return makeOwnRequest(context, "news")
    }

    @Throws(RequestException::class)
    fun getEvents(context: Context): EventsResponse {
        return makeOwnRequest(context, "events")
    }

    @Throws(RequestException::class)
    fun getDiversions(context: Context): DiversionsResponse {
        return makeOwnRequest(context, "diversions")
    }

    @Throws(RequestException::class)
    fun getRouteDelays(context: Context): RouteDelaysResponse {
        return makeOwnRequest(context, "routeDelays")
    }

    @JvmStatic
    @Throws(RequestException::class)
    fun getVehicles(context: Context): VehiclesResponse {
        return makeOwnRequest(context, "vehicles")
    }

    @Throws(RequestException::class)
    fun getVehicleInfo(context: Context, lineId: Int, routeId: Int): VehicleInfoResponse {
        return makeOwnRequest(
            context,
            "vehicleInfo?lineId=$lineId&routeId=$routeId"
        )
    }

    @Throws(RequestException::class)
    fun getDepartures(context: Context, stopId: StopId): ServerDeparturesResponse {
        return makeOwnRequest(
            context,
            "departures?stopId=${stopId.original}"
        )
    }

    @Throws(RequestException::class)
    fun getStopDelays(context: Context, stopId: StopId): StopDelaysResponse {
        return makeOwnRequest(
            context,
            "stopDelays?stopId=${stopId.original}"
        )
    }

    @Throws(RequestException::class)
    fun findConnections(
        context: Context,
        fromStop: Stop,
        toStop: Stop,
        time: String?
    ): ConnectionsResponse {
        return makeOwnRequest(
            context,
            "findConnections?fromStop=${fromStop.parentStation}&toStop=${toStop.parentStation}&time=$time"
        )
    }

    @JvmStatic
    @Throws(RequestException::class)
    fun getLatestReleaseInfo(context: Context): ReleaseInfoResponse {
        return makeRequest(
            context,
            Endpoint.GITHUB_API.resolve("repos", "MiranCZ", "LibreInfo", "releases", "latest")
        )
    }

    @JvmStatic
    @Throws(RequestException::class)
    fun getVersionInfo(context: Context, versionUrl: String): VersionInfoResponse {
        return makeRequest(context, Endpoint(versionUrl, Text.literal("Release version meta")))
    }

    @JvmStatic
    @Throws(RequestException::class)
    fun <T> readJsonUrl(
        context: Context,
        URL: String?,
        endpointName: String?,
        deserializer: DeserializationStrategy<T>
    ): T {
        return makeRequest(context, Endpoint(URL, Text.literal(endpointName)), deserializer)
    }

    @JvmStatic
    @Throws(RequestException::class)
    fun readUrl(context: Context, URL: String?, endpointName: String?): InputStream {
        return readUrl(context, Endpoint(URL, Text.literal(endpointName)))
    }


    @Throws(RequestException::class)
    private inline fun <reified T> makeRequest(context: Context, endpoint: Endpoint): T =
        makeRequest(context, endpoint, serializer<T>())

    @Throws(RequestException::class)
    private inline fun <reified T> makeOwnRequest(context: Context, endpoint: String): T =
        makeRequest(context, Endpoint.APP_SERVER.resolve(endpoint))

    @Throws(RequestException::class)
    private fun <T> makeOwnRequest(
        context: Context,
        endpoint: String,
        deserializer: DeserializationStrategy<T>
    ): T {
        val resolved = Endpoint.APP_SERVER.resolve(endpoint)
        return makeRequest(context, resolved, deserializer)
    }

    @Throws(RequestException::class)
    private fun <T> makeRequest(
        context: Context,
        endpoint: Endpoint,
        deserializer: DeserializationStrategy<T>
    ): T {
        try {
            readUrl(context, endpoint).use { stream ->
                val output = String(IOUtil.readAllBytes(stream), StandardCharsets.UTF_8)

                if (output.isBlank()) {
                    throw RequestException.readError(endpoint)
                }

                return json.decodeFromString(deserializer, output)
            }
        } catch (e: SerializationException) {
            AppLog.e("Failed to parse response from " + endpoint.url, e)
            throw RequestException.parseError(endpoint)
        } catch (e: IOException) {
            AppLog.e("IO error reading from " + endpoint.url, e)
            throw RequestException(Text.translatable(R.string.error_read, endpoint.name), e)
        } catch (e: Exception) {
            AppLog.e("Unexpected error reading from " + endpoint.url, e)
            throw RequestException.unknownError(endpoint, e)
        }
    }

    @Throws(RequestException::class)
    private fun readUrl(context: Context, endpoint: Endpoint): InputStream {
        if (!hasNetwork(context)) {
            AppLog.d("assuming network is unreachable for " + endpoint.url)
            throw RequestException.offlineError(endpoint)
        }

        // TODO make this into a setting
        val timeoutMs = 10000
        var con: HttpURLConnection? = null
        try {
            val url = URL(endpoint.url)

            con = url.openConnection() as HttpURLConnection
            con.setRequestProperty("User-Agent", getUserAgent())
            con.setConnectTimeout(timeoutMs)
            con.setReadTimeout(timeoutMs)

            val code = con.getResponseCode()
            if (code >= 400) {
                drainAndClose(con.errorStream)
                con.disconnect()
                throw RequestException.serverError(endpoint, code)
            }

            return con.getInputStream()
        } catch (e: SocketTimeoutException) {
            con?.disconnect()

            throw RequestException.timedOutError(endpoint, timeoutMs)
        } catch (e: IOException) {
            con?.disconnect()

            AppLog.e("Failed to reach " + endpoint.url, e)
            throw RequestException.reachError(endpoint)
        }
    }

    private fun getUserAgent(): String {
        if (BuildConfig.DEBUG) {
            return "LibreInfo-DEBUG"
        }

        return "LibreInfo-v${BuildConfig.VERSION_NAME}-${BuildConfig.FLAVOR}"
    }

    /** Reads and discards an error-stream body so the connection can be released, then closes it.  */
    private fun drainAndClose(stream: InputStream?) {
        if (stream == null) return
        try {
            stream.use {
                IOUtil.readAllBytes(stream)
            }
        } catch (ignored: IOException) {
        }
    }

    private fun hasNetwork(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

        if (cm == null) return true //not sure if its safe to assume wifi is not connected here


        val network = cm.getActiveNetwork()
        if (network == null) return false

        val capabilities = cm.getNetworkCapabilities(network)

        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}