package com.laodev.focus.fragments;

import android.app.Application;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.laodev.focus.R;
import com.laodev.focus.Utils.AppManager;
import com.laodev.focus.Utils.Constants;
import com.laodev.focus.Utils.FireManager;
import com.laodev.focus.Utils.SharedPrefManageer;
import com.laodev.focus.activities.MainActivity;
import com.laodev.focus.adapters.FriendListAdapter;
import com.laodev.focus.models.Users;

import java.util.ArrayList;
import java.util.List;

public class FriendsFragment extends Fragment implements FriendListAdapter.FriendListAdapterCallback {

    private View mainFrgView;
    private final static int QRcodeWidth = 500 ;
    private final static int QRcodeHeight = 500 ;

    private MainActivity activity;

    private TextView txtNoFriends;
    private ImageView imgAddFriend, imgShowFriends;
    private LinearLayout lltNoFriends;

    private ListView lstFriends;
    private FriendListAdapter mFriendAdapter;

    private ImageView myQRImage;

    public FriendsFragment(MainActivity context) {
        activity = context;
    }

    private void initEvents(){
        imgAddFriend.setOnClickListener(v -> activity.gotoQRScannFragment());
        imgShowFriends.setOnClickListener(v -> {
            AppManager.friendHomeDispIndex = 1;
            convertDisplayStatus();
        });
        activity.setOnBackPressed(() -> activity.exitDialog());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mainFrgView = inflater.inflate(R.layout.fragment_friends, container, false);

        initUIView();
        initEvents();

        return mainFrgView;
    }

    private void initUIView() {
        lstFriends = mainFrgView.findViewById(R.id.lst_friends);
        mFriendAdapter = new FriendListAdapter(activity, AppManager.gFriendIDList, this);
        lstFriends.setAdapter(mFriendAdapter);

        txtNoFriends = mainFrgView.findViewById(R.id.txt_friend_no_friends);
        txtNoFriends.setText(activity.getString(R.string.msg_no_friends));

        imgAddFriend = mainFrgView.findViewById(R.id.img_friend_add);
        imgShowFriends = mainFrgView.findViewById(R.id.img_friend_create_qr);

        lltNoFriends = mainFrgView.findViewById(R.id.llt_friend_no_friends);
        lltNoFriends.setVisibility(View.GONE);

        myQRImage = mainFrgView.findViewById(R.id.img_home_qr);

        convertDisplayStatus();
    }

    private void convertDisplayStatus() {
        switch (AppManager.friendHomeDispIndex){
            case 0:
                activity.toolBar.setTitle(getString(R.string.bottom_my_id));
                myQRImage.setVisibility(View.VISIBLE);
                lstFriends.setVisibility(View.GONE);
                txtNoFriends.setVisibility(View.GONE);

                generateQrCode();
                break;
            case 1:
                activity.toolBar.setTitle(getString(R.string.bottom_contactos));
                myQRImage.setVisibility(View.GONE);
                lstFriends.setVisibility(View.VISIBLE);

                initContactData();
                break;
        }
    }

    private void initContactData() {
        ProgressDialog dialog = ProgressDialog.show(activity, "", getString(R.string.alert_connect_server));
        dialog.show();
        FireManager.getFriendIdListByUid(AppManager.gUserInfo.userId, new FireManager.GetFriendsListener() {
            @Override
            public void onSuccess(List<String> friends) {
                dialog.dismiss();
                AppManager.gFriendIDList.clear();
                AppManager.gFriendIDList.addAll(friends);
                if (AppManager.gFriendIDList.size() == 0) {
                    lltNoFriends.setVisibility(View.VISIBLE);
                } else {
                    mFriendAdapter.notifyDataSetChanged();
                    lltNoFriends.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailed() {
                dialog.dismiss();
                txtNoFriends.setText(activity.getString(R.string.msg_failed_get_friends));
                lltNoFriends.setVisibility(View.VISIBLE);
                lstFriends.setVisibility(View.GONE);
            }
        });
    }

    private void generateQrCode() {
        String myId = SharedPrefManageer.getStringSharedPref(Constants.key_userid);
        try {
            if (AppManager.gBmpQRCode == null) {
                AppManager.gBmpQRCode = TextToImageEncode(myId);
            }
            myQRImage.setImageBitmap(AppManager.gBmpQRCode);
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

    private Bitmap TextToImageEncode(String Value) throws WriterException {
        BitMatrix bitMatrix;
        try {
            bitMatrix = new MultiFormatWriter().encode(
                    Value,
                    BarcodeFormat.QR_CODE,
                    QRcodeWidth, QRcodeHeight, null
            );
        } catch (IllegalArgumentException Illegalargumentexception) {
            return null;
        }
        int bitMatrixWidth = bitMatrix.getWidth();
        int bitMatrixHeight = bitMatrix.getHeight();
        int[] pixels = new int[bitMatrixWidth * bitMatrixHeight];

        for (int y = 0; y < bitMatrixHeight; y++) {
            int offset = y * bitMatrixWidth;
            for (int x = 0; x < bitMatrixWidth; x++) {
                pixels[offset + x] = bitMatrix.get(x, y) ?
                        getResources().getColor(R.color.colorTextBlack):getResources().getColor(R.color.colorWhite);
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(bitMatrixWidth, bitMatrixHeight, Bitmap.Config.ARGB_4444);
        bitmap.setPixels(pixels, 0, 500, 0, 0, bitMatrixWidth, bitMatrixHeight);
        return bitmap;
    }

    private void onEventDeleteUser(Users user) {
        ProgressDialog dialog = ProgressDialog.show(activity, "", getString(R.string.alert_connect_server));
        dialog.show();
        FireManager.getFriendIdListByUid(AppManager.gUserInfo.userId, new FireManager.GetFriendsListener() {
            @Override
            public void onSuccess(List<String> friends) {
                AppManager.gFriendIDList.clear();
                AppManager.gFriendIDList.addAll(friends);
                for (int i = 0; i < AppManager.gFriendIDList.size(); i++) {
                    String friendId = AppManager.gFriendIDList.get(i);
                    if (friendId.equals(user.userId)) {
                        AppManager.gFriendIDList.remove(i);
                        break;
                    }
                }
                FireManager.registerFriendToDatabase(AppManager.gUserInfo.userId, AppManager.gFriendIDList, new FireManager.CreateFBUserListener() {
                    @Override
                    public void onSuccess(String userID) {
                        dialog.dismiss();
                        mFriendAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure() {
                        dialog.dismiss();
                    }
                });
            }

            @Override
            public void onFailed() { }
        });

        FireManager.getFriendIdListByUid(user.userId, new FireManager.GetFriendsListener() {
            @Override
            public void onSuccess(List<String> friends) {
                List<String> newFriends = new ArrayList<>();
                newFriends.addAll(friends);
                for (int i = 0; i < newFriends.size(); i++) {
                    String friendId = newFriends.get(i);
                    if (friendId.equals(AppManager.gUserInfo.userId)) {
                        newFriends.remove(i);
                        break;
                    }
                }
                FireManager.registerFriendToDatabase(user.userId, newFriends, new FireManager.CreateFBUserListener() {
                    @Override
                    public void onSuccess(String userID) {
                        //
                    }

                    @Override
                    public void onFailure() {
                        //
                    }
                });
            }

            @Override
            public void onFailed() { }
        });
    }

    @Override
    public void onClickFriendCallback(Users user) {
        AppManager.gFriendUser = user;
        activity.gotoFriendFragment();
    }

    @Override
    public void onClickCllImage(Users user) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setTitle(R.string.app_name);
        dialog.setMessage(getString(R.string.alert_delet_content));
        dialog.setPositiveButton(getResources().getString(R.string.alert_yes), (dialogInterface, i) -> {
            onEventDeleteUser(user);
        });

        dialog.setNeutralButton(getResources().getString(R.string.alert_no), (dialogInterface, i) -> { });

        dialog.show();
    }

}
