package com.moonface.collabocode;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class NewPostActivity extends AppCompatActivity {
    private List<Code> codeList;
    private CodesAdapter codesAdapter;
    private TextView titleView, contentView;
    ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setTitle("New post");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(view1 -> onBackPressed());

        titleView = findViewById(R.id.title_view);
        contentView = findViewById(R.id.content_view);
        progressBar = findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.INVISIBLE);

        RecyclerView codesRecycler = findViewById(R.id.codes_list);
        codesRecycler.setLayoutManager(new LinearLayoutManager(this));
        codeList = new ArrayList<>();
        codesAdapter = new CodesAdapter(codeList);
        codesAdapter.setOnItemClickListener((code, position) -> {
            Intent editCodeIntent = new Intent(this, CodeEditorActivity.class);
            editCodeIntent.putExtra("title", code.getTitle());
            editCodeIntent.putExtra("code", code.getContent());
            editCodeIntent.putExtra("titlesList", getTitlesArray());
            startActivityForResult(editCodeIntent, 2);
        });
        codesRecycler.setAdapter(codesAdapter);
    }

    private String[] getTitlesArray() {
        String[] list = new String[codeList.size()];
        for (Code code : codeList){
            list[list.length - 1] = code.getTitle();
        }
        return list;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.new_post_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.code_action:
                startActivityForResult(new Intent(this, CodeEditorActivity.class).putExtra("titlesList", getTitlesArray()), 2);
                return true;
            case R.id.send_action:
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                progressBar.setVisibility(View.VISIBLE);
                Post post = new Post();
                post.setTitle(titleView.getText().toString());
                post.setContent(contentView.getText().toString());
                post.setDateSent(Timestamp.now());
                post.setUserId(FirebaseAuth.getInstance().getUid());
                post.setCodes(codeList);
                FirebaseFirestore.getInstance().collection("teams").document(getIntent().getStringExtra("team_id")).collection("posts").add(post).addOnCompleteListener(task -> {
                    FirebaseFirestore.getInstance().collection("teams").document(getIntent().getStringExtra("team_id")).collection("posts").document(Objects.requireNonNull(task.getResult()).getId()).update("id",task.getResult().getId());
                    finish();
                });
                return true;
            default:

                return super.onOptionsItemSelected(item);

        }
    }
    @Override
    public void onBackPressed(){
        DiscardPostDialog discardPostDialog = new DiscardPostDialog(this, super::onBackPressed);
        discardPostDialog.show();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            if (resultCode == RESULT_OK) {
                String title = data.getStringExtra("RESULT_TITLE");
                String content = data.getStringExtra("RESULT_CODE");
                Code code = new Code();
                code.setTitle(title);
                code.setContent(content);
                if (containsTitle(codeList, title)){
                    codeList.set(titleIndex(codeList, title), code);
                } else {
                    codeList.add(code);
                }
                codesAdapter.notifyDataSetChanged();
            }
        }
    }
    private boolean containsTitle(List<Code> codes, String title){
        for (int i = 0; i < codes.size(); i++) {
            String t = codes.get(i).getTitle();
            if (t.equals(title)) {
                return true;
            }
        }
        return false;
    }
    private int titleIndex(List<Code> codes, String title){
        for (int i = 0; i < codes.size(); i++) {
            String t = codes.get(i).getTitle();
            if (t.equals(title)) {
                return i;
            }
        }
        return -1;
    }
}