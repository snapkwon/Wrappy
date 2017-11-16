package net.wrappy.im.helper;

import android.os.AsyncTask;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by ben on 13/11/2017.
 */

public class RestAPI {

    public static String GET_QUESTIONS_SECURITY = "http://www.mocky.io/v2/5a0a619a2e00009219489c7e";
    public static String POST_QUESTION_ANSWERS = "http://www.mocky.io/v2/5a0a65fa2e0000391a489c94";
    public static String POST_UPDATE_EMAIL_USERNAME = "http://www.mocky.io/v2/5a0a67ce2e0000ab1a489c97";

    public interface RestAPIListenner {
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
        RestAPIListenner listenner;


        public PostDataUrl(String json, RestAPIListenner listenner) {
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
                conn.setDoOutput(true);
                conn.setDoInput(true);

                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                //os.writeBytes(URLEncoder.encode(jsonParam.toString(), "UTF-8"));
                os.writeBytes(json.trim());

                os.flush();
                os.close();

                BufferedReader reader=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
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
                listenner.OnComplete(error,s);
            }
        }
    }

}
