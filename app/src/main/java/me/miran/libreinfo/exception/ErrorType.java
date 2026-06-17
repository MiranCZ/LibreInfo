package me.miran.libreinfo.exception;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import me.miran.libreinfo.R;

/**
 * The kind of failure behind an {@link AppException}, used by the error UI to pick a distinct
 * icon, heading and retry affordance per category instead of showing one generic error screen.
 */
public enum ErrorType {
    /** No network connectivity at all. */
    OFFLINE(R.drawable.wifi_slash, R.string.error_offline_title, true),
    /** The server couldn't be reached, timed out, or returned an error status. */
    SERVER(R.drawable.server_solid, R.string.error_server_title, true),
    /** A response was received but couldn't be read or parsed into something usable. */
    PARSE(R.drawable.file_circle_exclamation, R.string.error_parse_title, true),
    /** The locally cached static data is missing or corrupt. */
    DATA(R.drawable.file_circle_exclamation, R.string.error_data_title, false),
    /** Anything that doesn't fit a more specific category. */
    GENERIC(R.drawable.triangle_exclamation_regular, R.string.generic_error, true);

    @DrawableRes
    public final int icon;
    @StringRes
    public final int title;
    public final boolean retryable;

    ErrorType(@DrawableRes int icon, @StringRes int title, boolean retryable) {
        this.icon = icon;
        this.title = title;
        this.retryable = retryable;
    }
}
