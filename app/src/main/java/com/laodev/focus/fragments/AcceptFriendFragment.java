package com.laodev.focus.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.laodev.focus.R;
import com.laodev.focus.Utils.AppManager;
import com.laodev.focus.Utils.FireManager;
import com.laodev.focus.activities.MainActivity;
import com.laodev.focus.models.Users;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AcceptFriendFragment extends Fragment {

    private MainActivity activity;
    private Users mUser;
    private String friendId;

    public AcceptFriendFragment(MainActivity context, Users user) {
        activity = context;
        mUser = user;
        friendId = user.userId;
    }

    private void initWithEvent() {
        activity.setOnBackPressed(() -> activity.gotoQRScannFragment());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View frgView = inflater.inflate(R.layout.fragment_accept_friend, container, false);

        activity.toolBar.setTitle(activity.getString(R.string.toolbar_title_new_friend));

        ImageView imgAvatar = frgView.findViewById(R.id.img_add_friend_profile);
        TextView txtFriendName = frgView.findViewById(R.id.txt_add_friend_username);

        txtFriendName.setText(mUser.userName);

        Picasso.with(activity).load(mUser.imgUrl).fit().centerCrop()
                .placeholder(R.drawable.ic_preview)
                .error(R.drawable.ic_profile_template)
                .into(imgAvatar, null);

        RelativeLayout rllView = frgView.findViewById(R.id.rll_return);
        rllView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend();
            }
        });

        initWithEvent();

        return frgView;
    }

    private void addFriend() {
        String friendId = mUser.userId;

        ProgressDialog dialog = ProgressDialog.show(activity, "", getString(R.string.alert_connect_server));
        dialog.show();
        FireManager.getFriendIdListByUid(AppManager.gUserInfo.userId, new FireManager.GetFriendsListener() {
            @Override
            public void onSuccess(List<String> myfriends) {
                if(!checkExistFriend(myfriends)){
                    myfriends.add(friendId);
                    FireManager.registerFriendToDatabase(AppManager.gUserInfo.userId, myfriends, new FireManager.CreateFBUserListener() {
                        @Override
                        public void onSuccess(String userID) {
                            FireManager.getFriendIdListByUid(friendId, new FireManager.GetFriendsListener() {
                                @Override
                                public void onSuccess(List<String> friends) {
                                    friends.add(AppManager.gUserInfo.userId);
                                    FireManager.registerFriendToDatabase(friendId, friends, new FireManager.CreateFBUserListener() {
                                        @Override
                                        public void onSuccess(String userID) {
                                            dialog.dismiss();
                                            gotoHomeFragment();
                                        }

                                        @Override
                                        public void onFailure() {
                                            dialog.dismiss();
                                            gotoHomeFragment();
                                        }
                                    });
                                }

                                @Override
                                public void onFailed() {
                                    dialog.dismiss();
                                    Toast.makeText(activity, activity.getString(R.string.msg_fail_to_add_friend), Toast.LENGTH_SHORT).show();
                                    gotoHomeFragment();
                                }
                            });
                        }

                        @Override
                        public void onFailure() {
                            dialog.dismiss();
                            gotoHomeFragment();
                        }
                    });
                }else{
                    dialog.dismiss();
                    Toast.makeText(activity, activity.getString(R.string.toast_thisis_you), Toast.LENGTH_SHORT).show();
                    gotoHomeFragment();
                }
            }

            @Override
            public void onFailed() {
                dialog.dismiss();
                Toast.makeText(activity, activity.getString(R.string.msg_fail_to_add_friend), Toast.LENGTH_SHORT).show();
                gotoHomeFragment();
            }
        });
    }

    private boolean checkExistFriend(List<String> friends) {
        if(friends.size() == 0){
            return false;
        }else{
            for(int i=0; i<friends.size(); i++){
                if(friendId.equals(friends.get(i))) return true;
            }
        }
        return false;
    }

    private void gotoHomeFragment() {
        AppManager.friendHomeDispIndex = 1;
        activity.loadFragmentByIndex(0);
    }

}
