package com.moonface.collabocode;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.Window;
import android.widget.Button;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Objects;

public class MembersDialog extends Dialog implements View.OnClickListener {

    private String teamId;
    private ArrayList<Member> members;
    private Activity activity;

    MembersDialog(Activity activity, String teamId) {
        super(activity);
        this.activity = activity;
        this.teamId = teamId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.members_dialog);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button pos = findViewById(R.id.positiveButton);
        pos.setOnClickListener(this);
        RecyclerView peopleRecycler = findViewById(R.id.people_recycler);
        members = new ArrayList<>();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        peopleRecycler.setLayoutManager(layoutManager);
        peopleRecycler.setItemAnimator(new DefaultItemAnimator());
        RecyclerView.Adapter adapter = new MembersAdapter(members, activity, (member, position) -> {

        });
        peopleRecycler.setAdapter(adapter);
        firestore.collection("teams").document(teamId).collection("members").addSnapshotListener((queryDocumentSnapshots, e) -> {
            if (queryDocumentSnapshots != null) {
                members.clear();
                members.addAll(queryDocumentSnapshots.toObjects(Member.class));
                adapter.notifyDataSetChanged();
                peopleRecycler.setAdapter(adapter);
            }
        });
        View addPeopleButton = findViewById(R.id.add_people_button);
        addPeopleButton.setOnClickListener(v -> {

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

