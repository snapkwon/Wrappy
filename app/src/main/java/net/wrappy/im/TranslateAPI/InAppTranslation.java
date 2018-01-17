package net.wrappy.im.TranslateAPI;

import android.content.Context;
import android.text.TextUtils;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Response;

import net.wrappy.im.BuildConfig;
import net.wrappy.im.helper.RestAPI;
import net.wrappy.im.model.BaseObject;
import net.wrappy.im.model.translate.DetectLanguageResponse;
import net.wrappy.im.model.translate.TranslateLanguageResponse;
import net.wrappy.im.util.Debug;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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


    public InAppTranslation(Context context, CompleteTransaction complete) {
        m_context = context;
        this.callback = complete;
        target = "en";

    }

    public void setSourceLanguage(String languageCode) {
        source = languageCode;
    }

    public void setTargetLanguage(String languageCode) {
        target = languageCode;
    }

    public static void localize(String text) {
        //getInstance().translate(text, null, null, view, TargetTextType.Hint);
    }

    public void detectLanguage(final String query, final int position) {
        if (query == null) {
            return;
        }

        if (!isValidTranslate(query, position))
            return;

        try {
            String queryEncoded = URLEncoder.encode(query, "utf-8");
            RestAPI.apiGETDetectLanguage(m_context, String.format(RestAPI.DETECT_LANGUAGE, BuildConfig.GoogleTranslateApiKey, queryEncoded))
                    .setCallback(new FutureCallback<Response<BaseObject<DetectLanguageResponse>>>() {
                        @Override
                        public void onCompleted(Exception e, Response<BaseObject<DetectLanguageResponse>> result) {
                            if (result != null && result.getResult() != null) {
                                DetectLanguageResponse response = result.getResult().getData();
                                if (response!=null && !response.getDetections().isEmpty() && !response.getDetections().get(0).isEmpty()) {
                                    DetectLanguageResponse.Detection detection = response.getDetections().get(0).get(0);
                                    onCallbackDetect(detection.getLanguage(), query, position);
                                } else onCallbackDetect(query, query, position);
                            } else onCallbackDetect(query, query, position);
                        }
                    });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void onCallbackDetect(String result, String source, int position) {
        m_result = result;
        callback.onTaskDetectComplete(result, source, position);
    }

    public void translate(final String query, String source, String target, final int position) {
        if (source == null) {
            source = this.source;
        }

        if (target == null) {
            target = this.target;
        }

        if (!isValidTranslate(query, position))
            return;

        try {
            String queryEncoded = URLEncoder.encode(query, "utf-8");
            String url = TextUtils.isEmpty(source) ? RestAPI.TRANSLATE_LANGUAGE_NO_SOURCE : RestAPI.TRANSLATE_LANGUAGE;
            url = TextUtils.isEmpty(source) ? String.format(url, BuildConfig.GoogleTranslateApiKey, target, queryEncoded) : String.format(url, BuildConfig.GoogleTranslateApiKey, source, target, queryEncoded);
            RestAPI.apiGETTranslateLanguage(m_context, url).setCallback(new FutureCallback<Response<BaseObject<TranslateLanguageResponse>>>() {
                @Override
                public void onCompleted(Exception e, Response<BaseObject<TranslateLanguageResponse>> result) {
                    if (result != null && result.getResult() != null) {
                        TranslateLanguageResponse response = result.getResult().getData();
                        if (response != null && !response.getTranslations().isEmpty()) {
                            onCallBackTranslate(response.getTranslations().get(0).getTranslatedText(), position);
                        } else
                            onCallBackTranslate(query, position);
                    } else
                        onCallBackTranslate(query, position);
                }
            });
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private boolean isValidTranslate(String query, int position) {
        String apiKey = BuildConfig.GoogleTranslateApiKey;
        if (TextUtils.isEmpty(apiKey) || TextUtils.isEmpty(query)) {
            Debug.d("Google Translate Api Key is not set in local.properties or empty translate text");
            onCallBackTranslate(query, position);
            return false;
        }
        return true;
    }

    private void onCallBackTranslate(String query, int position) {
        m_result = query;
        callback.onTaskTranslateComplete(query, position);
    }

//    public void translate(List<String> query, String source, String target, int position) {
//        if (source == null) {
//            source = this.source;
//        }
//
//        if (target == null) {
//            target = this.target;
//        }
//
//        GetArrayTranslationAsyncTask task = new GetArrayTranslationAsyncTask() {
//            @Override
//            protected void onPostExecute(List<String> result) {
//                if (result != null) {
//                    listresult = result;
//                    callback.onTaskLListTranslateComplete(result);
//                }
//            }
//        };
//        task.execute(query, source, target);
//    }


    public interface CompleteTransaction {
        void onTaskTranslateComplete(String result, int position);
        void onTaskDetectComplete(String result, String src, int position);
    }
}
