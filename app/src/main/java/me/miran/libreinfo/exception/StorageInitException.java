package me.miran.libreinfo.exception;

import me.miran.libreinfo.parsing.storage.IdStorage;

/**
 * Thrown by {@link me.miran.libreinfo.parsing.storage.IdStorage IdStorage} if data load failed
 * and one of {@link IdStorage#getInstance() getInstance()} methods is called
 */
public class StorageInitException extends RuntimeException {

    public StorageInitException(AppException cause) {
        super(cause);
    }

    public AppException appException() {
        return (AppException) getCause();
    }
}
