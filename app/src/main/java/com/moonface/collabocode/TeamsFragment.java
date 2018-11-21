package com.moonface.collabocode;


import android.content.Intent;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class TeamsFragment extends Fragment {

    private static final String TEAM_ID = "team_id";
    RecyclerView teamsView;
    TeamsAdapter adapter;
    FirebaseFirestore firestore;
    CollectionReference teamsRef;
    DocumentReference userRef;


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
            View star = sheetView.findViewById(R.id.star);
            View rename = sheetView.findViewById(R.id.rename);
            View leave = sheetView.findViewById(R.id.leave);

            addPeople.setVisibility(View.GONE);
            rename.setVisibility(View.GONE);
            teamsRef.document(team.getId()).collection("members").get().addOnCompleteListener(task -> {
                if (task.getResult() != null) {
                    List<Member> members = task.getResult().toObjects(Member.class);
                    for (Member member : members){
                        if (member.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && member.isAdmin()){
                            addPeople.setVisibility(View.VISIBLE);
                            rename.setVisibility(View.VISIBLE);
                            break;
                        }
                    }
                }
            });

            titleView.setText(team.getTitle());
            if (info != null){
                info.setOnClickListener(v -> {

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

                });
            }
            if (rename != null) {
                rename.setOnClickListener(v -> {
                    teamsRef.document(team.getId()).collection("members").get().addOnCompleteListener(task -> {
                        if (task.getResult() != null) {
                            List<Member> members = task.getResult().toObjects(Member.class);
                            for (Member member : members){
                                if (member.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && member.isAdmin()){
                                    //actions here
                                    break;
                                }
                            }
                        }
                    });
                });
            }
            if (leave != null) {
                leave.setOnClickListener(v -> {

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
                                    if (o1.getDateCreated() != null && o2.getDateCreated() != null) {
                                        return o2.getDateCreated().compareTo(o1.getDateCreated());
                                    }
                                    return 0;
                                }));
                            }
                            adapter.notifyDataSetChanged();
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
