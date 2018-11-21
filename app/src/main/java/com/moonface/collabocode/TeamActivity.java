package com.moonface.collabocode;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
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
        PostsAdapter adapter = new PostsAdapter(posts, this);
        adapter.setOnItemLongClickListener((view, post, position) -> {
            PopupMenu menu = new PopupMenu(this, view);
            menu.inflate(R.menu.post_menu);
            menu.show();

            menu.getMenu().findItem(R.id.delete_action).setVisible(post.getUserId().equals(FirebaseAuth.getInstance().getUid()));
            menu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()){
                    case R.id.reply_action:

                        return true;
                    case R.id.copy_action:

                        return true;
                    case R.id.delete_action:

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
                        adapter.notifyDataSetChanged();
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
        teamRef.collection("members").get().addOnCompleteListener(task -> {
            if (task.getResult() != null) {
                List<Member> members = task.getResult().toObjects(Member.class);
                for (Member member : members){
                    if (FirebaseAuth.getInstance().getCurrentUser() != null && member.getUserId().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) && member.isAdmin()){
                        menu.findItem(R.id.add_people).setVisible(true);
                        break;
                    }
                }
            }
        });
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
