package net.wrappy.im.helper;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import net.wrappy.im.util.GiphyAPI;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by ben on 13/11/2017.
 */

public class RestAPI {

    public static String root_url = "https://webserv-ci.proteusiondev.com:8081/8EF640C4836D96CE990B71F60E0EA1DB/";

    public static String GET_QUESTIONS_SECURITY = "http://www.mocky.io/v2/5a0a619a2e00009219489c7e";
    public static String POST_QUESTION_ANSWERS = "http://www.mocky.io/v2/5a0a65fa2e0000391a489c94";
    public static String POST_REGISTER = root_url + "member/registration";
    public static String POST_LOGIN = root_url + "oauth/token?grant_type=password&username=%s&password=%s&scope=all";
    public static String PUT_UPDATEPASS ="http://www.mocky.io/v2/5a0becc03200006b22e96545";
    public static String POST_UPDATE_EMAIL_USERNAME = "http://www.mocky.io/v2/5a0e8572300000de204335a8";

    public static String loginUrl(String user, String pass) {
        return String.format(POST_LOGIN,user,pass);
    }

    public interface RestAPIListenner {
        public void OnInit();
        public void OnComplete(int httpCode, String error, String s);
    }

    public static JsonElement parseStringToJsonElement(String jsonString) {
        return (new JsonParser()).parse(jsonString);
    }

    public static int getStatus(JsonObject jsonObject) {
        return jsonObject.get("status").getAsInt();
    }

    public static String getDescription(JsonObject jsonObject) {
        return jsonObject.get("description").getAsString();
    }

    public static JsonElement getData(JsonObject jsonObject) {
        return jsonObject.get("data");
    }

    public static boolean checkHttpCode(int code) {
        return ( (code==200) || (code==201) ) ? true : false;
    }

    public static TrustManager[] trustAllCerts = new X509TrustManager[] { new X509TrustManager() {

        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {}

        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType) {}

        public X509Certificate[] getAcceptedIssuers() {return null;}
    } };


    public static javax.net.ssl.SSLSocketFactory getSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext inst = SSLContext.getInstance("TLS");
        inst.init(null, trustAllCerts, null);
        return inst.getSocketFactory();
    }

    public static SSLContext getSSLContextInst() throws NoSuchAlgorithmException, KeyManagementException{
        SSLContext inst = SSLContext.getInstance("TLS");
        inst.init(null, trustAllCerts, null);
        return inst;
    }

    public static void initIon(Context context){
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
        }catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void PostDataWrappy(Context context, JsonObject jsonObject, String url, final RestAPIListenner listenner) {
        Ion.with(context).load(url).addHeader("Authorization","Basic d3JhcHB5X2FwcDp3cmFwcHlfYXBw").setJsonObjectBody((jsonObject==null)? new JsonObject() : jsonObject).asString().withResponse().setCallback(new FutureCallback<Response<String>>() {
            @Override
            public void onCompleted(Exception e, Response<String> result) {
                listenner.OnComplete((result!=null) ? result.getHeaders().code() : null,(e!=null)? e.getLocalizedMessage() : null,(result!=null) ? result.getResult() : null);
            }
        });
    }


    public static class PostDataUrl extends AsyncTask<String, String, String> {

        HttpURLConnection conn;

        String json="";
        String error = null;
        RestAPIListenner listenner;
        int statusCode = 0;

        public PostDataUrl(String json, RestAPIListenner listenner) {
            this.json = json;
            this.listenner = listenner;
            if (listenner!=null) {
                listenner.OnInit();
            }
        }

        @Override
        protected String doInBackground(String... args) {

            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(args[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept","application/json");
                conn.setReadTimeout(5000);
                conn.setConnectTimeout(5000);
                conn.setDoOutput(true);
                conn.setDoInput(true);

                conn.connect();

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                os.writeBytes("");

                os.flush();
                os.close();

                statusCode = conn.getResponseCode();
                if (statusCode ==  200 || statusCode == 201) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                }
            } catch (Exception e) {
                error = e.getLocalizedMessage();
            }
            finally {
                if (conn!=null) {
                    conn.disconnect();
                }
            }


            return result.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (listenner!=null) {
                listenner.OnComplete(statusCode, error,s);
            }
        }
    }

    public static class PutDataUrl extends AsyncTask<String, String, String> {

        HttpURLConnection conn;

        String json="";
        String error = null;
        RestAPIListenner listenner;
        int statusCode = 0;

        public PutDataUrl(String json, RestAPIListenner listenner) {
            this.json = json;
            this.listenner = listenner;
            if (listenner!=null) {
                listenner.OnInit();
            }
        }

        @Override
        protected String doInBackground(String... args) {

            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(args[0]);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT");
                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                conn.setRequestProperty("Accept","application/json");
                conn.setReadTimeout(5000);
                conn.setConnectTimeout(5000);
                conn.setDoOutput(true);
                conn.setDoInput(true);

                conn.connect();

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                os.writeBytes("");

                os.flush();
                os.close();

                statusCode = conn.getResponseCode();
                if (statusCode ==  200 || statusCode == 201) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                }
            } catch (Exception e) {
                error = e.getLocalizedMessage();
            }
            finally {
                if (conn!=null) {
                    conn.disconnect();
                }
            }


            return result.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (listenner!=null) {
                listenner.OnComplete(statusCode, error,s);
            }
        }
    }

    public static class GetDataUrl extends AsyncTask<String, String, String> {

        HttpURLConnection conn;
        String error = null;
        RestAPIListenner listenner;

        public GetDataUrl(RestAPIListenner listenner) {
            this.listenner = listenner;
        }

        @Override
        protected String doInBackground(String... args) {

            StringBuilder result = new StringBuilder();

            try {
                URL url = new URL(args[0]);
                conn = (HttpURLConnection) url.openConnection();
                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            }catch( Exception e) {
                error = e.getLocalizedMessage();
            }
            finally {
                if (conn!=null) {
                    conn.disconnect();
                }
            }


            return result.toString();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (listenner!=null) {
                listenner.OnComplete(201,error,s);
            }
        }
    }

}
