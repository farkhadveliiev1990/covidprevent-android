package com.laodev.focus.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.laodev.focus.R;
import com.laodev.focus.Utils.AppManager;
import com.laodev.focus.Utils.ConnectionHelper;
import com.laodev.focus.Utils.Constants;
import com.laodev.focus.Utils.FireManager;
import com.laodev.focus.Utils.SharedPrefManageer;
import com.laodev.focus.models.Users;

public class SplashActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;
    private ConnectionHelper helper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        initUIView();
    }

    private void initUIView() {
        setInitData();

        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);

        helper = new ConnectionHelper(getApplicationContext());

        final Handler handler = new Handler();
        int SPLASH_TIME_OUT = 3000;
        handler.postDelayed(() -> {
            if (helper.isConnectingToInternet()) {
                gotoNext();
            }else {
                Toast.makeText(SplashActivity.this, getString(R.string.toast_something_went_wrong_net), Toast.LENGTH_SHORT).show();
            }
        }, SPLASH_TIME_OUT);
    }

    private void setInitData() {
        new SharedPrefManageer(this);
        new FireManager();
    }

    private void gotoNext() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser fbUser = mAuth.getCurrentUser();
        if (fbUser != null) {
            getUserInfo(fbUser.getUid());
        }
        else {
            SharedPreferences prefs = getSharedPreferences("com.laodev.focus", MODE_PRIVATE);
            boolean isfirst = prefs.getBoolean("isfirst", true);
            if (isfirst) {
                startActivity(new Intent(SplashActivity.this, RegisterActivity.class));
            } else {
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish();
        }
    }

    private void getUserInfo(String uid) {
        mProgressBar.setVisibility(View.VISIBLE);
        FireManager.getUserInfoByUid(uid, new FireManager.UserInfoListener() {
            @Override
            public void onFound(Users userInfo) {
                // save data to sharedpreference.
                mProgressBar.setVisibility(View.GONE);
                AppManager.gUserInfo = userInfo;
                SharedPrefManageer.setStringSharedPref(Constants.key_userid, userInfo.userId);
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onNotFound(String error) {
                mProgressBar.setVisibility(View.GONE);
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                finish();
            }
        });
    }
}
