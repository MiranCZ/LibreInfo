package com.example.mhdstuff.exception;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Looper;

import androidx.appcompat.view.ContextThemeWrapper;

import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.DiversionsActivity;
import com.example.mhdstuff.activity.base.BaseActivity;
import com.example.mhdstuff.util.Text;

public class AppException extends Exception {

    private final Text text;
    public AppException(String message) {
        this(Text.literal(message));
    }

    public AppException(int stringId) {
        this(Text.translatable(stringId));
    }

    public AppException(Text text) {
        super();
        this.text = text;
    }

    public String getPrettyText(Context context) {
        return text.getName(context);
    }

    public void showErrPopup(BaseActivity activity) {
        activity.runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.AlertDialogStyle));

            builder.setMessage(getPrettyText(activity))
                    .setTitle("Error");

            AlertDialog dialog = builder.create();
            dialog.show();
        });
    }
}
