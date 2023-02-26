package com.laodev.focus.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;
import com.laodev.focus.R;
import com.laodev.focus.Utils.AppManager;
import com.laodev.focus.Utils.ConnectionHelper;
import com.laodev.focus.Utils.Constants;
import com.laodev.focus.Utils.FireManager;
import com.laodev.focus.Utils.SharedPrefManageer;
import com.laodev.focus.models.Users;

import java.util.concurrent.TimeUnit;

public class LoginActivity extends AppCompatActivity {

    private int counter = 60;
    private String verificationId = "";
    private FirebaseAuth mAuth;

    private TextView lbl_counter;
    private EditText txt_phone, txt_code;

    private CountryCodePicker ccp;

    LocationManager manager;
    private ConnectionHelper helper;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            ProgressDialog dialog = ProgressDialog.show(LoginActivity.this, "", getString(R.string.alert_connect_server));
            dialog.show();
            mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener(task -> {
                dialog.dismiss();
                if (task.isSuccessful()) {
                    FirebaseUser user = task.getResult().getUser();
                    getUserInfo(user.getUid());
                } else {
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        Toast.makeText(LoginActivity.this, getString(R.string.verification_invalid_alert), Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .addOnFailureListener(e -> dialog.dismiss());
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Toast.makeText(LoginActivity.this, getString(R.string.toast_invalid_number), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            verificationId = s;
            Toast.makeText(LoginActivity.this, getString(R.string.toast_verification_sent), Toast.LENGTH_SHORT).show();

            if (lbl_counter.getVisibility() == View.GONE) {
                counter = 60;
                lbl_counter.setVisibility(View.VISIBLE);
                onCalcDownCounter();
            }
        }
    };

    public void onClickLogin(View view) {
        String str_code = txt_code.getText().toString();
        if (str_code.length() == 0) {
            Toast.makeText(this, getString(R.string.verification_code_alert), Toast.LENGTH_SHORT).show();
            return;
        }

        ProgressDialog dialog = ProgressDialog.show(this, "", getString(R.string.alert_connect_server));
        dialog.show();

        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, str_code);
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    FirebaseUser user = task.getResult().getUser();
                    getUserInfo(user.getUid());
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this, getString(R.string.verification_invalid_alert), Toast.LENGTH_SHORT).show();
                    }
                }
                dialog.dismiss();
            })
            .addOnFailureListener(e -> dialog.dismiss());
    }

    public void onClickVerifyUB() {

        if (txt_phone.getText().toString().length() == 0) {
            Toast.makeText(this, getString(R.string.alert_phone_number), Toast.LENGTH_SHORT).show();
            return;
        }

        String str_phone =  checkZeroFirstNumber(txt_phone.getText().toString());
        str_phone = ccp.getSelectedCountryCodeWithPlus() + str_phone;

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                str_phone,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);
    }

    private String checkZeroFirstNumber(String number){
        String rtnPhone = "";
        boolean isFirst = true;
        for(int i=0; i<number.length(); i++){
            String tmpN = "";
                tmpN = number.substring(i, i+1);
            if (!(isFirst && tmpN.equals("0"))) {
                isFirst = false;
                rtnPhone += number.charAt(i);
            }
        }
        return rtnPhone;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initWithView();

        mAuth = FirebaseAuth.getInstance();
    }

    private void initWithView() {
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        helper = new ConnectionHelper(getApplicationContext());

        lbl_counter = findViewById(R.id.lbl_login_counter);
        lbl_counter.setVisibility(View.GONE);

        txt_phone = findViewById(R.id.txt_login_phone);
        txt_code = findViewById(R.id.txt_login_code);

        ccp = findViewById(R.id.ccp_login);
        LinearLayout lltVerify = findViewById(R.id.llt_login_verify);
        lltVerify.setOnClickListener(v -> onClickVerifyUB());
    }

    private void onCalcDownCounter() {
        new Handler().postDelayed(this::onShowCounter, 1000);
    }

    private void onShowCounter() {
        counter--;
        if (counter == 0) {
            lbl_counter.setVisibility(View.GONE);
            return;
        }
        lbl_counter.setText(counter + " s");
        onCalcDownCounter();
    }

    private void getUserInfo(String uid) {
        ProgressDialog dialog = ProgressDialog.show(this, "", getString(R.string.alert_connect_server));
        dialog.show();

        FireManager.getUserInfoByUid(uid, new FireManager.UserInfoListener() {
            @Override
            public void onFound(Users userInfo) {
                dialog.dismiss();
                AppManager.gUserInfo = userInfo;
                SharedPrefManageer.setStringSharedPref(Constants.key_userid, userInfo.userId);
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }

            @Override
            public void onNotFound(String error) {
                dialog.dismiss();
                Toast.makeText(LoginActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void goToRegister(View view) {
        if (helper.isConnectingToInternet()) {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        }else {
            Toast.makeText(this, getString(R.string.toast_something_went_wrong_net), Toast.LENGTH_SHORT).show();
        }
    }

    private void exitDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setTitle(R.string.app_name);
        dialog.setMessage(getString(R.string.alert_onback));
        dialog.setPositiveButton(getResources().getString(R.string.alert_yes), (dialogInterface, i) -> {
            moveTaskToBack(true);
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        });

        dialog.setNeutralButton(getResources().getString(R.string.alert_no), (dialogInterface, i) -> {

        });

        dialog.show();
    }

    @Override
    public void onBackPressed() {
        exitDialog();
    }
}
