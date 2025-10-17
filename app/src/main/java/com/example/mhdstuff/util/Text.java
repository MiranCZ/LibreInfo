package com.example.mhdstuff.util;

import android.content.Context;

import androidx.core.content.ContextCompat;

import com.example.mhdstuff.activity.base.BaseActivity;

public interface Text {

    static Text literal(String text) {
        return new LiteralText(text);
    }

    static Text translatable(int id) {
        return new TranslatableText(id);
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

        TranslatableText(int id) {
            this.id = id;
        }

        @Override
        public String getName(Context context) {
            return ContextCompat.getString(context, id);
        }
    }

}
