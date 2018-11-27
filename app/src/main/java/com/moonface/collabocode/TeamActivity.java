package com.moonface.collabocode;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TeamActivity extends AppCompatActivity {

    private static final String TEAM_ID = "team_id";
    private Team team;
    private List<Post> posts;
    DocumentReference teamRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        RecyclerView postsView  = findViewById(R.id.posts_view);
        posts = new ArrayList<>();
        postsView.setLayoutManager(new LinearLayoutManager(this));
        PostsAdapter adapter = new PostsAdapter(posts, false, getIntent().getStringExtra(TEAM_ID), this);
        adapter.setOnItemLongClickListener((view, post, position) -> {
            PopupMenu menu = new PopupMenu(this, view);
            menu.inflate(R.menu.post_menu);
            menu.show();

            menu.getMenu().findItem(R.id.delete_action).setVisible(post.getUserId().equals(FirebaseAuth.getInstance().getUid()));
            menu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()){
                    case R.id.reply_action:
                        startActivity(new Intent(this, NewPostActivity.class).putExtra("team_id", team.getId()).putExtra("post_id", post.getId()).putExtra("post_title", post.getTitle()));
                        return true;
                    case R.id.copy_action:
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Post", post.getTitle()+"\n"+post.getContent());
                        if (clipboard != null) {
                            clipboard.setPrimaryClip(clip);
                            Snackbar.make(findViewById(android.R.id.content), "Copied post", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    case R.id.delete_action:
                        DeletePostDialog deletePostDialog = new DeletePostDialog(this, false, () -> FirebaseFirestore.getInstance().collection("teams").document(team.getId()).collection("posts").document(post.getId()).delete().addOnCompleteListener((task) -> {
                            if (task.isSuccessful()){
                                Snackbar.make(findViewById(android.R.id.content), "Post deleted successfully", Snackbar.LENGTH_SHORT).show();
                            }
                        }));
                        deletePostDialog.show();
                        return true;
                    default:
                        return false;
                }
            });

            return true;
        });
        postsView.setAdapter(adapter);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        teamRef = firestore.collection("teams").document(getIntent().getStringExtra(TEAM_ID));

        teamRef.addSnapshotListener((snapshot, e) -> {
            if (snapshot != null) {
                team = snapshot.toObject(Team.class);
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(team.getTitle());
                    getSupportActionBar().setSubtitle(team.getDescription());
                }
                teamRef.collection("posts").addSnapshotListener((queryDocumentSnapshots, e1) -> {
                    if (queryDocumentSnapshots != null){
                        posts.clear();
                        posts.addAll(queryDocumentSnapshots.toObjects(Post.class));
                        Collections.sort(posts, (o1, o2) -> o2.getDateSent().compareTo(o1.getDateSent()));
                        adapter.notifyDataSetChanged();
                        postsView.scheduleLayoutAnimation();
                    }
                });
            }
        });

        View addPostView = findViewById(R.id.add_post_card);
        addPostView.setOnClickListener(v -> startActivity(new Intent(this, NewPostActivity.class).putExtra("team_id", team.getId())));

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.team_view_menu, menu);
        menu.findItem(R.id.add_people).setVisible(false);
        if (FirebaseAuth.getInstance().getUid() != null) {
            teamRef.collection("members").document(FirebaseAuth.getInstance().getUid()).get().addOnCompleteListener(task -> {
                if (task.getResult() != null) {
                    Member member = task.getResult().toObject(Member.class);
                    if (member != null && member.isAdmin()) {
                        menu.findItem(R.id.add_people).setVisible(true);
                    }
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.add_people:
                AddPeopleDialog addPeopleDialog = new AddPeopleDialog(this, team);
                addPeopleDialog.show();
                return true;

            default:

                return super.onOptionsItemSelected(item);

        }
    }
}
