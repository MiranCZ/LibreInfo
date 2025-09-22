package com.example.mhdstuff.util.request.soap;

import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SoapSaneObject implements Iterable<Object>{


    public static SoapSaneObject parse(SoapObject soapObject) {
        Map<String, Object> map = new HashMap<>();

        for (int i = 0; i < soapObject.getPropertyCount(); i++) {
            String name = soapObject.getPropertyInfo(i).name;
            map.put(name, soapObject.getProperty(i));
        }

        return new SoapSaneObject(soapObject, map);
    }

    private final SoapObject original;
    private final Map<String, Object> map;

    private SoapSaneObject(SoapObject original,Map<String, Object> map) {
        this.original = original;
        this.map = map;
    }

    public Object get(String name) {
        return map.get(name);
    }

    public String getString(String name) {
        return getType(name);
    }

    public Integer getInt(String name) {
        try {
            return getType(name);
        } catch (ClassCastException ignored) {
        }

        String value = getType(name);
        if (value == null) return null;

        return Integer.parseInt(value);
    }

    public Boolean getBoolean(String name) {
        try {
            return getType(name);
        } catch (ClassCastException ignored) {
        }

        String value = getType(name);
        if (value == null) return null;

        return Boolean.parseBoolean(value);
    }

    public SoapObject getSoupObject(String name) {
        return getType(name);
    }

    public SoapSaneObject getSoapSaneObject(String name) {
        SoapObject soapObject = getSoupObject(name);
        if (soapObject == null) return null;

        return SoapSaneObject.parse(soapObject);
    }

    public <T> T getType(String name) {
        Object value = get(name);
        if (value == null) return null;

        if (value instanceof SoapPrimitive primitive) {
            //noinspection unchecked
            return (T) primitive.getValue();
        }

        //noinspection unchecked
        return (T) value;
    }

    public SoapObject getOriginal() {
        return original;
    }

    @Override
    public String toString() {
        return "SoapSaneObject{" +
                "original=" + original +
                '}';
    }

    @Override
    public Iterator<Object> iterator() {
        return new Iterator<>() {
            int index = 0;

            @Override
            public boolean hasNext() {
                return index < original.getPropertyCount();
            }

            @Override
            public Object next() {
                return original.getProperty(index++);
            }
        };
    }

}
