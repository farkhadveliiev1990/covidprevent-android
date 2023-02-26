package com.laodev.focus.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.laodev.focus.R;
import com.laodev.focus.Utils.AppManager;
import com.laodev.focus.Utils.FireManager;
import com.laodev.focus.adapters.NotitAdapter;
import com.laodev.focus.models.NotiModel;

import java.util.ArrayList;
import java.util.List;

public class NotificationActivity extends AppCompatActivity {

    private NotitAdapter notitAdapter;
    private List<NotiModel> mNotis = new ArrayList<>();

    private final int CALL_PHONE_ID = 100;
    private String str_phone = "";


    private void initWithDatas() {
        FireManager.onGetNotiFromFB(AppManager.gUserInfo.userId, new FireManager.NotificationListener() {
            @Override
            public void onSuccess(List<NotiModel> notiModels) {
                mNotis.addAll(notiModels);
                notitAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure() {
                Toast.makeText(NotificationActivity.this, getString(R.string.error_server), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        setToolbar();
        initWithView();
        initWithDatas();
    }

    private void initWithView() {
        ListView lst_noti = findViewById(R.id.lst_noti);
        notitAdapter = new NotitAdapter(this, mNotis, new NotitAdapter.NotiAdapterListener() {
            @Override
            public void onCallCovidByPhonenumber(String phone) {
                onCallPhoneNumber(phone);
            }
        });
        lst_noti.setAdapter(notitAdapter);
    }

    private void onCallPhoneNumber(String phone) {
//        str_phone = phone;
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
//                == PackageManager.PERMISSION_GRANTED) {
//            Intent intent = new Intent(Intent.ACTION_CALL);
//            intent.setData(Uri.parse("tel:" + phone));
//            startActivity(intent);
//        } else {
//            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CALL_PHONE}, this.CALL_PHONE_ID);
//        }

    }

    private void setToolbar() {
        Toolbar toolBar = findViewById(R.id.toolbar);

        if (toolBar != null) {
            setSupportActionBar(toolBar);
            toolBar.setNavigationOnClickListener(v -> onBackPressed());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PHONE_ID) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onCallPhoneNumber(str_phone);
            } else {
                Toast.makeText(this, getString(R.string.toast_call_phone_permission), Toast.LENGTH_LONG).show();
            }
        }
    }

}
