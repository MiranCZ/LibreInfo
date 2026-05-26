package me.miran.libreinfo.util;

import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

public class HtmlHelper {

    public static Spanned parseHtml(String input) {
        Spanned str = Html.fromHtml(input, 0);

        SpannableStringBuilder ssb = new SpannableStringBuilder(str);
        while (ssb.length() > 0 && ssb.charAt(ssb.length() - 1) == '\n') {
            ssb.delete(ssb.length() - 1, ssb.length());
        }

        return ssb;
    }

}
