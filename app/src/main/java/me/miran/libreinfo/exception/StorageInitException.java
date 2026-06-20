package me.miran.libreinfo.exception;

import me.miran.libreinfo.parsing.storage.manager.IdStorage;

/**
 * Thrown by {@link IdStorage IdStorage} if data load failed
 * and one of {@link IdStorage#getInstance() getInstance()} methods is called
 */
public class StorageInitException extends Exception {

    public static RuntimeStorageInitException runtime(AppException cause) {
        return new RuntimeStorageInitException(new StorageInitException(cause));
    }

    public StorageInitException(AppException cause) {
        super(cause);
    }

    public AppException appException() {
        return (AppException) getCause();
    }

    public static class RuntimeStorageInitException extends RuntimeException {
        public RuntimeStorageInitException(StorageInitException cause) {
            super(cause);
        }
    }

}
