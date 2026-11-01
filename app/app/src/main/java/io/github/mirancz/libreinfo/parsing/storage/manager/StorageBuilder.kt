package io.github.mirancz.libreinfo.parsing.storage.manager

import android.content.Context
import io.github.mirancz.libreinfo.exception.AppException
import io.github.mirancz.libreinfo.exception.StorageInitException
import io.github.mirancz.libreinfo.parsing.storage.ApiStorage
import io.github.mirancz.libreinfo.parsing.storage.AppStorage
import io.github.mirancz.libreinfo.parsing.storage.CalendarStorage
import io.github.mirancz.libreinfo.parsing.storage.LineStorage
import io.github.mirancz.libreinfo.parsing.storage.PostStorage
import io.github.mirancz.libreinfo.parsing.storage.RouteStopStorage
import io.github.mirancz.libreinfo.parsing.storage.StopStorage
import io.github.mirancz.libreinfo.parsing.storage.TripStorage
import io.github.mirancz.libreinfo.util.AppLog

class StorageBuilder(val context: Context, val consumer: ((AppStorage) -> Unit)? = null) {


    @Throws(StorageInitException::class)
    fun build(manager: StorageManager): IdStorage {
        try {
            return buildInternal(manager)
        } catch (e: AppException) {
            throw StorageInitException(e)
        }
    }

    @Suppress("UsePropertyAccessSyntax")
    @Throws(AppException::class)
    private fun buildInternal(manager: StorageManager): IdStorage {
        AppLog.d("Initializing...")
        val ms = System.currentTimeMillis()

        fun <T : AppStorage> onLoaded(instance: T) {
            AppLog.d("Initialized ${instance.javaClass.simpleName} in ${System.currentTimeMillis()-ms}ms")

            consumer?.invoke(instance)
        }

        val stopStorage = manager.useStops {
            StopStorage.parse(it)
        }
        onLoaded(stopStorage)

        val lineStorage = manager.useLineAliases(LineStorage::parse)
        onLoaded(lineStorage)

        val postStorage = manager.usePosts { PostStorage.parse(it, stopStorage) }
        onLoaded(postStorage)

        val apiStorage = manager.useApi(ApiStorage::parse)
        onLoaded(apiStorage)

        val tripStorage = manager.useTrips(TripStorage::parse)
        onLoaded(tripStorage)

        val routeStopStorage = manager.useStopTimes {
            RouteStopStorage.parse(
                it, manager.getRouteStopsBuff(), stopStorage.stopsLengths
            )
        }

        val calendarStorage = manager.useCalendar { calendar ->
            manager.useCalendarDates { dates ->
                CalendarStorage.parse(
                    calendar, dates
                )
            }
        }
        onLoaded(calendarStorage)

        AppLog.d("Initialized in " + (System.currentTimeMillis() - ms) + "ms")

        return IdStorage(
            lineStorage,
            stopStorage,
            postStorage,
            tripStorage,
            routeStopStorage,
            calendarStorage,
            apiStorage
        )
    }

}