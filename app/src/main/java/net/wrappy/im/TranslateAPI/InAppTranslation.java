package net.wrappy.im.TranslateAPI;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import net.wrappy.im.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by PCPV on 12/07/2017.
 */

public class InAppTranslation {


    private static InAppTranslation sharedInstance = null;
    private String source = null;
    private String target = null;
    private String m_result = "";
    private Context m_context;
    CompleteTransaction callback;
    private List<String> listresult;


    public InAppTranslation(Context context , CompleteTransaction complete) {
        m_context = context;
        this.callback = complete;
        target = "en";

    }

    public  void setSourceLanguage(String languageCode){
        source = languageCode;
    }

    public  void setTargetLanguage(String languageCode){
        target = languageCode;
    }

    public static void localize(String text) {
        //getInstance().translate(text, null, null, view, TargetTextType.Hint);
    }

    public void detectlanguage(final String query, final int position) {
        if(query == null){
            return;
        }

        GetDetectLanguageAsyncTask task = new GetDetectLanguageAsyncTask() {
            @Override
            protected void onPostExecute(String result) {
                if(result != null){;
                    m_result = result;
                    callback.onTaskDetectComplete(result,query,position);
                }
            }
        };
        task.execute(query);
    }

    public void translate(String query, String source, String target , final int position) {
        if(source == null){
            source = this.source;
        }

        if(target == null){
            target = this.target;
        }

        GetTranslationAsyncTask task = new GetTranslationAsyncTask() {
            @Override
            protected void onPostExecute(String result) {
                if(result != null){
                    Log.d(TAG, "Translated test is " + result);
                    m_result = result;
                    callback.onTaskTranslateComplete(result, position);
                }
            }
        };
        task.execute(query, source, target);
    }

    public void translate(List<String> query, String source, String target,int position) {
        if(source == null){
            source = this.source;
        }

        if(target == null){
            target = this.target;
        }

        GetArrayTranslationAsyncTask task = new GetArrayTranslationAsyncTask() {
            @Override
            protected void onPostExecute(List<String> result) {
                if(result != null){
                    listresult = result;
                    callback.onTaskLListTranslateComplete(result);
                }
            }
        };
        task.execute(query, source, target);
    }


    public interface CompleteTransaction {
        public void onTaskTranslateComplete(String result, int position);
        public void onTaskDetectComplete(String result,String src,int position);
        public void onTaskLListTranslateComplete(List<String> result);
    }

    private class GetArrayTranslationAsyncTask extends
            AsyncTask<Object, Void, List<String>> {
        final String TAG = "GetArrayTranslationAsyncTask";

        final String urlTemplate = "https://translation.googleapis.com/language/translate/v2?key=%1$s&source=%2$s&target=%3$s&q=%4$s";
        final String urlTemplateWithoutSource = "https://translation.googleapis.com/language/translate/v2?key=%1$s&target=%2$s&q=%3$s";

        public GetArrayTranslationAsyncTask() {

        }

	/*
     * protected void onPreExecute() {
	 *
	 * }
	 */

        protected List<String> doInBackground(Object... params) {
            List<String> query = (List<String>) params[0];
            String source = (String) params[1];
            String target = (String) params[2];

            List<String> translatedText = new ArrayList<>();
            translatedText.clear();
            HttpURLConnection urlConnection = null;

            try {
                for (int i = 0; i < query.size(); i++) {
                    String urlString = "";

                    if (query.get(i) == null || query.get(i).isEmpty()) {
                        return query;
                    }

                    String apiKey = BuildConfig.GoogleTranslateApiKey;
                    if (apiKey == null || apiKey.isEmpty()) {
                        return query;
                    }

                    String queryEncoded = URLEncoder.encode(query.get(i), "utf-8");

                    if (source == null && target != null) {
                        urlString = String.format(urlTemplateWithoutSource, apiKey, target, queryEncoded);
                    } else if (source != null && target != null) {
                        urlString = String.format(urlTemplate, apiKey, source, target, queryEncoded);
                    } else {
                        // query.get(i) == ;
                    }
                    if(source.equals(target))
                    {
                        return query;
                    }

                    URL url = new URL(urlString);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setReadTimeout(10000);
                    urlConnection.setConnectTimeout(20000);
                    urlConnection.setRequestMethod("GET");
                    urlConnection.setInstanceFollowRedirects(true);

                    int resp = urlConnection.getResponseCode();

                    switch (resp) {
                        case HttpURLConnection.HTTP_OK:
                            InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                            StringBuilder result = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                result.append(line);
                            }
                            in.close();

                            JSONTokener json = new JSONTokener(result.toString());
                            if (json != null) {
                                JSONObject rootObject = (JSONObject) json.nextValue();
                                if (rootObject != null) {
                                    JSONObject dataObject = rootObject.getJSONObject("data");
                                    if (dataObject != null) {
                                        JSONArray translations = dataObject.getJSONArray("translations");
                                        for (int j = 0; j < translations.length(); j++) {
                                            JSONObject translation = translations.getJSONObject(j);
                                            translatedText.add(translation.getString("translatedText"));
                                            break;
                                        }
                                    } else {
                                    }
                                } else {
                                }
                            }
                            break;
                        default:
                            InputStream errorIn = new BufferedInputStream(urlConnection.getErrorStream());
                            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorIn));

                            StringBuilder errorResult = new StringBuilder();
                            String errorLine;
                            while ((errorLine = errorReader.readLine()) != null) {
                                errorResult.append(errorLine);
                            }
                            errorIn.close();
                            String errorResponseString = errorResult.toString();

                            String errorText = String.format("Failed to get data from Google Translate. Status code = %d, Response = %s", resp, errorResponseString);
                            break;
                    }
                }
            } catch(Exception e){
                e.printStackTrace();
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            if (translatedText == null) {
                return query;
            } else {
                return translatedText;
            }

        }
    }



    private class GetTranslationAsyncTask extends
            AsyncTask<Object, Void, String> {
        final String TAG = "GetTranslationAsyncTask";

        final String urlTemplate = "https://translation.googleapis.com/language/translate/v2?key=%1$s&source=%2$s&target=%3$s&q=%4$s";
        final String urlTemplateWithoutSource = "https://translation.googleapis.com/language/translate/v2?key=%1$s&target=%2$s&q=%3$s";

        public GetTranslationAsyncTask() {

        }

	/*
     * protected void onPreExecute() {
	 *
	 * }
	 */

        protected String doInBackground(Object... params) {
            String query = (String) params[0];
            String source = (String) params[1];
            String target = (String) params[2];

            String translatedText = null;
            HttpURLConnection urlConnection = null;

            try {
                String urlString;

                if(query == null || query.isEmpty()){
                    Log.d(TAG, "query is null or empty");
                    return query;
                }

                String apiKey = BuildConfig.GoogleTranslateApiKey;
                if(apiKey == null || apiKey.isEmpty()){
                    Log.d(TAG, "Google Translate Api Key is not set in local.properties");
                    return query;
                }

                String queryEncoded = URLEncoder.encode(query, "utf-8");

                if(source == null && target != null){
                    urlString = String.format(urlTemplateWithoutSource, apiKey, target, queryEncoded);
                }else if(source != null && target != null){
                    urlString = String.format(urlTemplate, apiKey, source, target, queryEncoded);
                }else{
                    Log.d(TAG, "The source and target langauges are both not set.");
                    return query;
                }

                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(20000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setInstanceFollowRedirects(true);

                int resp = urlConnection.getResponseCode();

                switch (resp){
                    case HttpURLConnection.HTTP_OK:
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                        in.close();

                        JSONTokener json = new JSONTokener(result.toString());
                        if (json != null) {
                            JSONObject rootObject = (JSONObject) json.nextValue();
                            if(rootObject != null){
                                JSONObject dataObject = rootObject.getJSONObject("data");
                                if(dataObject != null){
                                    JSONArray translations = dataObject.getJSONArray("translations");
                                    for (int i = 0; i < translations.length(); i++) {
                                        JSONObject translation = translations.getJSONObject(i);
                                        translatedText = translation.getString("translatedText");
                                        break;
                                    }
                                }else{
                                    Log.e(TAG, "[Google Translate API] Data is missing in the response" + rootObject.toString());
                                }
                            }else{
                                Log.e(TAG, "[Google Translate API] Root json object is missing in the response");
                            }
                        }
                        break;
                    default:
                        InputStream errorIn = new BufferedInputStream(urlConnection.getErrorStream());
                        BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorIn));

                        StringBuilder errorResult = new StringBuilder();
                        String errorLine;
                        while ((errorLine = errorReader.readLine()) != null) {
                            errorResult.append(errorLine);
                        }
                        errorIn.close();
                        String errorResponseString = errorResult.toString();

                        String errorText = String.format("Failed to get data from Google Translate. Status code = %d, Response = %s", resp, errorResponseString);
                        Log.d(TAG, errorText);
                        break;
                }
            } catch (Exception e) {
                Log.d(TAG, "Got exception while accessing Google Translation");
                e.printStackTrace();
            } finally {
                if(urlConnection != null){
                    urlConnection.disconnect();
                }
            }

            if(translatedText == null){
                return query;
            }else{
                return translatedText;
            }
        }
    }

    private class GetDetectLanguageAsyncTask extends  AsyncTask<Object, Void, String> {

        final String urlTemplate = "https://www.googleapis.com/language/translate/v2/detect?key=%1$s&q=%2$s";

        public GetDetectLanguageAsyncTask() {

        }

        @Override
        protected String doInBackground(Object... params) {
            String query = (String) params[0];
            String languagecode = "";
            HttpURLConnection urlConnection = null;
            try {
                String urlString;

                String apiKey = BuildConfig.GoogleTranslateApiKey;
                if (apiKey == null || apiKey.isEmpty()) {
                    return query;
                }

                String queryEncoded = URLEncoder.encode(query, "utf-8");

                urlString = String.format(urlTemplate, apiKey, queryEncoded);


                URL url = new URL(urlString);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setReadTimeout(10000);
                urlConnection.setConnectTimeout(20000);
                urlConnection.setRequestMethod("GET");
                urlConnection.setInstanceFollowRedirects(true);

                int resp = urlConnection.getResponseCode();

                switch (resp) {
                    case HttpURLConnection.HTTP_OK:
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());

                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                        StringBuilder result = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            result.append(line);
                        }
                        in.close();

                        JSONTokener json = new JSONTokener(result.toString());
                        if (json != null) {
                            JSONObject rootObject = (JSONObject) json.nextValue();
                            if (rootObject != null) {
                                JSONObject dataObject = rootObject.getJSONObject("data");
                                if (dataObject != null) {
                                    JSONArray translations = dataObject.getJSONArray("detections");

                                    JSONArray translation = translations.getJSONArray(0);
                                    for (int i = 0; i < translation.length(); i++) {
                                        JSONObject translationd = translation.getJSONObject(i);
                                        languagecode = translationd.getString("language");
                                        break;
                                    }
                                }
                            }
                        }
                        break;
                    default:
                        InputStream errorIn = new BufferedInputStream(urlConnection.getErrorStream());
                        BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorIn));

                        StringBuilder errorResult = new StringBuilder();
                        String errorLine;
                        while ((errorLine = errorReader.readLine()) != null) {
                            errorResult.append(errorLine);
                        }
                        errorIn.close();
                        String errorResponseString = errorResult.toString();

                        String errorText = String.format("Failed to get data from Google Translate. Status code = %d, Response = %s", resp, errorResponseString);
                        break;
                }

            } catch (Exception e) {

            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                return languagecode;
            }
        }
    }

}
