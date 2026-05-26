package me.miran.libreinfo.exception;

import android.content.Context;

import me.miran.libreinfo.util.request.Endpoint;

public class RequestException extends AppException {

    public static RequestException reachError(Endpoint endpoint) {
        return new RequestException("Failed to reach the", endpoint);
    }

    public static RequestException parseError(Endpoint endpoint) {
        return new RequestException("Failed to parse", endpoint);
    }

    public static RequestException readError(Endpoint endpoint) {
        return new RequestException("Failed to read from",endpoint);
    }

    public static RequestException timedOutError(Endpoint endpoint, int limitMs) {
        return new RequestException("Timed out after "+limitMs/1000+" seconds from",endpoint);
    }

    private final Endpoint endpoint;

    public RequestException(String message, Endpoint endpoint) {
        super(message);
        this.endpoint = endpoint;
    }

    @Override
    public String getPrettyText(Context context) {
        return super.getPrettyText(context) + " " + endpoint.name.getName(context);
    }
}
