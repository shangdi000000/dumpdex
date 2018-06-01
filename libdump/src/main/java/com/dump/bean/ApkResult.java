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

    public String dumpRet = "0";

    public boolean needDump(){
        return "-1".equals(risk);
    }


    public ApkResult(String tCLHash, String risk, String virusName, String dumpRet) {
        this.tCLHash = tCLHash;
        this.risk = risk;
        this.virusName = virusName;
        this.dumpRet = dumpRet;
    }

    @Override
    public String toString() {
        return "ApkResult{" +
                "tCLHash='" + tCLHash + '\'' +
                ", risk='" + risk + '\'' +
                ", virusName='" + virusName + '\'' +
                ", dumpRet='" + dumpRet + '\'' +
                '}';
    }
}
