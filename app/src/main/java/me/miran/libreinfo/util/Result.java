package me.miran.libreinfo.util;

public abstract class Result<T, E> {
    public static <T, E> Result<T, E> ok(T value) {
        return new Ok<>(value);
    }

    public static <L, R> Result<L, R> err(R value) {
        return new Err<>(value);
    }

    public T getOr(T defaultValue) {
        if (this instanceof Ok<T, E> ok) {
            return ok.value;
        }
        return defaultValue;
    }


    public static class Ok<T, E> extends Result<T, E> {

        public T value;
        private Ok(T value) {
            this.value = value;
        }

    }

    public static class Err<T, E> extends Result<T, E> {

        public E err;
        private Err(E err) {
            this.err = err;
        }

    }

}
