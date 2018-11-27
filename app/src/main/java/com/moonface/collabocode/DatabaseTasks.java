package com.moonface.collabocode;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DatabaseTasks {

    public static Task<Void> addMember(String id) {
        Map<String, Object> data = new HashMap<>();
        data.put("team_id", id);
        return FirebaseFunctions.getInstance().getHttpsCallable("joinTeam").call(data).continueWith(task -> null);
    }

    public static Task<Void> makeAdmin(String id){
        Map<String, Object> data = new HashMap<>();
        data.put("team_id", id);
        data.put("user_id", Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));
        data.put("value", true);
        return FirebaseFunctions.getInstance().getHttpsCallable("makeAdmin").call(data).continueWith(task -> null);
    }

    public static Task<User> getUser(String id){
        Map<String, Object> data = new HashMap<>();
        data.put("user_id",id);
        return FirebaseFunctions.getInstance().getHttpsCallable("getUser").call(data).continueWith(task -> ((QueryDocumentSnapshot)Objects.requireNonNull(task.getResult()).getData()).toObject(User.class));
    }
}
