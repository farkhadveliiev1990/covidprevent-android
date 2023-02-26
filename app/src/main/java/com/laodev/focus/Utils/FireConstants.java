package com.laodev.focus.Utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.laodev.focus.models.StatusType;

public class FireConstants {

    private static final FirebaseDatabase database = FirebaseDatabase.getInstance();
    public static final FirebaseStorage storage = FirebaseStorage.getInstance();
    public static final DatabaseReference mainRef = database.getReference();

    //users ref that contain user's data (name,phone,photo etc..)
    public static final DatabaseReference usersRef = mainRef.child("users");

    // friends
    public static final DatabaseReference friendsRef = mainRef.child("friends");

    // quizs
    public static final DatabaseReference quizRef = mainRef.child("quizs");

    // noti
    public static final DatabaseReference notiRef = mainRef.child("noti");

    // token
    public static final DatabaseReference tokenRef = mainRef.child("tokens");


}
