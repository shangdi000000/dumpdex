package com.dump.http;

import android.util.Log;

import com.dump.bean.ApkResult;
import com.dump.bean.HashBean;
import com.dump.utils.RetrofitLogger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.functions.Function;
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


    public Observable<ApiResponse<List<ApkResult>>> scanTclhash(List<HashBean> hashList, String appPackageName) {


        JSONObject result = new JSONObject();
        try {
            JSONArray hashJson = new JSONArray();
            for (HashBean bean : hashList) {
                hashJson.put(bean.hash);
            }
            Log.d("HttpServiceManager", "TCLHashList : " + hashJson.toString());
            result.put("TCLHashList", hashJson);
            result.put("AiEngineSDK", getSdkEngine(appPackageName));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), result.toString());

        return getService().scanTclhash(body);
    }


    public ObservableSource<List<ApkResult>> scanFeature(JSONArray apklist, String appPackageName, final List<ApkResult> dumpFailure) {
        if (apklist.length() == 0) {
            return new ObservableSource<List<ApkResult>>() {
                @Override
                public void subscribe(Observer<? super List<ApkResult>> observer) {
                    observer.onNext(dumpFailure);
                    observer.onComplete();
                }
            };
        }

        JSONObject result = new JSONObject();
        try {
            Log.d("HttpServiceManager", "TCLHashList : " + apklist.toString());
            result.put("ApkList", apklist);
            result.put("AiEngineSDK", getSdkEngine(appPackageName));
            result.put("Basic", getBasic());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), result.toString());

        return getService().scanFeature(body).flatMap(new Function<ApiResponse<List<ApkResult>>, ObservableSource<List<ApkResult>>>() {
            @Override
            public ObservableSource<List<ApkResult>> apply(final ApiResponse<List<ApkResult>> listApiResponse) throws Exception {
                if (listApiResponse.isSuccess()){
                    return new ObservableSource<List<ApkResult>>() {
                        @Override
                        public void subscribe(Observer<? super List<ApkResult>> observer) {
                            dumpFailure.addAll(listApiResponse.scanRes);
                            observer.onNext(dumpFailure);
                            observer.onComplete();
                        }
                    };
                } else {
                    return new ObservableSource<List<ApkResult>>() {
                        @Override
                        public void subscribe(Observer<? super List<ApkResult>> observer) {
                            observer.onError(new RuntimeException("error code : " + listApiResponse.status));
                        }
                    };
                }

            }
        });
    }




    private ApiService getService() {
        return mRetrofit.create(ApiService.class);
    }

    private JSONObject getSdkEngine(String appPackageName){

        JSONObject sdkJson = new JSONObject();
        try {
            sdkJson.put("PkgName", appPackageName);
            sdkJson.put("VersionName", "v1.0.0");
            sdkJson.put("VersionCode", "1");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("HttpServiceManager", "AiEngineSDK : " + sdkJson.toString());
        return sdkJson;
    }

    /**
     * Basic : {                  // 手机信息
         Brand      string      // 手机品牌
         Model      string      // 手机型号
         Language   string      // 手机设置的语言
         Country    string      // 手机当前国家
         NetWork    string      // "2G|3G|4G|5G|WIFI"
         AndoridID  string      // 安卓ID
         AndroidSDK string      // 安卓SDK版本
     }
     * @return
     */
    private JSONObject getBasic(){

        JSONObject sdkJson = new JSONObject();
        try {
            sdkJson.put("Brand", "-");
            sdkJson.put("Model", "-");
            sdkJson.put("Language", "-");
            sdkJson.put("Country", "-");
            sdkJson.put("NetWork", "-");
            sdkJson.put("AndoridID", "-");
            sdkJson.put("AndroidSDK", "-");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("HttpServiceManager", "AiEngineSDK : " + sdkJson.toString());
        return sdkJson;
    }
}
