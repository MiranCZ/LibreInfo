package io.github.mirancz.libreinfo.engine

import io.github.mirancz.libreinfo.parsing.storage.manager.IdStorage

interface StorageProvider {

    suspend fun get(): IdStorage

    fun getOrNull(): IdStorage?

}