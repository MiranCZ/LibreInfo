package io.github.mirancz.libreinfo.util

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import java.io.File
import java.io.IOException

object ApkInstaller {


    var lastError by mutableStateOf<String?>(null)
        private set

    var preparing by mutableStateOf(false)
        private set

    fun reportFailure(detail: String?) {
        preparing = false
        lastError = detail
    }

    fun clearError() {
        lastError = null
    }

    fun finishPreparing() {
        preparing = false
    }

    fun installAllowed(context: Context): Boolean =
        context.packageManager.canRequestPackageInstalls()

    fun unknownSourcesIntent(context: Context): Intent =
        Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            "package:${context.packageName}".toUri()
        )

    fun launchInstall(context: Context) {
        val appContext = context.applicationContext
        val apk = UpdateDownloader.getUpdateFilePath(appContext).toFile()

        preparing = true
        Thread {
            try {
                installApk(appContext, apk)
            } catch (e: IOException) {
                AppLog.e("Failed to start update install session", e)
                reportFailure(e.message)
            }
        }.start()
    }

    @SuppressLint("RequestInstallPackagesPolicy")
    private fun installApk(context: Context, apk: File) {
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)

        val sessionId = installer.createSession(params)
        installer.openSession(sessionId).use { session ->
            apk.inputStream().use { input ->
                session.openWrite("update.apk", 0, apk.length()).use { output ->
                    input.copyTo(output)
                    session.fsync(output)
                }
            }
            session.commit(statusSender(context, sessionId).intentSender)
        }
    }

    private fun statusSender(context: Context, sessionId: Int): PendingIntent {
        val intent = Intent(context, InstallResultReceiver::class.java)
        val mutability =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        return PendingIntent.getBroadcast(
            context,
            sessionId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or mutability
        )
    }
}
