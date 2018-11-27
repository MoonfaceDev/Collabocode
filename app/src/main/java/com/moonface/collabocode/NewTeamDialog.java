package com.moonface.collabocode;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.Objects;

public class NewTeamDialog extends Dialog implements View.OnClickListener {

    private EditText nameView;
    private EditText descriptionView;
    private OnOkPressedListener listener;
    private Activity activity;
    private Uri iconUri;
    private ImageView iconView;
    private boolean isNew;
    private Team team;

    NewTeamDialog(Activity activity, OnOkPressedListener listener) {
        super(activity);
        this.listener = listener;
        this.activity = activity;
        this.isNew = true;
    }

    NewTeamDialog(Activity activity, Team team, OnOkPressedListener listener) {
        super(activity);
        this.listener = listener;
        this.activity = activity;
        this.isNew = false;
        this.team = team;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_new_team);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button pos = findViewById(R.id.positiveButton);
        Button neg = findViewById(R.id.negativeButton);
        pos.setOnClickListener(this);
        neg.setOnClickListener(this);
        nameView = findViewById(R.id.name_view);
        descriptionView = findViewById(R.id.description_view);
        iconView = findViewById(R.id.icon_view);
        iconView.setOnClickListener(v -> pick());
        if (isNew){
            ((TextView)findViewById(R.id.dialog_title)).setText(R.string.new_team_label);
        } else {
            ((TextView)findViewById(R.id.dialog_title)).setText(R.string.edit_team_label);
            nameView.setText(team.getTitle());
            descriptionView.setText(team.getDescription());
            if(team.getIconUrl() != null) {
                Glide.with(activity).load(team.getIconUrl()).apply(RequestOptions.circleCropTransform()).into(iconView);
            }
        }
    }
    private final static int RESULT_LOAD_IMAGE = 1;

    private void pick() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activity.startActivityForResult(pickIntent, RESULT_LOAD_IMAGE);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.positiveButton:
                String name = nameView.getText().toString();
                String description = descriptionView.getText().toString();
                boolean canCreate = true;
                String toast = null;
                if(name.length() == 0){
                    toast = getContext().getString(R.string.title_empty_toast);
                    canCreate = false;
                }
                if(canCreate){
                    listener.onButtonClick(name, description, iconUri);
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

    public interface OnOkPressedListener {
        void onButtonClick(String name, String description, Uri iconUri);
    }

    public void setIcon(Uri iconUri) {
        this.iconUri = iconUri;
        Glide.with(activity).load(iconUri).apply(RequestOptions.circleCropTransform()).into(iconView);
    }

}

