package com.laodev.focus.Utils;

import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.laodev.focus.models.NotiModel;
import com.laodev.focus.models.QuizModel;
import com.laodev.focus.models.Users;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.client.cache.HeaderConstants;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static java.util.UUID.randomUUID;

public class FireManager {

    private static FirebaseAuth mFireAuth;
    public static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");

    public FireManager(){
        mFireAuth = FirebaseAuth.getInstance();
    }

    public static String getUid() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null)
            return FirebaseAuth.getInstance().getCurrentUser().getUid();
        return null;
    }

    public static void getUserInfoByUid( String uid, final UserInfoListener listener ) {
        FireConstants.usersRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if( dataSnapshot.getValue() != null ) {
                    String key = dataSnapshot.getKey();
                    Users info = dataSnapshot.getValue(Users.class);
                    info.userId = key;
                    listener.onFound(info);
                } else {
                    listener.onNotFound("No hay una cuenta con su número de teléfono, cree una cuenta ahora.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onNotFound("Error de la base de datos");
            }
        });
    }

    public static void getAllUsers( final GetAllUsersListener listener ) {
        FireConstants.usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if( dataSnapshot.getValue() != null ) {
                    List<Users> users = new ArrayList<>();
                    for(DataSnapshot singleData: dataSnapshot.getChildren()){
                        Users userInfo = singleData.getValue(Users.class);
                        users.add(userInfo);
                    }
                    if(users.size() == 0){
                        listener.onNotExist();
                    }else{
                        listener.onFound(users);
                    }
                } else {
                    listener.onNotFound("No hay una cuenta con su número de teléfono, cree una cuenta ahora.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onNotFound("Error de la base de datos");
            }
        });
    }

    public static String getPhoneNumber() {
        return FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber();
    }

    public static void getQuizListByUserIdAndDate(String userId, final GetQuizsListener listener) {

        FireConstants.quizRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<QuizModel> quizs = new ArrayList<>();
                for(DataSnapshot singleData: dataSnapshot.getChildren()){
                    QuizModel quizModel = singleData.getValue(QuizModel.class);
                    quizs.add(quizModel);
                }
                listener.onSuccess(quizs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailed();
            }
        });
    }

    public static void registerToken(String token, final RegisterTokenListener listener ){
        FireConstants.tokenRef.child(AppManager.gUserInfo.userId).child("token").setValue(token)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        listener.onSuccess(token);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onFailure();
                    }
                });

    }

    public static void registerQuizToDatabase(String userId, List<QuizModel> quizs, final RegisterQuizListener listener ){
        FireConstants.quizRef.child(userId).setValue(quizs)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    listener.onSuccess("success");
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    listener.onFailure();
                }
            });

    }

    public static void getFriendIdListByUid(String uid, final GetFriendsListener listener) {

        FireConstants.friendsRef.child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<String> friends = new ArrayList<>();
                for(DataSnapshot singleData: dataSnapshot.getChildren()){
                    String friendId = singleData.getValue(String.class);
                    friends.add(friendId);
                }
                listener.onSuccess(friends);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailed();
            }
        });
    }

    public static void getFriendTokenList(List<String> uidList, final GetFriendsListener listener) {
        List<String> friendTokenList = new ArrayList<>();
        for(int i=0; i<uidList.size(); i++){
            FireConstants.tokenRef.child(uidList.get(i)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot singData: dataSnapshot.getChildren()){
                        String friendToken = singData.getValue(String.class);
                        friendTokenList.add(friendToken);
                    }
                    if(uidList.size() == friendTokenList.size()){
                        listener.onSuccess(friendTokenList);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    listener.onFailed();
                    return;
                }
            });
        }
    }

    public static void updateProfile(final Users user, final UpdateProfileListener listener ){
        FireConstants.usersRef.child(user.userId).setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        listener.onSuccess(user.userId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onFailure();
                    }
                });
    }

    public static void registerUserToDatabase(final Users user, final CreateFBUserListener listener ){
        FireConstants.usersRef.child(user.userId).setValue(user)
            .addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    listener.onSuccess(user.userId);
                }
            })
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    listener.onFailure();
                }
            });
    }

    public static void registerFriendToDatabase(String uid, final List<String> friends, final CreateFBUserListener listener ){
        FireConstants.friendsRef.child(uid).setValue(friends)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        listener.onSuccess("success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onFailure();
                    }
                });
    }

    public static void setUserAvatarToFirebase(final Uri imagePath, final UploadUserAvatarCallback callback) {
        String filename = randomUUID().toString() + ".jpg";

        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        StorageReference childRef = storageRef.child(filename);

        String storagePath = "image_profile/" + filename;
        final StorageReference mountainImagesRef = storageRef.child(storagePath);

        childRef.getName().equals(mountainImagesRef.getName());    // true
        childRef.getPath().equals(mountainImagesRef.getPath());    // false

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        AppManager.gBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = mountainImagesRef.putBytes(data);
        uploadTask.addOnSuccessListener(taskSnapshot -> mountainImagesRef.getDownloadUrl().addOnSuccessListener(downloadPhotoUrl -> {
            String url = downloadPhotoUrl.toString();
            callback.onSuccessUploadCallback(url);
        }))
        .addOnFailureListener(e -> callback.onFailureUploadCallback());
    }

    public static void onSendFBPushnotification(String token, String title, String sBody) {
        String json = "{\"to\":\"" + token + "\",\"notification\":{\"body\":\"" + sBody + "\",\"title\":\"" + title + "\"}}";
        RequestBody body = RequestBody.create(JSON, json);
        Request request = new Request.Builder()
                .addHeader(HeaderConstants.AUTHORIZATION, Constants.SERVER_KEY)
                .addHeader("Content-Type", "application/json")
                .url("https://fcm.googleapis.com/fcm/send")
                .post(body)
                .build();
        new OkHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) { }

            @Override
            public void onResponse(Call call, Response response) { }
        });
    }

    public static void onSaveNotiToFB(String userid, NotiModel model) {
        String key = FireConstants.notiRef.child(userid).push().getKey();
        model.id = key;
        FireConstants.notiRef.child(userid).child(key).setValue(model);
    }

    public static void onGetNotiFromFB(String userid, NotificationListener listener) {
        FireConstants.notiRef.child(userid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                List<NotiModel> models = new ArrayList<>();
                for (DataSnapshot snapshot: dataSnapshot.getChildren()) {
                    NotiModel notiModel = snapshot.getValue(NotiModel.class);
                    models.add(notiModel);
                }
                listener.onSuccess(models);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                listener.onFailure();
            }
        });
    }

    public interface NotificationListener {
        void onSuccess(List<NotiModel> notiModels);
        void onFailure();
    }

    public interface RegisterTokenListener{
        void onSuccess(String token);
        void onFailure();
    }

    public interface UserInfoListener {
        void onFound(Users userInfo);
        void onNotFound(String error);
    }

    public interface GetAllUsersListener {
        void onFound(List<Users> userInfos);
        void onNotExist();
        void onNotFound(String error);
    }

    public interface UpdateProfileListener{
        void onSuccess(String userID);
        void onFailure();
    }

    public interface CreateFBUserListener{
        void onSuccess(String userID);
        void onFailure();
    }

    public interface RegisterQuizListener{
        void onSuccess(String result);
        void onFailure();
    }

    public interface UploadUserAvatarCallback {
        void onSuccessUploadCallback(String url);
        void onFailureUploadCallback();
    }

    public interface GetFriendsListener{
        void onSuccess(List<String> friends);
        void onFailed();
    }

    public interface GetQuizsListener{
        void onSuccess(List<QuizModel> friends);
        void onFailed();
    }

}
