package com.laodev.focus.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.laodev.focus.Utils.FireManager;
import com.laodev.focus.activities.MainActivity;
import com.laodev.focus.R;
import com.laodev.focus.Utils.Constants;
import com.laodev.focus.models.Users;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FriendListAdapter extends BaseAdapter {

    private MainActivity context;
    private List<String> mFriendUIDs;

    private FriendListAdapterCallback callback;

    public FriendListAdapter(MainActivity context, List<String> friends, FriendListAdapterCallback callback) {
        this.context = context;
        mFriendUIDs = friends;
        this.callback = callback;
    }

    @Override
    public int getCount() {
        return mFriendUIDs.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View containView, ViewGroup parent) {
        containView = LayoutInflater.from(context).inflate(R.layout.item_friend, null);

        ImageView imgProfile = containView.findViewById(R.id.img_friend_profile);
        TextView txtFriendName = containView.findViewById(R.id.txt_friend_username);
        TextView txtAddress = containView.findViewById(R.id.txt_friend_location);
        TextView txtPhone = containView.findViewById(R.id.txt_friend_phone);
        ImageView imgCovidStatus = containView.findViewById(R.id.img_friend_covid);
        ImageView callImage = containView.findViewById(R.id.img_call_image);

        String friendUid = mFriendUIDs.get(position);
        View finalContainView = containView;
        FireManager.getUserInfoByUid(friendUid, new FireManager.UserInfoListener() {
            @Override
            public void onFound(Users userInfo) {
                Picasso.with(context).load(userInfo.imgUrl).fit().centerCrop()
                        .placeholder(R.drawable.ic_preview)
                        .error(R.drawable.ic_profile_template)
                        .into(imgProfile, null);

                txtFriendName.setText(userInfo.userName);
                txtAddress.setText(userInfo.location);
                txtPhone.setText(userInfo.phoneNumber);

                if(userInfo.userCovid.toLowerCase().equals(Constants.COVID_NORMAL)){
                    imgCovidStatus.setImageResource(R.drawable.ic_covid_no);
                } else if(userInfo.userCovid.toLowerCase().equals(Constants.COVID_SUSPECTED)){
                    imgCovidStatus.setImageResource(R.drawable.ic_covid_medium);
                } else if(userInfo.userCovid.toLowerCase().equals(Constants.COVID_INFECTED)){
                    imgCovidStatus.setImageResource(R.drawable.ic_covid_yes);
                } else{
                    imgCovidStatus.setImageResource(R.drawable.ic_covid_no);
                }

                finalContainView.setOnClickListener(v -> callback.onClickFriendCallback(userInfo));
                callImage.setOnClickListener(v -> callback.onClickCllImage(userInfo));
            }

            @Override
            public void onNotFound(String error) {

            }
        });

        return containView;
    }

    public interface FriendListAdapterCallback {
        void onClickFriendCallback(Users user);
        void onClickCllImage(Users user);
    }

}
