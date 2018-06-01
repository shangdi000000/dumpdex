package com.dump.bean;

/**
 * Created by huan on 2018/6/1.
 */

public class HashBean {
    public String packageName;
    public String hash;

    public HashBean(String packageName, String hash) {
        this.packageName = packageName;
        this.hash = hash;
    }

    @Override
    public String toString() {
        return "HashBean{" +
                "packageName='" + packageName + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}
