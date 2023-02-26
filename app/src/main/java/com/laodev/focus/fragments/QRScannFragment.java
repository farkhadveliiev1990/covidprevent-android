package com.laodev.focus.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.laodev.focus.R;
import com.laodev.focus.Utils.AppManager;
import com.laodev.focus.Utils.FireManager;
import com.laodev.focus.activities.MainActivity;
import com.laodev.focus.models.Users;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class QRScannFragment extends Fragment implements ZBarScannerView.ResultHandler {
    private MainActivity activity;

    private ZBarScannerView mScannerView;

    public QRScannFragment(MainActivity context) {
        activity = context;
    }

    private void initWithEvent() {
        activity.setOnBackPressed(() -> activity.loadFragmentByIndex(0));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity.toolBar.setTitle(activity.getString(R.string.toobar_title_qr_scann));

        View view = inflater.inflate(R.layout.fragment_qr_scann, container, false);

        mScannerView = view.findViewById(R.id.zx_view);
        mScannerView.setAspectTolerance(0.5f);

        initWithEvent();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this);
        mScannerView.startCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
    }

    @Override
    public void handleResult(Result rawResult) {
        onEventGetResult(rawResult.getContents());
    }

    private void onEventGetResult(String contents) {
        ProgressDialog dialog = ProgressDialog.show(activity, "", getString(R.string.alert_connect_server));
        dialog.show();
        FireManager.getUserInfoByUid(contents, new FireManager.UserInfoListener() {
            @Override
            public void onFound(Users userInfo) {
                // save data to sharedpreference.
                dialog.dismiss();
                if (userInfo.userId.equals(AppManager.gUserInfo.userId)) {
                    Toast.makeText(activity, getString(R.string.error_self_qr), Toast.LENGTH_SHORT).show();
                    activity.loadFragmentByIndex(0);
                    return;
                }
                activity.gotoAcceptedFragment(userInfo);
            }

            @Override
            public void onNotFound(String error) {
                dialog.dismiss();
                Toast.makeText(activity, R.string.msg_fail_scan_qr, Toast.LENGTH_SHORT).show();

                activity.loadFragmentByIndex(0);
            }
        });
    }

}
