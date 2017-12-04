package net.wrappy.im.helper;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import net.wrappy.im.model.WpkToken;
import net.wrappy.im.provider.Store;

import java.lang.reflect.Type;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by ben on 13/11/2017.
 */

public class RestAPI {

    public static String root_url = "https://webserv-ci.proteusiondev.com:8081/8EF640C4836D96CE990B71F60E0EA1DB/";

    public static String GET_SEARCH_USERNAME = root_url + "member/%s";// identifier
    public static String POST_ADD_CONTACT = root_url + "chat/roster/%s";// account
    public static String GET_QUESTIONS_SECURITY = root_url + "master/security";
    public static String POST_REFRESH_TOKEN = root_url + "/oauth/token?grant_type=refresh_token&refresh_token=%s&scope=all";
    public static String POST_REGISTER = root_url + "member/registration";
    public static String POST_LOGIN = root_url + "oauth/token?grant_type=password&username=%s&password=%s&scope=all";
    public static String PUT_UPDATEPASS = "http://www.mocky.io/v2/5a0becc03200006b22e96545";
    public static String POST_UPDATE_EMAIL_USERNAME = "http://www.mocky.io/v2/5a0e8572300000de204335a8";

    public static String loginUrl(String user, String pass) {
        return String.format(POST_LOGIN, user, pass);
    }

    private static String refreshTokenUrl(Context context) {
        String refreshToken = Store.getStringData(context, WpkToken.STORE_REFRESH_TOKEN);
        return String.format(POST_REFRESH_TOKEN, refreshToken);
    }

    public interface RestAPIListenner {
        public void OnComplete(int httpCode, String error, String s);
    }

    public static JsonElement getData(JsonObject jsonObject) {
        return jsonObject.get("data");
    }

    public static boolean checkHttpCode(int code) {
        return ((code == 200) || (code == 201)) ? true : false;
    }

    public static TrustManager[] trustAllCerts = new X509TrustManager[]{new X509TrustManager() {

        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {
        }

        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {
        }

        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }};


    public static javax.net.ssl.SSLSocketFactory getSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext inst = SSLContext.getInstance("TLS");
        inst.init(null, trustAllCerts, null);
        return inst.getSocketFactory();
    }

    public static SSLContext getSSLContextInst() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext inst = SSLContext.getInstance("TLS");
        inst.init(null, trustAllCerts, null);
        return inst;
    }

    public static void initIon(Context context) {
        try {
            Ion.getDefault(context).configure().createSSLContext("TLS");
            Ion.getDefault(context).getHttpClient().getSSLSocketMiddleware().setSSLContext(getSSLContextInst());
            Ion.getDefault(context).getHttpClient().getSSLSocketMiddleware().setTrustManagers(trustAllCerts);
            Ion.getDefault(context).getHttpClient().getSSLSocketMiddleware().setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static String getHeaderHttps(Context context, String url) {
        String header = Store.getStringData(context, WpkToken.STORE_TOKEN_TYPE) + " " + Store.getStringData(context, WpkToken.STORE_ACCESS_TOKEN);
        if (url.contains("oauth/token?grant_type=password") || url.startsWith(POST_REGISTER)) {
            header = "Basic d3JhcHB5X2FwcDp3cmFwcHlfYXBw";
        }
        return header;
    }

    public static void PostDataWrappy(Context context, JsonObject jsonObject, String url, final RestAPIListenner listenner) {
        String header = getHeaderHttps(context,url);
        Ion.with(context).load(url).setTimeout(10000).addHeader("Authorization",header)
                .setJsonObjectBody((jsonObject==null)? new JsonObject() : jsonObject)
        .asString().withResponse().setCallback(new FutureCallback<Response<String>>() {
            @Override
            public void onCompleted(Exception e, Response<String> result) {
                try {
                    listenner.OnComplete((result!=null) ? result.getHeaders().code() : 0,(e!=null)? e.getLocalizedMessage() : null,(result!=null) ? result.getResult() : null);
                }catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });
    }

    public static void GetDataWrappy(Context context, String url, final RestAPIListenner listenner) {
        String header = getHeaderHttps(context,url);
        Ion.with(context).load(url).setTimeout(10000).addHeader("Authorization",header).asString().withResponse().setCallback(new FutureCallback<Response<String>>() {
            @Override
            public void onCompleted(Exception e, Response<String> result) {
                listenner.OnComplete((result != null) ? result.getHeaders().code() : 0, (e != null) ? e.getLocalizedMessage() : null, (result != null) ? result.getResult() : null);
            }
        });
    }

    public static void refreshTokenHttps(final Context context) {
        Ion.with(context).load(refreshTokenUrl(context)).addHeader("Authorization", "Basic d3JhcHB5X2FwcDp3cmFwcHlfYXBw").asString().withResponse().setCallback(new FutureCallback<Response<String>>() {
            @Override
            public void onCompleted(Exception e, Response<String> result) {
                try {
                    int httpCode = (result != null) ? result.getHeaders().code() : 0;
                    String error = (e != null) ? e.getLocalizedMessage() : "";
                    if (!RestAPI.checkHttpCode(httpCode)) {
                        AppFuncs.alert(context, error, true);
                        return;
                    }
                    JsonObject jsonObject = (new JsonParser()).parse(result.getResult()).getAsJsonObject();
                    Gson gson = new Gson();
                    WpkToken wpkToken = gson.fromJson(jsonObject, WpkToken.class);
                    wpkToken.saveToken(context);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    public static HashMap<String, String> jsonToMap(JsonObject object) {
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        return gson.fromJson(object, type);
    }

}
