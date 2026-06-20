package me.miran.libreinfo.parsing.storage.manager

import android.os.Looper
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import me.miran.libreinfo.exception.AppException
import me.miran.libreinfo.exception.StorageInitException
import me.miran.libreinfo.parsing.storage.AppStorage
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass


class StorageProvider {

    @PublishedApi
    internal val storageMap = ConcurrentHashMap<Class<out AppStorage>, CompletableDeferred<AppStorage>>()
    @Volatile
    private var failure: AppException? = null

    fun fail(e: AppException) {
        failure = e
        val ex = StorageInitException.runtime(e)
        storageMap.values.forEach { it.completeExceptionally(ex) }
    }

    fun error(): AppException? = failure

    suspend fun getInstance() : IdStorage {
        return get(IdStorage::class)
    }

    fun getInstanceOrNull() : IdStorage? {
        return getOrNull(IdStorage::class)
    }

    suspend fun <T : AppStorage> get(clazz: KClass<T>): T {
        return get(clazz.java);
    }

    suspend fun <T : AppStorage> get(clazz: Class<T>): T {
        if (Looper.getMainLooper().isCurrentThread) {
            throw IllegalStateException("Blocking storage access must not be called from the main thread")
        }
        if (failure != null) {
            throw failure!!
        }

        val deferred = storageMap.getOrPut(clazz) { CompletableDeferred() }

        @Suppress("UNCHECKED_CAST")
        return deferred.await() as T
    }

    fun <T : AppStorage> getOrNull(clazz: KClass<T>): T? {
        return getOrNull(clazz.java)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun <T : AppStorage> getOrNull(clazz: Class<T>): T? {
        val deferred = storageMap[clazz]

        if (deferred != null && deferred.isCompleted && !deferred.isCancelled && deferred.getCompletionExceptionOrNull() == null) {
            @Suppress("UNCHECKED_CAST")

            return deferred.getCompleted() as T
        }

        return null
    }

    fun <T : AppStorage> update(instance: T) {
        val clazz = instance::class.java
        val existing = storageMap[clazz]

        if (existing != null && !existing.isCompleted) {
            existing.complete(instance)
        } else {
            storageMap[clazz] = CompletableDeferred(instance)
        }

        // make sure all children are updated as well
        if (instance is IdStorage) {
            update(instance.lineStorage)
            update(instance.stopStorage)
            update(instance.postStorage)
            update(instance.tripStorage)
            update(instance.routeStopStorage)
            update(instance.calendarStorage)
            update(instance.apiStorage)
            update(instance.stopMapper)
        }
    }

    // Java interop
    fun <T : AppStorage> getBlocking(clazz: Class<T>): T {
        if (failure != null) {
            throw failure!!
        }

        return try {
            runBlocking {
                val deferred = storageMap.getOrPut(clazz) { CompletableDeferred() }

                @Suppress("UNCHECKED_CAST")
                deferred.await() as T
            }
        } catch (e: InterruptedException) {
            throw RuntimeException("Thread was interrupted while waiting for ${clazz.simpleName}", e)
        }
    }

}
