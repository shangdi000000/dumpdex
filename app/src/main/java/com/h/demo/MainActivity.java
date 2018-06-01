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
import com.dump.http.HttpServiceManager;
import com.dump.read.ReadFile;
import com.dump.read.ReadFileThread;
import com.dump.read.ReaderFileListener;
import com.dump.utils.FileUtils;
import com.dump.utils.PermissionConstants;
import com.dump.utils.PermissionUtils;
import com.dump.utils.ShellUtils;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View view) {


//                .callback(new PermissionUtils.SimpleCallback() {
//                    @Override
//                    public void onGranted() {
//        DumpManager.Companion.getInstance(MainActivity.this).dumpApk("", "sdcard/VPN.apk", "");
//        DumpManager.Companion.getInstance(MainActivity.this).dumpApk("", "sdcard/Smartgo.apk", "");
//        DumpManager.Companion.getInstance(MainActivity.this).dumpApk("", "", "");


//                    }
//
//                    @Override
//                    public void onDenied() {
//
//                    }
//                }).request();



        String apkPath = getPackageInfoByPkg(this, "com.h.demo").applicationInfo.publicSourceDir;
        DumpManager.Companion.getInstance(MainActivity.this).dumpApk("", apkPath, "");
//
        Log.d("-----", "--->apkPath: " + apkPath);
//        HttpServiceManager.getInstance().getDemo().subscribeOn()

    }


    /**
     * 通过pkg 获取pakgeinfo
     * @param context
     * @param pkg
     * @return
     */
    public static PackageInfo getPackageInfoByPkg(Context context, String pkg) {
        PackageInfo info = null;
        try {
            PackageManager pm = context.getPackageManager();
            info = pm.getPackageInfo(pkg,0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }


}
