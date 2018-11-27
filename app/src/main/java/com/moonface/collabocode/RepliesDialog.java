package com.moonface.collabocode;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class RepliesDialog extends DialogFragment {

    private String teamId;
    private String postId;
    private List<Post> replies;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.RepliesDialogStyle);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.dialog_replies, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        toolbar.setNavigationOnClickListener(view1 -> dismiss());
        toolbar.setTitle("Replies");
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        RecyclerView repliesView = view.findViewById(R.id.replies_view);
        LinearLayoutManager manager = new LinearLayoutManager(getContext());
        repliesView.setLayoutManager(manager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(Objects.requireNonNull(getContext()), manager.getOrientation());
        repliesView.addItemDecoration(dividerItemDecoration);
        replies = new ArrayList<>();
        PostsAdapter adapter = new PostsAdapter(replies, true, teamId, getActivity());
        adapter.setOnItemLongClickListener((view1, reply, position) -> {
            PopupMenu menu = new PopupMenu(Objects.requireNonNull(getActivity()), view1);
            menu.inflate(R.menu.post_menu);
            menu.show();

            menu.getMenu().findItem(R.id.delete_action).setVisible(reply.getUserId().equals(FirebaseAuth.getInstance().getUid()));
            menu.getMenu().findItem(R.id.reply_action).setVisible(false);
            menu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.copy_action:
                        ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clip = ClipData.newPlainText("Reply", reply.getTitle()+"\n"+reply.getContent());
                        if (clipboard != null) {
                            clipboard.setPrimaryClip(clip);
                            Snackbar.make(view, "Copied reply", Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    case R.id.delete_action:
                        DeletePostDialog deletePostDialog = new DeletePostDialog(getActivity(), true, () -> FirebaseFirestore.getInstance().collection("teams").document(teamId).collection("posts").document(postId).collection("replies").document(reply.getId()).delete().addOnCompleteListener((task) -> {
                            if (task.isSuccessful()){
                                Snackbar.make(view, "Reply deleted successfully", Snackbar.LENGTH_SHORT).show();
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
        repliesView.setAdapter(adapter);

        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        DocumentReference postRef = firestore.collection("teams").document(teamId).collection("posts").document(postId);

        postRef.collection("replies").addSnapshotListener((queryDocumentSnapshots, e1) -> {
            if (queryDocumentSnapshots != null) {
                replies.clear();
                replies.addAll(queryDocumentSnapshots.toObjects(Post.class));
                Collections.sort(replies, (o1, o2) -> o2.getDateSent().compareTo(o1.getDateSent()));
                adapter.notifyDataSetChanged();
            }
        });

        getDialog().setOnKeyListener((dialog, keyCode, event) -> {

            if ((keyCode == android.view.KeyEvent.KEYCODE_BACK)) {
                dismiss();
                return true;
            } else
                return false;
        });

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            Objects.requireNonNull(dialog.getWindow()).setLayout(width, height);
        }
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }
}
