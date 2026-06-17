package me.miran.libreinfo.util;

import android.content.Context;

import androidx.core.content.ContextCompat;

public interface Text {

    static Text literal(String text) {
        return new LiteralText(text);
    }

    static Text translatable(int id) {
        return new TranslatableText(id, new Object[0]);
    }

    /**
     * A translatable string with format arguments. Any argument that is itself a {@link Text}
     * (e.g. an endpoint name) is resolved against the context before formatting, so nested
     * translatable pieces stay translatable.
     */
    static Text translatable(int id, Object... args) {
        return new TranslatableText(id, args);
    }

    String getName(Context context);

    class LiteralText implements Text{
        private final String text;

        LiteralText(String text) {
            this.text = text;
        }

        @Override
        public String getName(Context context) {
            return text;
        }
    }

    class TranslatableText implements Text {
        private final int id;
        private final Object[] args;

        TranslatableText(int id, Object[] args) {
            this.id = id;
            this.args = args;
        }

        @Override
        public String getName(Context context) {
            if (args.length == 0) {
                return ContextCompat.getString(context, id);
            }

            Object[] resolved = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                resolved[i] = (arg instanceof Text text) ? text.getName(context) : arg;
            }
            return context.getString(id, resolved);
        }
    }

}
