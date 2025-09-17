package com.example.mhdstuff.parsing.types;

public record Color(int r, int g, int b) {
    public static final Color WHITE = new Color(255, 255, 255);
    public static final Color PINK = new Color(255, 128, 128);
}
