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

public class JoinTeamDialog extends Dialog implements View.OnClickListener {

    private EditText codeView;
    private OnJoinPressedListener listener;

    JoinTeamDialog(Activity activity, OnJoinPressedListener listener) {
        super(activity);
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_join_team);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button pos = findViewById(R.id.positiveButton);
        Button neg = findViewById(R.id.negativeButton);
        pos.setOnClickListener(this);
        neg.setOnClickListener(this);
        codeView = findViewById(R.id.code_view);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.positiveButton:
                String code = codeView.getText().toString();
                boolean canJoin = true;
                String toast = null;
                if(code.length() == 0){
                    toast = getContext().getString(R.string.code_empty_toast);
                    canJoin = false;
                }
                if(canJoin){
                    listener.onButtonClick(code);
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

    public interface OnJoinPressedListener {
        void onButtonClick(String code);
    }
}

