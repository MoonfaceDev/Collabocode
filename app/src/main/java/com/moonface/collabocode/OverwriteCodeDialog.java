package com.moonface.collabocode;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import java.util.Objects;

public class OverwriteCodeDialog extends Dialog implements View.OnClickListener {

    private OnOkPressedListener listener;
    private String title;

    OverwriteCodeDialog(Activity activity, String title, OnOkPressedListener listener) {
        super(activity);
        this.listener = listener;
        this.title = title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_overwrite_code);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button pos = findViewById(R.id.positiveButton);
        Button neg = findViewById(R.id.negativeButton);
        pos.setOnClickListener(this);
        neg.setOnClickListener(this);

        ((TextView)findViewById(R.id.dialog_message)).setText(getContext().getString(R.string.overwrite_dialog_message, title));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.positiveButton:
                listener.onButtonClick();
                dismiss();
                break;
            case R.id.negativeButton:
                dismiss();
                break;
            default:
                dismiss();
                break;
        }
    }

    public interface OnOkPressedListener {
        void onButtonClick();
    }
}

