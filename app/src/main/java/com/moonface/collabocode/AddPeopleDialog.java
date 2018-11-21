package com.moonface.collabocode;

import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class AddPeopleDialog extends Dialog implements View.OnClickListener {

    private Team team;
    private TeamCode code;
    private Activity activity;

    AddPeopleDialog(Activity activity, Team team) {
        super(activity);
        this.team = team;
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_add_people);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button pos = findViewById(R.id.positiveButton);
        pos.setOnClickListener(this);

        TextView codeView = findViewById(R.id.code_view);
        Switch enabledSwitch = findViewById(R.id.enabled_switch);

        FirebaseFirestore.getInstance().collection("codes").document(team.getId()).addSnapshotListener((documentSnapshot, e) -> {
            if (documentSnapshot != null){
                code = documentSnapshot.toObject(TeamCode.class);
                if (code != null) {
                    codeView.setText(code.getCode());
                    enabledSwitch.setChecked(code.isEnabled());
                }
            }
        });

        codeView.setOnClickListener(v -> {
            ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Collabocode team code", codeView.getText());
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(activity, "Copied team code", Toast.LENGTH_SHORT).show();
            }
        });

        enabledSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            FirebaseFirestore.getInstance().collection("codes").document(team.getId()).update("enabled", isChecked);
            if (isChecked && !code.isEnabled()){
                FirebaseFirestore.getInstance().collection("codes").get().addOnCompleteListener(task -> {
                    if (task.getResult() != null){
                        List<TeamCode> teamCodes = task.getResult().toObjects(TeamCode.class);
                        String code;
                        while (true){
                            code = UUID.randomUUID().toString().substring(0,6);
                            boolean unique = true;
                            for (TeamCode teamCode : teamCodes){
                                if (teamCode.getCode().equals(code)){
                                    unique = false;
                                }
                            }
                            if (unique) {
                                break;
                            }
                        }
                        FirebaseFirestore.getInstance().collection("codes").document(team.getId()).update("code", code);
                    }
                });
            }
            if (!isChecked){
                findViewById(R.id.code_layout).setVisibility(View.GONE);
            } else {
                findViewById(R.id.code_layout).setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.positiveButton:
                dismiss();
                break;
            default:
                dismiss();
                break;
        }
    }
}

