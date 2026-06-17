package me.miran.libreinfo.exception;

import me.miran.libreinfo.R;
import me.miran.libreinfo.util.Text;
import me.miran.libreinfo.util.request.Endpoint;

public class RequestException extends AppException {

    public static RequestException offlineError(Endpoint endpoint) {
        return new RequestException(Text.translatable(R.string.error_reach, endpoint.name), ErrorType.OFFLINE);
    }

    public static RequestException reachError(Endpoint endpoint) {
        return new RequestException(Text.translatable(R.string.error_reach, endpoint.name), ErrorType.SERVER);
    }

    public static RequestException parseError(Endpoint endpoint) {
        return new RequestException(Text.translatable(R.string.error_parse, endpoint.name), ErrorType.PARSE);
    }

    public static RequestException readError(Endpoint endpoint) {
        return new RequestException(Text.translatable(R.string.error_read, endpoint.name), ErrorType.PARSE);
    }

    public static RequestException timedOutError(Endpoint endpoint, int limitMs) {
        return new RequestException(Text.translatable(R.string.error_timeout, limitMs / 1000, endpoint.name), ErrorType.SERVER);
    }

    public static RequestException serverError(Endpoint endpoint, int code) {
        return new RequestException(Text.translatable(R.string.error_server, code, endpoint.name), ErrorType.SERVER);
    }

    public static RequestException unknownError(Endpoint endpoint, Throwable cause) {
        RequestException e = new RequestException(Text.translatable(R.string.error_unknown, endpoint.name), cause);
        e.type = ErrorType.GENERIC;
        return e;
    }

    public RequestException(Text text) {
        super(text);
    }

    public RequestException(Text text, ErrorType type) {
        super(text);
        this.type = type;
    }

    public RequestException(Text text, Throwable cause) {
        super(text, cause);
    }
}
