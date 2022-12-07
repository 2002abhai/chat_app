package com.example.chat_app.firebase;

import android.app.Activity;
import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FCMSend {

        String userFcmToken;
        String title;
        String body;
        Context mContext;
        Activity mActivity;


        private RequestQueue requestQueue;
        private final String postUrl = "https://fcm.googleapis.com/fcm/send";
        private final String fcmServerKey ="key= AAAA5Hnm8Zw:APA91bEvTay-_wtVX3arp5i1ZsR7FKNtWogKh9AZ300raTbkOVZfdqRoa30-SY9udaNCfeLbtKiOqwY64YxvgOxsGpbfnMk2LSDCyPWSiF6n5ViKdnMNkdlNsz1YtQW7ee6ZRvonz-3x";

        public FCMSend(String userFcmToken, String title, String body, Context mContext, Activity mActivity) {
            this.userFcmToken = userFcmToken;
            this.title = title;
            this.body = body;
            this.mContext = mContext;
            this.mActivity = mActivity;


        }

        public void SendNotifications() {

            requestQueue = Volley.newRequestQueue(mActivity);
            JSONObject mainObj = new JSONObject();
            JSONObject DataObj = new JSONObject();
            try {
                JSONObject notiObject = new JSONObject();
                notiObject.put("title", title);
                notiObject.put("body", body);
                notiObject.put("icon", "icon"); // enter icon that exists in drawable only

                mainObj.put("to", userFcmToken);
//                mainObj.put("notification", notiObject);
                mainObj.put("data", notiObject);
                mainObj.put("priority", "high");

//                DataObj.put("message",mainObj);

                JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, postUrl, mainObj, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        // code run is got response

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // code run is got error

                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {

                        Map<String, String> header = new HashMap<>();
                        header.put("Content-Type", "application/json");
                        header.put("Authorization",  fcmServerKey);

                        return header;
                    }
                };
                requestQueue.add(request);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
}
