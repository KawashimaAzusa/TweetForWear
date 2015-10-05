package com.example.hashimotomika.tweetforwear;

import android.content.Context;
import android.content.SharedPreferences;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;


/**
 * Created by hashimotomika on 9/19/15.
 */
public class CallRequest {
    private final static String PREF_NAME = "phone_settings";
    private final static String PHONE_NUMBER = "phone_number";

    public static void sendCallRequest(Context context) {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost("http://helpme765pro.appspot.com/call");
        List<NameValuePair> value = new ArrayList<NameValuePair>();
        String phoneNumber = getPhoneNumber(context);
        value.add(new BasicNameValuePair("phoneNumber", phoneNumber));

        String body = null;
        try {
            post.setEntity(new UrlEncodedFormEntity(value, "UTF-8"));
            HttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();
            body = EntityUtils.toString(entity, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        client.getConnectionManager().shutdown();
    }


    public static void storePhoneNumber(Context context, String phoneNumber) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PHONE_NUMBER, phoneNumber);
        editor.commit();
    }

    public static String getPhoneNumber(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String phoneNumber = preferences.getString(PHONE_NUMBER, "");
        return phoneNumber;
    }

    public static boolean hasPhoneNumber(Context context) {
        return true;
    }
}
