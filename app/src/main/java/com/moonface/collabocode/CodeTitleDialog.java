package com.moonface.collabocode;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.Objects;

public class CodeTitleDialog extends Dialog implements View.OnClickListener {

    private EditText titleView;
    private String title;
    private OnDonePressedListener listener;

    CodeTitleDialog(Activity activity, String title, OnDonePressedListener listener) {
        super(activity);
        this.listener = listener;
        this.title = title;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_code_title);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button pos = findViewById(R.id.positiveButton);
        Button neg = findViewById(R.id.negativeButton);
        pos.setOnClickListener(this);
        neg.setOnClickListener(this);
        titleView = findViewById(R.id.title_view);
        titleView.setText(title);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.positiveButton:
                String title = titleView.getText().toString();
                boolean canDone = true;
                String toast = null;
                if(title.length() == 0){
                    toast = getContext().getString(R.string.title_empty_toast);
                    canDone = false;
                }
                if(canDone){
                    listener.onButtonClick(title);
                    dismiss();
                } else {
                    Toast.makeText(getContext(), toast, Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.negativeButton:
                dismiss();
                break;
            default:
                dismiss();
                break;
        }
    }

    public interface OnDonePressedListener {
        void onButtonClick(String title);
    }
}

