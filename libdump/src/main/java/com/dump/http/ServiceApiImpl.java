package com.dump.http;

import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
/**
 * Created by huan on 2018/05/24.
 */

public class ServiceApiImpl {

    public static final String TAG = "StoreService";

    public static ServiceApiImpl instance = null;
    private Retrofit mRetrofit;


    public ServiceApiImpl() {

        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.connectTimeout(10, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .connectionPool(new ConnectionPool(0, 5, TimeUnit.MINUTES));
        OkHttpClient client = builder.build();

        mRetrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com/")
                .client(client)
                .build();
    }

    public synchronized static ServiceApiImpl getInstance() {
        if (instance == null) {
            instance = new ServiceApiImpl();
        }
        return instance;
    }


    public void getDemo(){
        getService().listRepos("fsdfsd");
    }


    private ApiService getService() {
        return mRetrofit.create(ApiService.class);
    }



}
