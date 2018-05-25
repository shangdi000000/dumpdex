package com.dump.http;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by huan on 2018/5/23.
 */

public interface ApiService {
    @GET("users/{user}/repos")
    Observable<List<Object>> listRepos(@Path("user") String user);
}
