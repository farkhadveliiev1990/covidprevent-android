package com.laodev.focus.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.laodev.focus.R;
import com.laodev.focus.Utils.AppManager;
import com.laodev.focus.Utils.ConnectionHelper;
import com.laodev.focus.Utils.Constants;
import com.laodev.focus.Utils.CropImageRequest;
import com.laodev.focus.Utils.FireManager;
import com.laodev.focus.activities.LoginActivity;
import com.laodev.focus.activities.MainActivity;
import com.squareup.picasso.Picasso;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {

    private MainActivity activity;

    private CircleImageView imgProfile;
    private TextView txtProfileEdit, txtPhone, txtLocation, txtProvince, txtDistrict;
    private EditText edtFullName, edtCin, edtAge, txt_userName, edtEmail;
    private LinearLayout lltSubmit, lltProvinceDistrict;
    private Spinner spnProvince, spnDistrict;

    private String[] aryProvince;
    private int provinceId;
    private List<String> aryDistricts = new ArrayList<>();

    private ConnectionHelper helper;

    private Spinner genderSpiner;

    private List<String> genders = new ArrayList<>();

    private String profileURL, gender;
    private boolean editable = false;
    private boolean isFirst = true;

    public ProfileFragment(MainActivity context) {
        activity = context;
    }

    private void initEvents() {
        imgProfile.setOnClickListener(v -> {
            if(editable){
                pickImages();
            }
        });

        lltSubmit.setOnClickListener(v -> {
            if(editable){
                editable = false;
                txtProfileEdit.setText(activity.getString(R.string.edit));
                disableEditView(edtFullName);
                disableEditView(edtCin);
                disableEditView(txt_userName);
                disableEditView(edtAge);
                disableEditView(edtEmail);
                genderSpiner.setEnabled(false);
                lltProvinceDistrict.setVisibility(View.GONE);

                if (helper.isConnectingToInternet()) {
                    AppManager.gUserInfo.userName = txt_userName.getText().toString();
                    AppManager.gUserInfo.fullName = edtFullName.getText().toString();
                    AppManager.gUserInfo.userCin = edtCin.getText().toString();
                    AppManager.gUserInfo.userAge = edtAge.getText().toString();
                    AppManager.gUserInfo.userEmail = edtEmail.getText().toString();
                    AppManager.gUserInfo.location = txtLocation.getText().toString();
                    AppManager.gUserInfo.province = txtProvince.getText().toString();
                    AppManager.gUserInfo.district = txtDistrict.getText().toString();
                    AppManager.gUserInfo.phoneNumber = txtPhone.getText().toString();
                    AppManager.gUserInfo.gender = gender;
                    AppManager.gUserInfo.countryCode = "";
                    AppManager.gUserInfo.imgUrl = profileURL;

                    ProgressDialog dialog = ProgressDialog.show(activity, "", getString(R.string.alert_connect_server));
                    dialog.show();

                    FireManager.updateProfile(AppManager.gUserInfo, new FireManager.UpdateProfileListener() {
                        @Override
                        public void onSuccess(String userID) {

                            dialog.dismiss();
                            Toast.makeText(activity, activity.getString(R.string.success_update_profile), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure() {
                            dialog.dismiss();
                            Toast.makeText(activity, activity.getString(R.string.faile_update_profile), Toast.LENGTH_SHORT).show();
                        }
                    });
                }else {
                    Toast.makeText(activity, getString(R.string.toast_something_went_wrong_net), Toast.LENGTH_SHORT).show();
                }
            } else {
                editable = true;
                txtProfileEdit.setText(activity.getString(R.string.save));
                edtFullName.setEnabled(true);
                edtCin.setEnabled(true);
                txt_userName.setEnabled(true);
                edtAge.setEnabled(true);
                edtEmail.setEnabled(true);
                genderSpiner.setEnabled(true);
                lltProvinceDistrict.setVisibility(View.VISIBLE);
//                setSpinnersProvinceDistrict();
            }
        });

        genderSpiner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                gender = genders.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        spnProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (isFirst) {
                    return;
                }
                provinceId = position;
                txtProvince.setText(aryProvince[provinceId]);
                txtLocation.setText(txtDistrict.getText().toString() + ", " + txtProvince.getText().toString());
                setSpinerDistricts(aryProvince[provinceId]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        spnDistrict.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (isFirst) {
                    isFirst = false;
                    return;
                }
                txtDistrict.setText(aryDistricts.get(position));
                txtLocation.setText(txtDistrict.getText().toString() + ", " + txtProvince.getText().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        activity.setOnBackPressed(() -> activity.exitDialog());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View frgView = inflater.inflate(R.layout.fragment_profile, container, false);

        activity.toolBar.setTitle(getString(R.string.bottom_profile));

        initUIView(frgView);
        initEvents();

        return frgView;
    }

    private void initUIView(View frgView) {
        imgProfile = frgView.findViewById(R.id.img_profile_image);
        txt_userName = frgView.findViewById(R.id.txt_profile_username);
        edtFullName = frgView.findViewById(R.id.edt_profile_full_name);
        edtCin = frgView.findViewById(R.id.edt_profile_cin);
        edtAge = frgView.findViewById(R.id.edt_profile_age);
        edtEmail = frgView.findViewById(R.id.edt_profile_email);
        txtPhone = frgView.findViewById(R.id.edt_profile_phone);
        txtLocation = frgView.findViewById(R.id.edt_profile_location);
        lltSubmit = frgView.findViewById(R.id.llt_profile_save);
        txtProfileEdit = frgView.findViewById(R.id.txt_profile_edit);
        ImageView imgCovidStatus = frgView.findViewById(R.id.img_profile_covid_image);
        switch (AppManager.gUserInfo.userCovid) {
            case Constants.COVID_NORMAL:
                imgCovidStatus.setImageResource(R.drawable.ic_covid_no);
                break;
            case Constants.COVID_SUSPECTED:
                imgCovidStatus.setImageResource(R.drawable.ic_covid_medium);
                break;
            case Constants.COVID_INFECTED:
                imgCovidStatus.setImageResource(R.drawable.ic_covid_yes);
                break;
        }

        lltProvinceDistrict = frgView.findViewById(R.id.llt_province_district);
        txtProvince = frgView.findViewById(R.id.txt_profile_province);
        txtDistrict = frgView.findViewById(R.id.txt_profile_district);
        spnProvince = frgView.findViewById(R.id.spn_profile_province);
        spnDistrict = frgView.findViewById(R.id.spn_profile_district);

        helper = new ConnectionHelper(activity.getApplicationContext());

        genderSpiner = frgView.findViewById(R.id.spiner_profile_gender);

        KeyboardVisibilityEvent.setEventListener(
            activity, isOpen -> {
                // write your code
                if(isOpen){
                    activity.bottomNavigation.setVisibility(View.GONE);
                }else{
                    activity.bottomNavigation.setVisibility(View.VISIBLE);
                }
            });

        initData();
    }

    private void initData() {
        genders.add(activity.getString(R.string.male));
        genders.add(activity.getString(R.string.female));

        editable = false;
        setEditTextAndTextView();

        profileURL = AppManager.gUserInfo.imgUrl;
        Picasso.with(activity).load(profileURL).fit().centerCrop()
                .placeholder(R.drawable.ic_preview)
                .error(R.drawable.ic_profile_template)
                .into(imgProfile, null);

        txt_userName.setText(AppManager.gUserInfo.userName);
        edtFullName.setText(AppManager.gUserInfo.fullName);
        edtCin.setText(AppManager.gUserInfo.userCin);
        edtAge.setText(AppManager.gUserInfo.userAge);
        edtEmail.setText(AppManager.gUserInfo.userEmail);

        txtPhone.setText(AppManager.gUserInfo.phoneNumber);

        ArrayAdapter<String> adapterGender = new ArrayAdapter<>(activity, android.R.layout.simple_dropdown_item_1line, genders);
        genderSpiner.setAdapter(adapterGender);
        if (genders.get(0).equals(AppManager.gUserInfo.gender)) {
            genderSpiner.setSelection(0);
        } else {
            genderSpiner.setSelection(1);
        }

        aryProvince = AppManager.gProvinces;
        ArrayAdapter<String> adapterProvince = new ArrayAdapter<>(activity, android.R.layout.simple_dropdown_item_1line, aryProvince);
        spnProvince.setAdapter(adapterProvince);

        setSpinnersProvinceDistrict();

        lltProvinceDistrict.setVisibility(View.GONE);
    }

    private void setSpinnersProvinceDistrict() {
        txtProvince.setText(AppManager.gUserInfo.province);
        provinceId = setSelectionProvinceSpinner();
        spnProvince.setSelection(provinceId);

        txtDistrict.setText(AppManager.gUserInfo.district);
        setSpinerDistricts(aryProvince[provinceId]);
        int districtId = setSelectionDistrictSpinner();
        spnDistrict.setSelection(districtId);

        txtLocation.setText(AppManager.gUserInfo.district.toUpperCase() + ", " + AppManager.gUserInfo.province.toUpperCase());
    }

    private int setSelectionProvinceSpinner() {
        for(int i=0; i<AppManager.gProvinces.length; i++){
            if(AppManager.gProvinces[i].equals(AppManager.gUserInfo.province)) {
                return i;
            }
        }
        return 0;
    }

    private int setSelectionDistrictSpinner() {
        for(int i=0; i<aryDistricts.size(); i++){
            if(aryDistricts.get(i).equals(AppManager.gUserInfo.district)) {
                return i;
            }
        }
        return 0;
    }

    private void setSpinerDistricts(String selectedProvince){
        aryDistricts.clear();
        for(int i=0; i<AppManager.gDistricts.length; i++){
            if(AppManager.gDistricts[i][0].equals(selectedProvince)){
                aryDistricts.add(AppManager.gDistricts[i][1]);
            }
        }
        ArrayAdapter<String> adapterDistrict = new ArrayAdapter<String>(activity, android.R.layout.simple_dropdown_item_1line, aryDistricts);
        spnDistrict.setAdapter(adapterDistrict);
        if (!isFirst) {
            txtDistrict.setText(aryDistricts.get(0));
        }
    }

    private void setEditTextAndTextView() {
        if(editable){
            txtProfileEdit.setText(activity.getString(R.string.save));
            edtFullName.setEnabled(true);
            edtCin.setEnabled(true);
            txt_userName.setEnabled(true);
            edtAge.setEnabled(true);
            edtEmail.setEnabled(true);
            genderSpiner.setEnabled(true);
            lltProvinceDistrict.setVisibility(View.VISIBLE);
            setSpinnersProvinceDistrict();
        } else {
            txtProfileEdit.setText(activity.getString(R.string.edit));
            genderSpiner.setEnabled(false);
            disableEditView(edtFullName);
            disableEditView(edtCin);
            disableEditView(txt_userName);
            disableEditView(edtAge);
            disableEditView(edtEmail);
            lltProvinceDistrict.setVisibility(View.GONE);
        }
    }

    private void disableEditView(EditText edtView){
        edtView.setEnabled(false);
        edtView.setFocusable(true);
        edtView.requestFocus();
        edtView.invalidate();
        edtView.setTextColor(getResources().getColor(R.color.colorTextBlack));
    }

    private void pickImages(){
        CropImageRequest.getCropImageRequest().start(getActivity());
    }

    public void onUpdateProfilePhoto(Uri resultUri) {
        try {
            AppManager.gBitmap = MediaStore.Images.Media.getBitmap(activity.getContentResolver(), resultUri);
            ProgressDialog dialog = ProgressDialog.show(activity, "", getString(R.string.alert_connect_server));
            dialog.show();
            FireManager.setUserAvatarToFirebase(resultUri, new FireManager.UploadUserAvatarCallback() {
                @Override
                public void onSuccessUploadCallback(String url) {
                    dialog.dismiss();
                    profileURL = url;
                    Picasso.with(activity).load(profileURL).fit().centerCrop()
                            .placeholder(R.drawable.ic_profile_template)
                            .error(R.drawable.ic_profile_template)
                            .into(imgProfile, null);
                    Toast.makeText(activity, activity.getString(R.string.toast_change_avatar), Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailureUploadCallback() {
                    dialog.dismiss();
                }
            });
        } catch (IOException e) {
            Toast.makeText(activity, activity.getString(R.string.toast_image_upload_failed), Toast.LENGTH_SHORT).show();
            startActivity(new Intent(activity, LoginActivity.class));
        }
    }

    public void onFailureUpdateProfilePhoto(){
        Toast.makeText(activity, activity.getString(R.string.toast_image_upload_failed), Toast.LENGTH_SHORT).show();
    }

}
