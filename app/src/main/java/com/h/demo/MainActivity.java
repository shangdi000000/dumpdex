package com.h.demo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.dump.DumpManager;
import com.dump.read.ReadFile;
import com.dump.read.ReadFileThread;
import com.dump.read.ReaderFileListener;
import com.dump.utils.FileUtils;
import com.dump.utils.PermissionConstants;
import com.dump.utils.PermissionUtils;
import com.dump.utils.ShellUtils;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import kotlin.coroutines.experimental.Continuation;
import kotlin.coroutines.experimental.CoroutineContext;

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
        DumpManager.Companion.getInstance(MainActivity.this).dumpApk("", "sdcard/VPN.apk", "");
//        DumpManager.Companion.getInstance(MainActivity.this).dumpApk("", "sdcard/Smartgo.apk", "");
//        DumpManager.Companion.getInstance(MainActivity.this).dumpApk("", "", "");


//                    }
//
//                    @Override
//                    public void onDenied() {
//
//                    }
//                }).request();

    }
}
