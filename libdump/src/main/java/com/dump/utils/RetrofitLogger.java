package com.dump.utils;

import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * Created by Huan on 16/7/13.
 */
public class RetrofitLogger implements Interceptor {
    public static final String TAG = "OkHttpUtils";
    private String tag;
    private boolean showResponse;

    private int logColum;      // -1时 全部打印

    public RetrofitLogger(String tag, boolean showResponse, int logColum) {
        if (TextUtils.isEmpty(tag)) {
            tag = TAG;
        }
        this.showResponse = showResponse;
        this.tag = tag;
        this.logColum = logColum;
    }

    public RetrofitLogger(String tag, boolean showResponse) {
        this(tag, showResponse, -1);
    }

    public RetrofitLogger(String tag) {
        this(tag, false);
    }

    @Override
    public okhttp3.Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        long requestTime = System.currentTimeMillis();
        StringBuilder log = new StringBuilder();
        logForRequest(request, log);
        try {
            okhttp3.Response response = chain.proceed(request);
            return logForResponse(request, requestTime, response);
        } catch (SocketTimeoutException e) {
            e.printStackTrace();
        }

        return logForResponse(request, requestTime, chain.proceed(request));

    }

    private okhttp3.Response logForResponse(Request request, long requestTime, okhttp3.Response response) {
        StringBuilder log = new StringBuilder();
        log.append("\n");
        try {
            //===>response log
             Log.e(tag, "========response'log=======");

            okhttp3.Response.Builder builder = response.newBuilder();
            okhttp3.Response clone = builder.build();
            log.append("response code : " + clone.code() + "\n");
//            Log.e(tag, "protocol : " + clone.protocol());
//            if (!TextUtils.isEmpty(clone.message()))
//                Log.e(tag, "message : " + clone.message());

            if (showResponse) {
                ResponseBody body = clone.body();
                if (body != null) {
                    MediaType mediaType = body.contentType();
                    if (mediaType != null) {
                        //log.append("responseBody's contentType : " + mediaType.toString()+"\n");
                        if (isText(mediaType)) {
                            String resp = body.string();
                            if (resp.length() < 100) {
                                log.append("responseBody : " + resp + "\n");
                            } else {
                                if (logColum != -1) {
                                    log.append("responseBody : " + resp.substring(0, logColum) + " ...\n");
                                } else {
                                    log.append("responseBody : " + resp);
                                }
                            }

                            log.append("\n");
                            log.append("request use : " + (System.currentTimeMillis() - requestTime) + " ms \n");

                            body = ResponseBody.create(mediaType, resp);
                            Log.d(TAG, TAG + log.toString());
                            return response.newBuilder().body(body).build();
                        } else {
                            log.append("responseBody's content : \" + \" maybe [fileInfos part] , too large too print , ignored! ");
                            Log.d(TAG, TAG + log.toString());
                        }
                    }
                }
            }

             Log.e(tag, "========response'log=======end");
        } catch (Exception e) {
//            e.printStackTrace();
        }
        return response;
    }

    private void logForRequest(Request request, StringBuilder log) {
        try {
            String url = request.url().toString();
            Headers headers = request.headers();
            Log.e(tag, "========request'log=======");
//            Log.e(tag, "method : " + request.method());


            log.append("request url : " + url + "\n");

            if (headers != null && headers.size() > 0) {
                log.append("request headers : " + headers.toString());
            }
            RequestBody requestBody = request.body();
            if (requestBody != null) {
                MediaType mediaType = requestBody.contentType();
                if (mediaType != null) {
                    //log.append("requestBody's contentType : " + mediaType.toString()+"\n");
                    if (isText(mediaType)) {
                        log.append("\nrequestBody's content : " + bodyToString(request));
                    } else {
                        log.append("\nrequestBody's content : " + " maybe [fileInfos part] , too large too print , ignored!");
                    }
                }
            }
            Log.e(tag, "request : " + log.toString());
            Log.e(tag, "========request'log=======end");
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private boolean isText(MediaType mediaType) {
        if (mediaType.type() != null && mediaType.type().equals("text")) {
            return true;
        }
        if (mediaType.subtype() != null) {
            if (mediaType.subtype().equals("json") ||
                    mediaType.subtype().equals("xml") ||
                    mediaType.subtype().equals("html") ||
                    mediaType.subtype().equals("webviewhtml")
                    )
                return true;
        }
        return false;
    }

    private String bodyToString(final Request request) {
        try {
            final Request copy = request.newBuilder().build();
            final Buffer buffer = new Buffer();
            copy.body().writeTo(buffer);
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "something error when show requestBody.";
        }
    }
}
