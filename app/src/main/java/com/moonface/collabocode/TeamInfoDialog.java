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
import android.widget.PopupMenu;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TeamInfoDialog extends Dialog implements View.OnClickListener {

    private String teamId;
    private List<Member> members;
    private Activity activity;

    TeamInfoDialog(Activity activity, String teamId) {
        super(activity);
        this.activity = activity;
        this.teamId = teamId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_team_info);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Button pos = findViewById(R.id.positiveButton);
        pos.setOnClickListener(this);
        RecyclerView peopleRecycler = findViewById(R.id.people_recycler);
        members = new ArrayList<>();
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        peopleRecycler.setLayoutManager(layoutManager);
        peopleRecycler.setItemAnimator(new DefaultItemAnimator());
        RecyclerView.Adapter adapter = new MembersAdapter(members, activity, (view, member, position) -> {
            FirebaseFirestore.getInstance().collection("teams").document(teamId).collection("members").document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).get().addOnCompleteListener(command -> {
                if (command.getResult() != null) {
                    if (Objects.requireNonNull(command.getResult().toObject(Member.class)).isAdmin() && !member.getUserId().equals(FirebaseAuth.getInstance().getUid())){
                        PopupMenu menu = new PopupMenu(activity, view);
                        menu.inflate(R.menu.member_menu);
                        menu.show();
                        menu.setOnMenuItemClickListener(menuItem -> {
                            switch (menuItem.getItemId()){
                                case R.id.remove_action:
                                    FirebaseFirestore.getInstance().collection("teams").document(teamId).collection("members").document(member.getUserId()).delete();
                                    FirebaseFirestore.getInstance().collection("users").document(member.getUserId()).collection("memberships").document(teamId).delete();
                                    return true;
                                case R.id.make_admin_action:
                                    FirebaseFirestore.getInstance().collection("teams").document(teamId).collection("members").document(member.getUserId()).get().addOnCompleteListener(task -> {
                                        FirebaseFirestore.getInstance().collection("teams").document(teamId).collection("members").document(member.getUserId()).update("admin", !task.getResult().getBoolean("admin"));
                                        FirebaseFirestore.getInstance().collection("users").document(member.getUserId()).collection("memberships").document(teamId).update("admin", !task.getResult().getBoolean("admin"));
                                    });
                                    return true;
                                default:
                                    return false;
                            }
                        });
                        menu.getMenu().findItem(R.id.make_admin_action).setVisible(false);
                        FirebaseFirestore.getInstance().collection("teams").document(teamId).collection("members").document(member.getUserId()).get().addOnCompleteListener(task -> {
                            if (task.getResult().getBoolean("admin")){
                                menu.getMenu().findItem(R.id.make_admin_action).setTitle(R.string.demote_from_admin);
                            }
                            menu.getMenu().findItem(R.id.make_admin_action).setVisible(true);
                        });
                    }
                }
            });

            return true;
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

