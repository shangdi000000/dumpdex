package com.h.demo;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.dump.DumpManager;
import com.dump.bean.ApkResult;
import com.dump.bean.HashBean;
import com.dump.http.ApiResponse;
import com.dump.http.HttpServiceManager;
import com.dump.read.ReadFile;
import com.dump.read.ReadFileThread;
import com.dump.read.ReaderFileListener;
import com.dump.utils.FileUtils;
import com.dump.utils.PackageUtils;
import com.dump.utils.PermissionConstants;
import com.dump.utils.PermissionUtils;
import com.dump.utils.ShellUtils;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {


            PermissionUtils.permission(PermissionConstants.STORAGE)
                .callback(new PermissionUtils.SimpleCallback() {
                    @Override
                    public void onGranted() {

                        List<HashBean> hashBeans = new ArrayList<>();
                        hashBeans.add(new HashBean("", ""));

                        DumpManager.Companion.getInstance(MainActivity.this).dumpApk(hashBeans);

                    }

                    @Override
                    public void onDenied() {

                    }
                }).request();

    }




}
