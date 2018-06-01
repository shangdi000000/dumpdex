package com.dump.http;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by Huan on 2017/9/29.
 */

public  class ApiResponseDeserializer implements JsonDeserializer<ApiResponse> {

    @Override
    public ApiResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonObject()) {
            JsonObject jsonOb = (JsonObject) json;
            String code = jsonOb.get("Status").getAsString();
            String message = "";
            if (jsonOb.has("ErrMsg")) {
                message = jsonOb.get("ErrMsg").getAsString();
            }
            ApiResponse response = new ApiResponse();
            response.status = code;
            response.errMsg = message;
            Type type = ((ParameterizedType)typeOfT).getActualTypeArguments()[0];

            if ("0".equals(code)) {
                response.scanRes = context.deserialize(jsonOb.get("ScanRes"),type);
            }
            return response;
        }
        return null;
    }
}
