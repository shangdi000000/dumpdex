package com.h.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.dump.DumpManager;
import com.dump.bean.ApkResult;
import com.dump.bean.HashBean;
import com.dump.listener.DumpListener;
import com.dump.utils.PermissionConstants;
import com.dump.utils.PermissionUtils;


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
//
                        DumpManager.Companion.getInstance(MainActivity.this).dumpApk(hashBeans, new DumpListener() {
                            @Override
                            public void dumpSuccess(List<ApkResult> it) {
                                for (ApkResult apkResult : it) {
                                  Log.e("DumpManager", "result : " + apkResult.toString());
                                }
                            }

                            @Override
                            public void dumpFailure(Throwable it) {
                                it.printStackTrace();

                            }
                        });
                    }

                    @Override
                    public void onDenied() {

                    }
                }).request();

    }




}
