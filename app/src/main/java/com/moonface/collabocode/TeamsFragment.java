package com.moonface.collabocode;


import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class TeamsFragment extends Fragment {

    private static final String TEAM_ID = "team_id";
    RecyclerView teamsView;
    TeamsAdapter adapter;
    FirebaseFirestore firestore;
    CollectionReference teamsRef;
    DocumentReference userRef;
    NewTeamDialog editTeamDialog;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_teams, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState){
        ProgressBar loadingBar = view.findViewById(R.id.loading_bar);
        loadingBar.setVisibility(View.INVISIBLE);

        firestore = FirebaseFirestore.getInstance();
        teamsRef = firestore.collection("teams");
        userRef = firestore.collection("users").document(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
        teamsView = view.findViewById(R.id.teams_view);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        teamsView.setLayoutManager(layoutManager);
        teamsView.setItemAnimator(new DefaultItemAnimator());
        final ArrayList<Team> teams = new ArrayList<>();
        adapter = new TeamsAdapter(teams, getActivity());
        adapter.setOnItemClickListener((team, position) -> startActivity(new Intent(getContext(), TeamActivity.class).putExtra(TEAM_ID, team.getId())));
        adapter.setOnItemLongClickListener((holder, team, position) -> {
            ((CardView)holder.block).setCardBackgroundColor(getResources().getColor(R.color.colorHighlight));
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(Objects.requireNonNull(getActivity()), R.style.TeamInfoDialog);
            View sheetView = getActivity().getLayoutInflater().inflate(R.layout.dialog_team_actions, null);
            TextView titleView = sheetView.findViewById(R.id.title_view);
            View info = sheetView.findViewById(R.id.info);
            View addPeople = sheetView.findViewById(R.id.add_people);
            TextView star = sheetView.findViewById(R.id.star);
            View edit = sheetView.findViewById(R.id.edit);
            View leave = sheetView.findViewById(R.id.leave);

            addPeople.setVisibility(View.GONE);
            star.setVisibility(View.GONE);
            edit.setVisibility(View.GONE);
            teamsRef.document(team.getId()).collection("members").get().addOnCompleteListener(task -> {
                if (task.getResult() != null) {
                    List<Member> members = task.getResult().toObjects(Member.class);
                    for (Member member : members){
                        if (member.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                            if (!member.isStarred()){
                                star.setText(R.string.star_label);
                            } else {
                                star.setText(R.string.unstar_label);
                            }
                            star.setVisibility(View.VISIBLE);
                            if (member.isAdmin()) {
                                addPeople.setVisibility(View.VISIBLE);
                                edit.setVisibility(View.VISIBLE);
                                break;
                            }
                        }
                    }
                }
            });

            titleView.setText(team.getTitle());
            if (info != null){
                info.setOnClickListener(v -> {
                    TeamInfoDialog teamInfoDialog = new TeamInfoDialog(getActivity(), team.getId());
                    teamInfoDialog.show();
                });
            }
            if (addPeople != null) {
                addPeople.setOnClickListener(v -> {
                    AddPeopleDialog addPeopleDialog = new AddPeopleDialog(getActivity(), team);
                    addPeopleDialog.show();
                });
            }
            if (star != null) {
                star.setOnClickListener(v -> {
                    if (FirebaseAuth.getInstance().getUid() != null) {
                        teamsRef.document(team.getId()).collection("members").document(FirebaseAuth.getInstance().getUid()).get().addOnCompleteListener(task -> {
                            if (task.getResult() != null) {
                                if (task.getResult().toObject(Member.class) != null) {
                                    teamsRef.document(team.getId()).collection("members").document(FirebaseAuth.getInstance().getUid()).update("starred", !task.getResult().toObject(Member.class).isStarred());
                                    userRef.collection("memberships").document(team.getId()).update("starred", !task.getResult().toObject(Member.class).isStarred());
                                    if (task.getResult().toObject(Member.class).isStarred()){
                                        star.setText(R.string.star_label);
                                    } else {
                                        star.setText(R.string.unstar_label);
                                    }
                                }
                            }
                        });
                    }
                });
            }
            if (edit != null) {
                edit.setOnClickListener(v -> {
                    editTeamDialog = new NewTeamDialog(getActivity(), team, (name, description, iconUri) -> {
                        teamsRef.document(team.getId()).update("title", name);
                        teamsRef.document(team.getId()).update("description", description);
                        try {
                            if (iconUri != null) {
                                Bitmap bmp = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), iconUri);
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                bmp.compress(Bitmap.CompressFormat.JPEG, 5, baos);
                                byte[] data = baos.toByteArray();
                                FirebaseStorage.getInstance().getReference().child("teams").child(team.getId()).child("icon").putBytes(data).addOnCompleteListener(task1 -> {
                                    if (task1.isSuccessful()) {
                                        Objects.requireNonNull(task1.getResult()).getStorage().getDownloadUrl().addOnCompleteListener(task2 -> {
                                            if (task2.isSuccessful()) {
                                                teamsRef.document(team.getId()).update("iconUrl", Objects.requireNonNull(task2.getResult()).toString());
                                            }
                                        });
                                    }
                                });
                            }
                        } catch (IOException ignored) {

                        }
                    });
                    editTeamDialog.show();
                });
            }
            if (leave != null) {
                leave.setOnClickListener(v -> {
                    LeaveTeamDialog leaveTeamDialog = new LeaveTeamDialog(getActivity(), team.getTitle(), () -> {
                        teamsRef.document(team.getId()).collection("members").document(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).delete().addOnCompleteListener(command -> {
                            teamsRef.document(team.getId()).collection("members").get().addOnCompleteListener(task -> {
                                if (task.getResult() != null){
                                    List<Member> members = task.getResult().toObjects(Member.class);
                                    int adminCount = 0;
                                    for(Member member : members){
                                        if (member.isAdmin()){
                                            adminCount++;
                                        }
                                    }
                                    if (adminCount == 0){
                                        if(members.size() > 0) {
                                            String newAdminId = members.get(new Random().nextInt(members.size())).getUserId();
                                            teamsRef.document(team.getId()).collection("members").document(newAdminId).update("admin",true);
                                        }
                                    }
                                }
                            });
                        });
                        userRef.collection("memberships").document(team.getId()).delete();
                    });
                    leaveTeamDialog.show();
                });
            }
            bottomSheetDialog.setContentView(sheetView);
            bottomSheetDialog.setCanceledOnTouchOutside(true);
            bottomSheetDialog.setOnCancelListener(dialog -> ((CardView)holder.block).setCardBackgroundColor(getResources().getColor(R.color.white)));
            bottomSheetDialog.show();
            return true;
        });
        teamsView.setAdapter(adapter);
        loadingBar.setVisibility(View.VISIBLE);
        userRef.collection("memberships").addSnapshotListener((queryDocumentSnapshots, e) -> {
            loadingBar.setVisibility(View.VISIBLE);
            if (queryDocumentSnapshots != null) {
                List<Member> memberships = queryDocumentSnapshots.toObjects(Member.class);
                teams.clear();
                for (int i = 0; i < memberships.size(); i++) {
                    loadingBar.setVisibility(View.VISIBLE);
                    Member m = memberships.get(i);
                    teamsRef.document(m.getTeamId()).addSnapshotListener((documentSnapshot, e1) -> {
                        loadingBar.setVisibility(View.INVISIBLE);
                        if (documentSnapshot != null) {
                            Team p = documentSnapshot.toObject(Team.class);
                            if (projectInList(teams, p)) {
                                teams.set(findProjectIndex(teams, p), p);
                            } else {
                                teams.add(p);
                            }
                            if (teams.size() > 0) {
                                Collections.sort(teams, ((o1, o2) -> {
                                    boolean isStarred1 = false;
                                    boolean isStarred2 = false;
                                    for (Member membership : memberships) {
                                        if (membership.getTeamId().equals(o1.getId())) {
                                            isStarred1 = membership.isStarred();
                                        }
                                        if (membership.getTeamId().equals(o2.getId())) {
                                            isStarred2 = membership.isStarred();
                                        }
                                    }
                                    int starComparison = Boolean.compare(isStarred2, isStarred1);
                                    if (starComparison != 0) {
                                        return starComparison;
                                    } else if (o1.getDateUpdated() != null && o2.getDateUpdated() != null){
                                        return o2.getDateUpdated().compareTo(o1.getDateUpdated());
                                    } else {
                                        return 0;
                                    }
                                }));
                            }
                            adapter.notifyDataSetChanged();
                            teamsView.scheduleLayoutAnimation();
                        }
                    });
                }
                if (memberships.size() == 0){
                    loadingBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private int findProjectIndex(ArrayList<Team> teams, Team p) {
        for(int i = 0; i< teams.size(); i++){
            if(teams.get(i).getId().equals(p.getId())){
                return i;
            }
        }
        return teams.size();
    }

    private boolean projectInList(ArrayList<Team> teams, Team p) {
        for(int i = 0; i< teams.size(); i++){
            if(teams.get(i).getId().equals(p.getId())){
                return true;
            }
        }
        return false;
    }
}
