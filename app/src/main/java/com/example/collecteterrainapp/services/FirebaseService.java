package com.example.collecteterrainapp.services;

import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Map;

public class FirebaseService {

    private FirebaseFirestore db;

    public FirebaseService() {
        db = FirebaseFirestore.getInstance();
    }

    public interface CollectionCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public void saveCollection(Map<String, Object> collecteData, CollectionCallback callback) {
        db.collection("collectes")
                .add(collecteData)
                .addOnSuccessListener(documentReference -> {
                    callback.onSuccess();
                })
                .addOnFailureListener(callback::onFailure);
    }
}
