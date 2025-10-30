package com.example.mhdstuff.activity;

import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.base.BaseActivity;
import com.example.mhdstuff.parsing.storage.IdStorage;
import com.example.mhdstuff.parsing.types.Diversion;
import com.example.mhdstuff.parsing.types.LineAlias;
import com.example.mhdstuff.parsing.types.DateTime;
import com.google.android.flexbox.FlexboxLayout;

public class DiversionInfoActivity extends BaseActivity {


    public DiversionInfoActivity() {
        super(R.string.diversions, R.layout.activity_diversion_info);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IdStorage.getInstanceOnUIThread(this::createElements, this);
    }

    private void createElements(IdStorage storage) {
        Diversion diversion = getIntent().getParcelableExtra("diversion");

        TextView view = findViewById(R.id.diversion_title);
        view.setText(diversion.title());

        TextView from = findViewById(R.id.diversion_from);
        from.setText(createSpannable("Od: ", diversion.from()));

        TextView to = findViewById(R.id.diversion_to);
        to.setText(createSpannable("Do: ", diversion.to()));


        FlexboxLayout lines = findViewById(R.id.diversion_line_list);

        for (LineAlias alias : diversion.lines()) {
            lines.addView(alias.createLineIconView(lines, this));
        }

        TextView content = findViewById(R.id.diversion_content);

        CharSequence str = Html.fromHtml(diversion.publicText(), 0);
        content.setText(str.toString().stripTrailing());
    }

    private SpannableString createSpannable(String info, DateTime time) {
        if (time == null || time == DateTime.NONE) {
            return new SpannableString("");
        }

        SpannableString spannable = new SpannableString(info+time);
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
