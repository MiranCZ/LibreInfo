package com.example.mhdstuff.activity;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.data.DiversionDataHolder;
import com.example.mhdstuff.parsing.types.Diversion;
import com.example.mhdstuff.parsing.types.LineAlias;
import com.example.mhdstuff.parsing.types.Time;
import com.example.mhdstuff.parsing.types.TransportLine;
import com.google.android.flexbox.FlexboxLayout;

public class DiversionInfoActivity extends AppCompatActivity {

//    private final Diversion diversion;

//    public DiversionInfoActivity(Diversion diversion) {
//        this.diversion = diversion;
//    }

    public DiversionInfoActivity() {

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Diversion diversion = DiversionDataHolder.getDiversion();

        setContentView(R.layout.activity_diversion_info);

        TextView view = findViewById(R.id.diversion_title);
        view.setText(diversion.title());

        TextView from = findViewById(R.id.diversion_from);
        from.setText(createSpannable("Od: ", diversion.from()));

        TextView to = findViewById(R.id.diversion_to);
        to.setText(createSpannable("Do: ", diversion.to()));


        FlexboxLayout lines = findViewById(R.id.diversion_line_list);

        for (TransportLine line : diversion.lines()) {
            LineAlias alias = MainActivity.storage.lineStorage().getAlias(line.id());

            lines.addView(alias.createLineIconView(lines, this));
        }

        TextView content = findViewById(R.id.diversion_content);
        content.setText(Html.fromHtml(diversion.publicText(), 0));
    }

    private SpannableString createSpannable(String info, Time time) {
        SpannableString spannable = new SpannableString(info+time.toString());
        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.secondary_color_tone)), 0, info.length(), 0);

        spannable.setSpan(new ForegroundColorSpan(ContextCompat.getColor(this, R.color.secondary_color_light_tone)), info.length(), spannable.length(), 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Typeface font = ResourcesCompat.getFont(this, R.font.roboto_bold);
            if (font != null) {
                spannable.setSpan(new TypefaceSpan(font), info.length(), spannable.length(), 0);
            }
        }

        return spannable;
    }

}
