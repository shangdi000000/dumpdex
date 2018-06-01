package com.dump.http;

import com.google.gson.annotations.SerializedName;

/**
 * Created by huan on 2018/6/1.
 */

public class ApiResponse<T> {
    @SerializedName("Status")
    public String status;
    @SerializedName("ErrMsg")
    public String errMsg;
    @SerializedName("ScanRes")
    public T scanRes;

    @Override
    public String toString() {
        return "ApiResponse{" +
                "status='" + status + '\'' +
                ", errMsg='" + errMsg + '\'' +
                ", scanRes=" + scanRes +
                '}';
    }
}
