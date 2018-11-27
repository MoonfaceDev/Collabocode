package com.moonface.collabocode;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private static final String TEAM_ID = "team_id";
    private FirebaseFirestore firestore;
    private FirebaseFunctions functions;
    private TeamsFragment teamsFragment;
    private FloatingActionsMenu fab;
    private NewTeamDialog newTeamDialog;
    private JoinTeamDialog joinTeamDialog;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(user == null) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
            finish();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        functions = FirebaseFunctions.getInstance();
        firestore = FirebaseFirestore.getInstance();
        teamsFragment = new TeamsFragment();

        fab = findViewById(R.id.fab);
        findViewById(R.id.fab_frame).setOnClickListener(v -> fab.collapse());
        findViewById(R.id.fab_frame).setClickable(false);
        fab.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
            @Override
            public void onMenuExpanded(){
                findViewById(R.id.fab_frame).setBackgroundColor(Color.parseColor("#EEFFFFFF"));
                findViewById(R.id.fab_frame).setClickable(true);
            }

            @Override
            public void onMenuCollapsed(){
                findViewById(R.id.fab_frame).setBackgroundColor(Color.TRANSPARENT);
                findViewById(R.id.fab_frame).setClickable(false);
            }
        });
        findViewById(R.id.new_team).setOnClickListener(v -> {
            newTeamDialog = new NewTeamDialog(MainActivity.this, this::createTeam);
            newTeamDialog.show();
            fab.collapse();
        });

        findViewById(R.id.join_team).setOnClickListener(v -> {
            joinTeamDialog = new JoinTeamDialog(this, this::joinTeam);
            joinTeamDialog.show();
            fab.collapse();
        });

        changeFragments(teamsFragment);

        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.INVISIBLE);

        if (FirebaseAuth.getInstance().getUid() != null) {
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(instanceIdResult -> {
                String token = instanceIdResult.getToken();
                FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().getUid()).update("token_id", token);
            });
        }
    }

    private void changeFragments(Fragment fragment){
        if (getFragmentManager() != null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.placeholder, fragment);
            transaction.commit();
        }
    }

    private void createTeam(String title, String description, Uri iconUri){
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        progressBar.setVisibility(View.VISIBLE);
        Team team = new Team();
        team.setTitle(title);
        team.setDescription(description);
        team.setDateCreated(Timestamp.now());
        team.setDateUpdated(Timestamp.now());
        firestore.collection("teams").add(team).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                firestore.collection("teams").document(Objects.requireNonNull(task.getResult()).getId()).update("id", task.getResult().getId());
                addMember(task.getResult().getId(), true);
                createTeamCode(task.getResult().getId());
                try {
                    if (iconUri != null) {
                        Bitmap bmp = MediaStore.Images.Media.getBitmap(getContentResolver(), iconUri);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        bmp.compress(Bitmap.CompressFormat.JPEG, 5, baos);
                        byte[] data = baos.toByteArray();
                        FirebaseStorage.getInstance().getReference().child("teams").child(task.getResult().getId()).child("icon").putBytes(data).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Objects.requireNonNull(task1.getResult()).getStorage().getDownloadUrl().addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        firestore.collection("teams").document(Objects.requireNonNull(task.getResult()).getId()).update("iconUrl", Objects.requireNonNull(task2.getResult()).toString());
                                        Intent intent = new Intent();
                                        intent.setClass(this, TeamActivity.class);
                                        intent.putExtra(TEAM_ID, task.getResult().getId());
                                        startActivity(intent);
                                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        progressBar.setVisibility(View.INVISIBLE);
                                    } else {
                                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                        progressBar.setVisibility(View.INVISIBLE);
                                    }
                                });
                            } else {
                                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });
                    } else {
                        Intent intent = new Intent();
                        intent.setClass(this, TeamActivity.class);
                        intent.putExtra(TEAM_ID, Objects.requireNonNull(task.getResult()).getId());
                        startActivity(intent);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                } catch (IOException e) {
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    progressBar.setVisibility(View.INVISIBLE);
                }
            } else {
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private void createTeamCode(String id) {
        FirebaseFirestore.getInstance().collection("codes").get().addOnCompleteListener(task -> {
            if (task.getResult() != null){
                List<TeamCode> teamCodes = task.getResult().toObjects(TeamCode.class);
                String code;
                while (true){
                    code = UUID.randomUUID().toString().substring(0,8);
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
                TeamCode teamCode = new TeamCode();
                teamCode.setCode(code);
                teamCode.setTeamId(id);
                teamCode.setEnabled(true);
                FirebaseFirestore.getInstance().collection("codes").document(id).set(teamCode);
            }
        });
    }

    private void joinTeam(String code){
        firestore.collection("codes").get().addOnCompleteListener(task -> {
            if (task.getResult() != null) {
                List<TeamCode> teamCodes = task.getResult().toObjects(TeamCode.class);
                boolean exists = false;
                for (TeamCode teamCode : teamCodes){
                    if (teamCode.getCode().equals(code)){
                        exists = true;
                        DatabaseTasks.addMember(teamCode.getTeamId());
                        break;
                    }
                }
                if (!exists){
                    Toast.makeText(this, "Couldn't find team", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private final static int RESULT_LOAD_IMAGE = 1;

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            if (newTeamDialog != null) {
                newTeamDialog.setIcon(data.getData());
            } else {
                teamsFragment.editTeamDialog.setIcon(data.getData());
            }
        }
    }
}
