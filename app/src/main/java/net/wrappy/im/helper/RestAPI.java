package net.wrappy.im.helper;

import android.os.AsyncTask;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by ben on 13/11/2017.
 */

public class RestAPI {

    public static String GET_QUESTIONS_SECURITY = "http://www.mocky.io/v2/5a0a619a2e00009219489c7e";
    public static String POST_QUESTION_ANSWERS = "http://www.mocky.io/v2/5a0a65fa2e0000391a489c94";
    public static String POST_REGISTER = "http://www.mocky.io/v2/5a0d513b2e000052213a2ddd";
    public static String PUT_UPDATEPASS ="http://www.mocky.io/v2/5a0becc03200006b22e96545";
    public static String POST_UPDATE_EMAIL_USERNAME = "http://www.mocky.io/v2/5a0a67ce2e0000ab1a489c97";

    public interface RectAPIListenner {
        public void OnComplete(String error, String s);
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


    public static class PostDataUrl extends AsyncTask<String, String, String> {

        HttpURLConnection conn;

        String json="";
        String error = null;
        RectAPIListenner listenner;


        public PostDataUrl(String json, RectAPIListenner listenner) {
            this.json = json;
            this.listenner = listenner;
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
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setDoOutput(true);
                conn.setDoInput(true);

                conn.connect();

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                os.writeBytes("");

                os.flush();
                os.close();

                int statusCode = conn.getResponseCode();
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
                listenner.OnComplete(error,s);
            }
        }
    }

    public static class PutDataUrl extends AsyncTask<String, String, String> {

        HttpURLConnection conn;

        String json="";
        String error = null;
        RectAPIListenner listenner;


        public PutDataUrl(String json, RectAPIListenner listenner) {
            this.json = json;
            this.listenner = listenner;
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
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setDoOutput(true);
                conn.setDoInput(true);

                conn.connect();

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                os.writeBytes("");

                os.flush();
                os.close();

                int statusCode = conn.getResponseCode();
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
                listenner.OnComplete(error,s);
            }
        }
    }

    public static class GetDataUrl extends AsyncTask<String, String, String> {

        HttpURLConnection conn;
        String error = null;
        RectAPIListenner listenner;

        public GetDataUrl(RectAPIListenner listenner) {
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
                listenner.OnComplete(error,s);
            }
        }
    }

}
