package com.laodev.focus.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.laodev.focus.R;
import com.laodev.focus.Utils.AppManager;
import com.laodev.focus.Utils.FireManager;
import com.laodev.focus.activities.MainActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewFriendInfoFragment extends Fragment {

    private MainActivity activity;

    public ViewFriendInfoFragment(MainActivity context) {
        activity = context;
    }

    private void initWithEvent() {
        activity.setOnBackPressed(() -> activity.loadFragmentByIndex(0));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.fragment_view_friend_info, container, false);

        activity.toolBar.setTitle(activity.getString(R.string.toobar_title_friend_info));

        initUIView(mainView);
        initWithEvent();

        return mainView;
    }

    private void initUIView(View view) {
        CircleImageView imgAvatar = view.findViewById(R.id.img_view_friend_profile);
        TextView txtFriendName = view.findViewById(R.id.txt_view_friend_username);
        TextView txtPhoneNumber = view.findViewById(R.id.txt_view_friend_phone);
        TextView txtLocation = view.findViewById(R.id.txt_view_friend_location);
        TextView txtFriendCount = view.findViewById(R.id.txt_view_friend_friends);

        Picasso.with(getContext()).load(AppManager.gFriendUser.imgUrl).fit().centerCrop()
                .placeholder(R.drawable.ic_preview)
                .error(R.drawable.ic_profile_template)
                .into(imgAvatar, null);
        txtFriendName.setText(AppManager.gFriendUser.userName);
        txtPhoneNumber.setText(AppManager.gFriendUser.phoneNumber);
        txtLocation.setText(AppManager.gFriendUser.district.toUpperCase() + ", " + AppManager.gFriendUser.province.toUpperCase());
        FireManager.getFriendIdListByUid(AppManager.gFriendUser.userId, new FireManager.GetFriendsListener() {
            @Override
            public void onSuccess(List<String> friends) {
                txtFriendCount.setText("" + friends.size());
            }

            @Override
            public void onFailed() {
                txtFriendCount.setText(getString(R.string.alert_no_friend));
            }
        });
    }

}
