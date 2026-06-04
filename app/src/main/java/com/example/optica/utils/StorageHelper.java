package com.example.optica.utils;

import android.net.Uri;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class StorageHelper {

    private final FirebaseStorage storage;

    public StorageHelper() {
        this.storage = FirebaseStorage.getInstance();
    }

    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onFailure(Exception e);
    }

    public void uploadPrescriptionImage(String userId, Uri fileUri, UploadCallback callback) {
        String fileName = "prescription_" + System.currentTimeMillis() + ".jpg";
        StorageReference storageRef = storage.getReference()
                .child("prescriptions")
                .child(userId)
                .child(fileName);

        UploadTask uploadTask = storageRef.putFile(fileUri);

        uploadTask.continueWithTask(task -> {
            if (!task.isSuccessful()) {
                throw task.getException();
            }
            return storageRef.getDownloadUrl();
        }).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Uri downloadUri = task.getResult();
                callback.onSuccess(downloadUri.toString());
            } else {
                callback.onFailure(task.getException());
            }
        });
    }
}
