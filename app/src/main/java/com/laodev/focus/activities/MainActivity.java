package com.laodev.focus.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.iid.FirebaseInstanceId;
import com.laodev.focus.R;
import com.laodev.focus.Utils.AppManager;
import com.laodev.focus.Utils.Constants;
import com.laodev.focus.Utils.FireManager;
import com.laodev.focus.Utils.SharedPrefManageer;
import com.laodev.focus.fragments.AcceptFriendFragment;
import com.laodev.focus.fragments.FriendsFragment;
import com.laodev.focus.fragments.ProfileFragment;
import com.laodev.focus.fragments.QRScannFragment;
import com.laodev.focus.fragments.QuizFragment;
import com.laodev.focus.fragments.RiskZoneFragment;
import com.laodev.focus.fragments.StatisticsFragment;
import com.laodev.focus.fragments.ViewFriendInfoFragment;
import com.laodev.focus.fragments.ViewQuizStatusFragment;
import com.laodev.focus.models.QuizModel;
import com.laodev.focus.models.Users;
import com.mapbox.mapboxsdk.Mapbox;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.List;

public class MainActivity extends AppCompatActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    public Toolbar toolBar;
    public BottomNavigationView bottomNavigation;
    private ProfileFragment frgProfile;

    private MainOnBackPressed onBackPressed;

    public final int CAMERA_SCANN_ID = 100;
    public final int FIND_LOCATION_ID = 101;

    private final LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            //your code here
            if (AppManager.gUserInfo.latitude.length() == 0) {
                AppManager.gUserInfo.latitude = "" + location.getLatitude();
                AppManager.gUserInfo.longitude = "" + location.getLongitude();
                FireManager.updateProfile(AppManager.gUserInfo, new FireManager.UpdateProfileListener() {
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
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setToolbar();
        initUIActivity();

        if (AppManager.gUserInfo.latitude.length() == 0) {
            checkLocationPerssion();
        }
    }

    private void setToolbar() {
        toolBar = findViewById(R.id.toolbar);

        if (toolBar != null) {
            setSupportActionBar(toolBar);

            toolBar.setOnMenuItemClickListener(item -> {
                int id = item.getItemId();
                switch (id) {
                    case R.id.action_noti:
                        AppManager.showOtherActivity(this, NotificationActivity.class, 0);
                        return true;
                }
                return false;
            });
        }
    }

    public void initUIActivity() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    String token = task.getResult().getToken();
                    FireManager.registerToken(token, new FireManager.RegisterTokenListener() {
                        @Override
                        public void onSuccess(String token) {
                            SharedPrefManageer.setStringSharedPref(Constants.key_token, token);
                        }

                        @Override
                        public void onFailure() { }
                    });
                }
            });

        FireManager.getFriendIdListByUid(AppManager.gUserInfo.userId, new FireManager.GetFriendsListener() {
            @Override
            public void onSuccess(List<String> friends) {
                AppManager.gFriendIDList.clear();
                AppManager.gFriendIDList.addAll(friends);
            }

            @Override
            public void onFailed() { }
        });

        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(this);

        AppManager.friendHomeDispIndex = 0;
        loadFragmentByIndex(0);
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
        dialog.setNeutralButton(getResources().getString(R.string.alert_no), (dialogInterface, i) -> { });
        dialog.show();
    }

    public void loadFragmentByIndex(int index) {
        Fragment frg = null;
        switch (index) {
            case 0:
                frg = new FriendsFragment(this);
                break;
            case 1:
                frg = new StatisticsFragment(this);
                break;
            case 2:
                frg = new RiskZoneFragment(this);
                break;
            case 3:
                frgProfile = new ProfileFragment(this);
                frg = frgProfile;
                break;
        }

        onLoadFragment(frg);
    }

    public void gotoQRScannFragment() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            QRScannFragment frgQRScann = new QRScannFragment(MainActivity.this);
            onLoadFragment(frgQRScann);
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, this.CAMERA_SCANN_ID);
        }
    }

    private void checkLocationPerssion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, this.FIND_LOCATION_ID);
                return;
            }
        }
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1,
                100, mLocationListener);
    }

    public void gotoFriendFragment() {
        Fragment frg = new ViewFriendInfoFragment(this);
        onLoadFragment(frg);
    }

    public void gotoAcceptedFragment(Users user) {
        Fragment frg = new AcceptFriendFragment(this, user);
        onLoadFragment(frg);
    }

    public void gotoQuizFragment() {
        Fragment frg = new QuizFragment(this);
        onLoadFragment(frg);
    }

    public void gotoQuizStatusFragment(QuizModel model) {
        Fragment frg = new ViewQuizStatusFragment(this, model);
        onLoadFragment(frg);
    }

    private void onLoadFragment(Fragment frg) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction frgTran = fm.beginTransaction();
        frgTran.replace(R.id.frg_main, frg);
        frgTran.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_SCANN_ID) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                gotoQRScannFragment();
            } else {
                Toast.makeText(this, getString(R.string.toast_camera_permission_denied), Toast.LENGTH_LONG).show();
            }
        }

        if (requestCode == FIND_LOCATION_ID) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkLocationPerssion();
            } else {
                Toast.makeText(this, getString(R.string.toast_location_permission_denided), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (onBackPressed == null) {
            return;
        }
        onBackPressed.onBackPressed();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_friends:
                AppManager.friendHomeDispIndex = 0;
                loadFragmentByIndex(0);
                break;
            case R.id.action_quiz:
                loadFragmentByIndex(1);
                break;
            case R.id.action_statistics:
                loadFragmentByIndex(2);
                break;
            case R.id.action_profile:
                loadFragmentByIndex(3);
                break;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri resultUri = result.getUri();
                frgProfile.onUpdateProfilePhoto(resultUri);
            }else{
                frgProfile.onFailureUpdateProfilePhoto();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_top, menu);
        return true;
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    public void setOnBackPressed(MainOnBackPressed onBackPressed) {
        this.onBackPressed = onBackPressed;
    }

    public interface MainOnBackPressed {
        void onBackPressed();
    }

}
