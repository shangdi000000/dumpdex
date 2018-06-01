package com.dump.bean;

import com.google.gson.annotations.SerializedName;

/**
 * Created by huan on 2018/6/1.
 */

public class ApkResult {
    @SerializedName("TCLHash")
    public String tCLHash;
    @SerializedName("Risk")
    public String risk;
    @SerializedName("VirusName")
    public String virusName;
}
