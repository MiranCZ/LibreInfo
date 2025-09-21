package com.example.mhdstuff.parsing.types;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.mhdstuff.R;
import com.example.mhdstuff.parsing.storage.LineStorage;
import com.google.gson.JsonObject;

public record LineAlias(int id, String lineDisplayName, int backgroundColor, String backgroundColorStr, int textColor, String textColorStr) {

    public static LineAlias parse(JsonObject obj) {
        if (obj == null) return null;

        int id = obj.get("LineID").getAsInt();
        String displayName = obj.get("LineName").getAsString();

        String backgroundStr = obj.get("Color").getAsString();
        int background = Color.parseColor(backgroundStr);
        String textColorStr = obj.get("TextColor").getAsString();
        int textColor = Color.parseColor(textColorStr);

        return new LineAlias(id, displayName, background, backgroundStr, textColor, textColorStr);
    }

    public static LineAlias parse(String str, LineStorage storage) {
        if (str == null) return null;
        str = str.strip();

        if (TypeHelper.isInteger(str)) {
            int id = Integer.parseInt(str);

            return storage.getAlias(id);
        }

        return storage.getAlias(str);
    }

    public TransportLine toTransportLine() {
        return new TransportLine(id, lineDisplayName);
    }

    public View createLineIconView(ViewGroup parent, Context context) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.line_icon_layout, parent , false);
        TextView title = itemView.findViewById(R.id.line_name);
        title.setText(lineDisplayName());
        title.setTextColor(textColor());

        View view = itemView.findViewById(R.id.icon_container);
        GradientDrawable back = (GradientDrawable) view.getBackground();
        back.setColor(backgroundColor());

        return itemView;
    }

}
