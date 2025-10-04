package com.example.mhdstuff.util;

/**
 * Helper class to contain something
 * <p>
 * can be useful when "final" field is needed in an lambda or if you need a reference to a primitive
 * @param <T>
 */
public class Container<T> {


    public T item;

    public Container() {
        item = null;
    }

    public Container(T item) {
        this.item = item;
    }

}
