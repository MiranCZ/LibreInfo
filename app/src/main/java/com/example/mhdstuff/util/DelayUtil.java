package com.example.mhdstuff.util;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import com.example.mhdstuff.R;

public class DelayUtil {

    public static SpannableString getDelaySpan(Context context,int delay) {
        String text = getDelayText(context, delay);
        int color = getDelayColor(delay);

        SpannableString spannable = new SpannableString(text);

        spannable.setSpan(new ForegroundColorSpan(color), 0, text.length(), 0);

        return spannable;
    }

    public static String getDelayText(Context context, int delay) {
        if (delay == 0) {
            return context.getString(R.string.on_time);
        }

        return delay + " min";
    }

    public static int getDelayColor(int delay) {
        int color;
        if (delay == 0) {
            color = Color.GREEN;
        } else if (delay < 3) {
            color = 0xFFEED000;
        } else if (delay < 5) {
            color = 0xFFFFA500; //orange
        } else {
            color = Color.RED;
        } /*else {
            color = 0xFF8B0000; //darkred
        }*/
        return color;
    }



}
