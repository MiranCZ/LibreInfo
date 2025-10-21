package com.example.mhdstuff.exception;

import android.app.AlertDialog;
import android.content.Context;

import androidx.appcompat.view.ContextThemeWrapper;

import com.example.mhdstuff.R;
import com.example.mhdstuff.activity.base.BaseActivity;
import com.example.mhdstuff.util.Text;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

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


    public void showError(BaseActivity activity) {
        showError(activity, NotificationType.POPUP);
    }

    public void showError(BaseActivity activity, NotificationType type) {
        activity.runOnUiThread(() -> {
            switch (type) {
                case POPUP -> showPopup(activity);
                case SNACK_BAR -> showSnackBar(activity);
            }


        });
    }

    private void showSnackBar(BaseActivity activity) {
        Snackbar snackbar = Snackbar.make(activity.getContentView(), getPrettyText(activity), BaseTransientBottomBar.LENGTH_SHORT);
        snackbar.show();
    }

    private void showPopup(BaseActivity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(activity, R.style.AlertDialogStyle));

        builder.setMessage(getPrettyText(activity))
                .setTitle("Error");

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public enum NotificationType {
        POPUP, SNACK_BAR
    }

}
