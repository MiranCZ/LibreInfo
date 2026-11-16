package io.github.mirancz.libreinfo.model.types;

/**
 * {@link java.awt.Color} is not supported in Android and androids colors is not (surprisingly) present in normal Java.
 * <p>
 *
 * So we make a third class yeppiee
 */
public class Color {

    private final int argb;

    public Color(int argb) {
        this.argb = argb;
    }

    public Color(int r, int g, int b) {
        this(((255) << 24 | (r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF) << 0));
    }

    public int getRgb() {
        return argb;
    }

    public int getRed() {
        return (argb >> 16) & 0xFF;
    }

    public int getGreen() {
        return (argb >> 8) & 0xFF;
    }

    public int getBlue() {
        return (argb >> 0) & 0xFF;
    }

}
