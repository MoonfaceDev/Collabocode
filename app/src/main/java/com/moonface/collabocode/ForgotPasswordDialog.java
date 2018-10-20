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

public class ForgotPasswordDialog extends Dialog implements android.view.View.OnClickListener {

    private EditText emailView;
    private OnSentPressedListener listener;

    ForgotPasswordDialog(Activity activity, OnSentPressedListener listener) {
        super(activity);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.forgot_password_dialog);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button pos = findViewById(R.id.positiveButton);
        Button neg = findViewById(R.id.negativeButton);
        pos.setOnClickListener(this);
        neg.setOnClickListener(this);
        emailView = findViewById(R.id.email_view);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.positiveButton:
                String email = emailView.getText().toString();
                boolean canSend = true;
                String toast = null;
                if(email.length() == 0){
                    toast = getContext().getString(R.string.email_empty_toast);
                    canSend = false;
                }
                if(canSend){
                    listener.onButtonClick(email);
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

    public interface OnSentPressedListener {
        void onButtonClick(String email);
    }
}

