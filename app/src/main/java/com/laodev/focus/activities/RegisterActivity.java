package com.laodev.focus.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.laodev.focus.R;
import com.laodev.focus.Utils.AppManager;
import com.laodev.focus.Utils.ConnectionHelper;
import com.laodev.focus.Utils.Constants;
import com.laodev.focus.Utils.CropImageRequest;
import com.laodev.focus.Utils.FireManager;
import com.laodev.focus.Utils.SharedPrefManageer;
import com.laodev.focus.models.Users;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class RegisterActivity extends AppCompatActivity {
    private TextView txtGotoLogin;
    private Spinner spnProvince, spnDistrict;
    private EditText edtUserName, edtEmail, edtPassword, edtConfirm;
    private LinearLayout lltRegisterNext, lltFinish;
    private LinearLayout lltInformationDetails, lltLocation;
    private CircleImageView imgProfile;

    private String mUserId;
    private String phoneNumber = "";
    private String profileURL = "";
    private String province, district;

    private String[] aryProvince;
    private List<String> aryDistricts = new ArrayList<>();
    private ArrayAdapter<String> adapterDistrict;

    private int flowIndex = 0;
    private ConnectionHelper helper;

    private int RC_SIGN_UP = 955;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                try {
                    AppManager.gBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);

                    ProgressDialog dialog = ProgressDialog.show(this, "", getString(R.string.alert_connect_server));
                    dialog.show();
                    FireManager.setUserAvatarToFirebase(resultUri, new FireManager.UploadUserAvatarCallback() {
                        @Override
                        public void onSuccessUploadCallback(String url) {
                            profileURL = url;
                            dialog.dismiss();
                            Picasso.with(RegisterActivity.this).load(profileURL).fit().centerCrop()
                                    .placeholder(R.drawable.ic_profile_template)
                                    .error(R.drawable.ic_profile_template)
                                    .into(imgProfile, null);
                            Toast.makeText(RegisterActivity.this, RegisterActivity.this.getString(R.string.toast_change_avatar), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailureUploadCallback() {
                            Toast.makeText(RegisterActivity.this, RegisterActivity.this.getString(R.string.toast_change_not_avatar), Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }
                    });
                } catch (IOException e) {
                    Toast.makeText(RegisterActivity.this, getString(R.string.toast_image_upload_failed), Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                }

            }
        } else if (requestCode == RC_SIGN_UP) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                startTheActivity();
                return;
            } else {
                // Sign in failed
                if (response == null) {
                    // User pressed back button
                    return;
                }
                if (Objects.requireNonNull(response.getError()).getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Toast.makeText(RegisterActivity.this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (response.getError().getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                    Toast.makeText(RegisterActivity.this, getString(R.string.unknown_error), Toast.LENGTH_SHORT).show();
                    return;
                }
            }
            Toast.makeText(RegisterActivity.this, getString(R.string.unknown_response), Toast.LENGTH_SHORT).show();
        }
    }

    private void initEvents() {
        imgProfile.setOnClickListener(v -> pickImages());

        txtGotoLogin.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this, LoginActivity.class)));

        lltRegisterNext.setOnClickListener(v -> {
            if(checkInformationFields()){
                if (helper.isConnectingToInternet()) {
                    OnSignIn();
                } else {
                    Toast.makeText(RegisterActivity.this, getString(R.string.toast_something_went_wrong_net), Toast.LENGTH_SHORT).show();
                }
            }
        });

        lltFinish.setOnClickListener(v -> onRegister());

        spnProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                province = aryProvince[position];
                setSpinerDistricts(province);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        spnDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                district = aryDistricts.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initUIView();
        initEvents();
    }

    private void initUIView() {
        imgProfile = findViewById(R.id.img_register_profile);
        txtGotoLogin = findViewById(R.id.txt_register_goto_login);

        spnProvince = findViewById(R.id.spn_register_province);
        spnDistrict = findViewById(R.id.spn_register_district);

        edtUserName = findViewById(R.id.edt_register_username);
        edtEmail = findViewById(R.id.edt_register_email);
        edtPassword = findViewById(R.id.edt_register_password);
        edtConfirm = findViewById(R.id.edt_register_confirm_password);

        lltRegisterNext = findViewById(R.id.llt_register_next);
        lltFinish = findViewById(R.id.llt_register_finish);

        lltInformationDetails = findViewById(R.id.llt_register_details);
        lltLocation = findViewById(R.id.llt_register_Location);

        helper = new ConnectionHelper(getApplicationContext());

        initData();
    }

    private void initData() {
        profileURL = getString(R.string.profile_avatar_temp_url_1) + "&" + getString(R.string.profile_avatar_temp_url_2);
        Picasso.with(RegisterActivity.this).load(profileURL).fit().centerCrop()
                .placeholder(R.drawable.ic_profile_template)
                .error(R.drawable.ic_profile_template)
                .into(imgProfile, null);

        aryProvince = AppManager.gProvinces;
        ArrayAdapter<String> adapterProvince = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, aryProvince);
        spnProvince.setAdapter(adapterProvince);

        adapterDistrict = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, aryDistricts);
        spnDistrict.setAdapter(adapterDistrict);

        setSpinerDistricts(aryProvince[0]);

        flowIndex = 0;
        updateUI(flowIndex);
    }

    private void updateUI(int index){
        lltInformationDetails.setVisibility(View.GONE);
        lltLocation.setVisibility(View.GONE);

        switch (index){
            case 0: lltInformationDetails.setVisibility(View.VISIBLE); break;
            case 1: lltLocation.setVisibility(View.VISIBLE); break;
        }
    }

    private void setSpinerDistricts(String selectedProvince){
        aryDistricts.clear();
        for(int i=0; i<AppManager.gDistricts.length; i++){
            if(AppManager.gDistricts[i][0].equals(selectedProvince)){
                aryDistricts.add(AppManager.gDistricts[i][1]);
            }
        }
        adapterDistrict.notifyDataSetChanged();
    }

    private boolean checkInformationFields() {
        if(edtUserName.getText().toString().length() == 0){
            Toast.makeText(RegisterActivity.this, getString(R.string.toast_username_empty), Toast.LENGTH_SHORT).show();
            return false;
        }
        if(edtUserName.getText().toString().length() < 5){
            Toast.makeText(RegisterActivity.this, getString(R.string.toast_username_short), Toast.LENGTH_SHORT).show();
            return false;
        }
        if(edtEmail.getText().toString().length() == 0){
            Toast.makeText(RegisterActivity.this, getString(R.string.toast_email_empty), Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!edtEmail.getText().toString().contains("@")){
            Toast.makeText(RegisterActivity.this, getString(R.string.toast_email_invalid), Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!edtEmail.getText().toString().contains(".")){
            Toast.makeText(RegisterActivity.this, getString(R.string.toast_email_invalid), Toast.LENGTH_SHORT).show();
            return false;
        }
        edtEmail.setText(trimEmail(edtEmail.getText().toString()));
        if(edtPassword.getText().toString().length() == 0){
            Toast.makeText(RegisterActivity.this, getString(R.string.toast_password_empty), Toast.LENGTH_SHORT).show();
            return false;
        }
        if(edtPassword.getText().toString().length() < 6){
            Toast.makeText(RegisterActivity.this, getString(R.string.toast_password_short), Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!edtConfirm.getText().toString().equals(edtPassword.getText().toString())){
            Toast.makeText(RegisterActivity.this, getString(R.string.toast_confirm_password), Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private String trimEmail(String emaillString) {
        return emaillString.replaceAll(" ", "");
    }

    public void pickImages(){
        CropImageRequest.getCropImageRequest().start(this);
    }

    private void onRegister() {
        Users userInfo = new Users();

        userInfo.userId = mUserId;
        userInfo.userName = edtUserName.getText().toString();
        userInfo.userEmail = edtEmail.getText().toString();
        userInfo.phoneNumber = phoneNumber;
        userInfo.imgUrl = profileURL;
        userInfo.userCovid = Constants.COVID_NORMAL;
        userInfo.district = district;
        userInfo.province = province;
        userInfo.location = "";
        userInfo.fullName = "";
        userInfo.userAge = "";
        userInfo.userCin = "";
        userInfo.gender = "";
        userInfo.countryCode = "";
        userInfo.latitude = "";
        userInfo.longitude = "";

        AppManager.gUserInfo = userInfo;

        ProgressDialog dialog = ProgressDialog.show(this, "", getString(R.string.alert_connect_server));
        dialog.show();
        FireManager.registerUserToDatabase(userInfo, new FireManager.CreateFBUserListener() {
            @Override
            public void onSuccess(String userID) {
                dialog.dismiss();
                SharedPrefManageer.setStringSharedPref(Constants.key_userid, userID);
                startActivity(new Intent(RegisterActivity.this, MainActivity.class));

                SharedPreferences.Editor editor = getSharedPreferences("com.laodev.focus", MODE_PRIVATE).edit();
                editor.putBoolean("isfirst", false);
                editor.apply();

                finish();
            }

            @Override
            public void onFailure() {
                dialog.dismiss();
                Toast.makeText(RegisterActivity.this, getString(R.string.toast_reister_failed), Toast.LENGTH_SHORT).show();

                flowIndex = 0;
                updateUI(flowIndex);
            }
        });
    }

    public void OnSignIn() {
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build())).build(),
            RC_SIGN_UP);
    }

    private void startTheActivity() {
        mUserId = FireManager.getUid();
        phoneNumber = FireManager.getPhoneNumber();

        flowIndex = 1;
        updateUI(flowIndex);
    }

    public void exitDialog() {
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
        if(flowIndex == 0){
            exitDialog();
        }
        if(flowIndex == 1) {
            flowIndex = 0;
            updateUI(flowIndex);
        }
    }
}
