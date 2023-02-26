package com.laodev.focus.Utils;

import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class ApiManager {

    public static final String SERVER_URL = "https://nominatim.openstreetmap.org/search.php?";


    public enum APIMethod {
        GET, POST
    }

    public static void onAPIConnectionResponse (String url
            , Map<String, String> params
            , APIMethod method
            , final APIManagerCallback apiResponse)
    {
        if (method == APIMethod.POST) {
            OkHttpUtils.post().url(url)
                    .params(params)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(okhttp3.Call call, Exception e, int id) {
                            apiResponse.onEventInternetError(e);
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            try {
                                JSONObject obj = new JSONObject(response);
                                apiResponse.onEventCallBack(obj);
                            } catch (JSONException e) {
                                apiResponse.onEventServerError(e);
                                e.printStackTrace();
                            }
                        }
                    });
        } else {
            OkHttpUtils.get().url(url)
                    .params(params)
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(okhttp3.Call call, Exception e, int id) {
                            apiResponse.onEventInternetError(e);
                        }

                        @Override
                        public void onResponse(String response, int id) {
                            try {
                                JSONObject obj = new JSONObject(response);
                                apiResponse.onEventCallBack(obj);
                            } catch (JSONException e) {
                                apiResponse.onEventServerError(e);
                                e.printStackTrace();
                            }
                        }
                    });
        }
    }

    public interface APIManagerCallback {
        void onEventCallBack(JSONObject obj);
        void onEventInternetError(Exception e);
        void onEventServerError(Exception e);
    }

}
