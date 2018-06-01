package com.dump.http;

import com.dump.bean.ApkResult;

import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Created by huan on 2018/5/23.
 */

public interface ApiService {

    @POST("v1.0/scan_feature")
    Observable<ApiResponse<List<ApkResult>>> scanFeature(@Body RequestBody body);
    @POST("v1.0/scan_tclhash")
    Observable<ApiResponse<List<ApkResult>>> scanTclhash(@Body RequestBody body);
}
