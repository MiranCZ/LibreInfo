package io.github.mirancz.libreinfo.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller

/**
 * Receives status callbacks from a [PackageInstaller] session started by [ApkInstaller]
 */
class InstallResultReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)

        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                ApkInstaller.finishPreparing()
                val confirm = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)

                if (confirm != null) {
                    confirm.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(confirm)
                } else {
                    AppLog.w("Install pending user action but no confirm intent was provided")
                }
            }
            PackageInstaller.STATUS_SUCCESS -> {
                ApkInstaller.finishPreparing()
                AppLog.d("Update installed successfully")
            }
            // The user canceled the system prompt
            PackageInstaller.STATUS_FAILURE_ABORTED -> {
                ApkInstaller.finishPreparing()
                AppLog.d("Update install aborted")
            }
            else -> {
                AppLog.w("Update install failed (status=$status): $message")
                ApkInstaller.reportFailure("status=$status\n${message ?: ""}")
            }
        }
    }
}
