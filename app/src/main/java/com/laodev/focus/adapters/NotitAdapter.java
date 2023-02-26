package com.laodev.focus.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.laodev.focus.R;
import com.laodev.focus.Utils.FireManager;
import com.laodev.focus.activities.MainActivity;
import com.laodev.focus.models.NotiModel;
import com.laodev.focus.models.Users;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NotitAdapter extends BaseAdapter {

    private Context context;
    private List<NotiModel> mFriendUIDs;
    private NotiAdapterListener mListener;

    public NotitAdapter(Context context, List<NotiModel> friends, NotiAdapterListener listener) {
        this.context = context;
        mFriendUIDs = friends;
        mListener = listener;
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
        containView = LayoutInflater.from(context).inflate(R.layout.item_noti, null);

        ImageView imgProfile = containView.findViewById(R.id.img_noti_avatar);
        TextView txtFriendName = containView.findViewById(R.id.txt_noti_name);
        TextView txtPhone = containView.findViewById(R.id.txt_noti_phone);

        String friendUid = mFriendUIDs.get(position).friendid;
        View finalContainView = containView;
        FireManager.getUserInfoByUid(friendUid, new FireManager.UserInfoListener() {
            @Override
            public void onFound(Users userInfo) {
                Picasso.with(context).load(userInfo.imgUrl).fit().centerCrop()
                        .placeholder(R.drawable.ic_preview)
                        .error(R.drawable.ic_profile_template)
                        .into(imgProfile, null);

                txtFriendName.setText(userInfo.userName);
                txtPhone.setText(mFriendUIDs.get(position).datetime);
                finalContainView.setOnClickListener(v -> {
                    mListener.onCallCovidByPhonenumber(userInfo.phoneNumber);
                });
            }

            @Override
            public void onNotFound(String error) {

            }
        });

        return containView;
    }

    public interface NotiAdapterListener {
        void onCallCovidByPhonenumber(String phone);
    }

}
