package com.dump.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by huan on 2018/6/1.
 */

public class PackageUtils {
    /**
     * 通过pkg 获取pakgeinfo
     *
     * @param context
     * @param pkg
     * @return
     */
    public static PackageInfo getPackageInfoByPkg(Context context, String pkg) {
        PackageInfo info = null;
        try {
            PackageManager pm = context.getPackageManager();
            info = pm.getPackageInfo(pkg, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return info;
    }

//    public String getVersion(Context context) {
//        try {
//            PackageManager manager = context.getPackageManager();
//            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
//            String version = info.versionName;
//            return this.getString(R.string.version_name) + version;
//        } catch (Exception e) {
//            e.printStackTrace();
//            return this.getString(R.string.can_not_find_version_name);
//        }
//    }

}
