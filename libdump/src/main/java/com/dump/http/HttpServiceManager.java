package com.dump.http;

import android.util.Log;

import com.dump.bean.ApkResult;
import com.dump.utils.RetrofitLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by huan on 2018/05/24.
 */

public class HttpServiceManager {

    public static final String TAG = "StoreService";
    public static final String BASE_URL = "http://10.115.132.28:8888/hi_ai_engine_api/";

    public static HttpServiceManager instance = null;
    private Retrofit mRetrofit;


    public HttpServiceManager() {

        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.addInterceptor(new RetrofitLogger("HttpServiceManager", true));
        builder.connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(0, 5, TimeUnit.MINUTES));
        OkHttpClient client = builder.build();

        Gson gson = new GsonBuilder().registerTypeAdapter(ApiResponse.class,
                new ApiResponseDeserializer()).create();
        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
    }

    public synchronized static HttpServiceManager getInstance() {
        if (instance == null) {
            instance = new HttpServiceManager();
        }
        return instance;
    }


    public Observable<ApiResponse<List<ApkResult>>> scanTclhash() {


        JSONObject result = new JSONObject();
        try {
            JSONArray hashJson = new JSONArray();
            hashJson.put("ABCDEF1234567890");
            hashJson.put("ABCDEF1234567891");
            Log.d("HttpServiceManager", "TCLHashList : " + hashJson.toString());
            result.put("TCLHashList", hashJson.toString());


            JSONObject sdkJson = new JSONObject();
            sdkJson.put("PkgName", "com.h.application");
            sdkJson.put("VersionName", "v1.0.0");
            sdkJson.put("VersionCode", "1");
            Log.d("HttpServiceManager", "AiEngineSDK : " + sdkJson.toString());
            result.put("AiEngineSDK", sdkJson.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), result.toString());

        return getService().scanTclhash(body);
    }


    private ApiService getService() {
        return mRetrofit.create(ApiService.class);
    }


}
