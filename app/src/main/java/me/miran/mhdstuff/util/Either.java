package me.miran.mhdstuff.util;

import java.util.Optional;

public abstract class Either<L, R> {
    public static <L, R> Either<L, R> left(L value) {
        return new Left<>(value);
    }

    public static <L, R> Either<L, R> right(R value) {
        return new Right<>(value);
    }


    public static class Left<L, R> extends Either<L, R> {

        public L left;
        Left(L left) {
            this.left = left;
        }

    }

    public static class Right<L, R> extends Either<L, R> {

        public R right;
        Right(R right) {
            this.right = right;
        }

    }

}
