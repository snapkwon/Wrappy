package net.wrappy.im.helper;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import com.koushikdutta.ion.builder.Builders;

import net.wrappy.im.BuildConfig;
import net.wrappy.im.ImApp;
import net.wrappy.im.model.BaseObject;
import net.wrappy.im.model.WpkToken;
import net.wrappy.im.model.translate.DetectLanguageResponse;
import net.wrappy.im.model.translate.TranslateLanguageResponse;
import net.wrappy.im.provider.Store;
import net.wrappy.im.util.Constant;
import net.wrappy.im.util.Debug;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.lang.reflect.Type;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.HashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by ben on 13/11/2017.
 */

public class RestAPI {

    private static final int TIME_OUT = 120000;

    private static String root_url = BuildConfig.ROOT_URL;// real

    public static String GET_MEMBER_INFO = root_url + "member";// identifier
    public static String GET_SEARCH_USERNAME = root_url + "member/%s";// identifier
    public static String POST_ADD_CONTACT = root_url + "chat/roster/%s";// account
    public static String DELETE_CONTACT = root_url + "chat/roster/%s";// account
    public static String GET_QUESTIONS_SECURITY = root_url + "master/security";
    private static String POST_REFRESH_TOKEN = root_url + "oauth/token?grant_type=refresh_token&refresh_token=%s&scope=all";
    public static String POST_REGISTER = root_url + "member/registration";
    public static String POST_REGISTER_DEV = root_url + "member/registration";
    private static String POST_LOGIN = root_url + "oauth/token?grant_type=password&username=%s&password=%s&scope=all";
    private static String POST_PHOTO = root_url + "kernal/asset/retain/";
    private static String GET_PHOTO = root_url + "kernal/asset/";
    public static String GET_MEMBER_INFO_BY_JID = root_url + "member/get-detail-by-jid/%s/";
    public static String POST_RESET_PASSWORD = root_url + "member/%s/password/%s/%s/reset/";
    private static String GET_HASH_RESET_PASS = root_url + "member/%s/security/1/%s/2/%s/3/%s/password/reset";
    public static String PIN_CONVERSATION = root_url + "chat/pin/%s";// XMPP ID
    public static String GET_RANDOM_2_QUESTIONS = root_url + "member/security/";
    private static String GET_FORGET_PASS_SEND_EMAIL = root_url + "member/%s/%s/password/mail";
    public static String GET_COUNTRY_CODES = root_url + "master/country";
    public static String GET_TYPE_ROSTER = root_url + "chat/roster/group/type";
    public static String POST_ROSTER_CREATE = root_url + "chat/roster/group/add";
    private static String GET_MEMBER_BY_JID = root_url + "member/find-by-jid/%s/";
    public static String POST_CHANGE_QUESTION_CHECK = root_url + "member/security/check";
    public static String PUT_CHANGE_SECURITY_QUESTION = root_url + "member/security/";
    private static String POST_FORGET_PASS_CHECK_QUESTIONS = root_url + "/member/%s/security/password/reset";
    public static String POST_CHECK_OBJECTIONABLE = root_url + "chat/check-objectionable";
    public static String POST_REPORT_MESSAGE = root_url + "chat/report";
    public static String GET_POPUP_NOTICE = root_url + "kernal/notice";
    public static String GET_LIST_CONTACT = root_url + "chat/roster";
    public static String DELETE_AVATAR = root_url + "member/avatar";
    public static String DELETE_BANNER = root_url + "member/banner";
    public static String GET_GROUP_BY_XMPP_ID = root_url + "chat/group/%s/by-xmpp-group-id";
    public static String CHAT_GROUP = root_url + "chat/group";
    private static String POST_VERIFY_CODE = root_url + "member/sms/active/%s/%s";
    private static String POST_VERIFY_CODE_CHANGE_PHONE = root_url + "member/mobile/update/%s/%s/%s";
    private static String POST_VERIFY_RESEND_CODE = root_url + "/member/sms/resend/%s/%s";
    public static String GET_PROMOTION_HISTORY = root_url + "member/promotion/invitation/award_history";
    public static String GET_PROMOTION_SETTING = root_url + "master/promotion/setting";
    private static String GET_PROMOTION_CONTENT = root_url + "member/%s/promotion-content";
    public static String GET_PROMOTION_BALANCE = root_url + "member/promotion/invitation/bonus_balance";
    public static String DELETE_MEMBER_GROUP = root_url + "chat/group/%s/participator/%s";
    public static String ADD_MEMBER_TO_GROUP = root_url + "chat/group/%s/";
    public static String REFERRAL = root_url + "member/inviter/%s";
    public static String ADD_MEMBER_TO_GROUP_CHAT = root_url + "chat/group/%s/participator/%s";

    public static String CHECK_PASSCODE = root_url + "member/login/%s/check-passcode/";
    public static String CREATE_PASSCODE = root_url + "member/%s/check-passcode/";

    public static String DETECT_LANGUAGE = "https://www.googleapis.com/language/translate/v2/detect?key=%1$s&q=%2$s";
    public static String TRANSLATE_LANGUAGE = "https://translation.googleapis.com/language/translate/v2?key=%1$s&source=%2$s&target=%3$s&q=%4$s";
    public static String TRANSLATE_LANGUAGE_NO_SOURCE = "https://translation.googleapis.com/language/translate/v2?key=%1$s&target=%2$s&q=%3$s";

    public static String PHOTO_REFERENCE = "reference";
    public static String PHOTO_AVATAR = "AVATAR";
    public static String PHOTO_BRAND = "BRAND";

    private static int POST_METHOD = 0;
    private static int DELETE_METHOD = 1;
    private static int GET_METHOD = 2;

    private static int NUMBER_REQUEST_TOKEN = 3;

    public static String getMemberByIdUrl(String jid) {
        String identifer = jid;
        if (jid.contains(Constant.EMAIL_DOMAIN)) {
            identifer = jid.split("@")[0];
        }
        return String.format(GET_MEMBER_INFO_BY_JID, identifer.toLowerCase());
    }

    public static String getContentPromotionUrl(String countryCode) {
        return String.format(GET_PROMOTION_CONTENT, countryCode);
    }

    public static String getVerifyCodeByNewPhoneNumber(String user, String oldPhone, String newPhone) {
        return String.format(POST_VERIFY_CODE_CHANGE_PHONE, user, oldPhone, newPhone);
    }

    public static String getVerifyCodeUrl(String phoneNumber, String activeCode) {
        return String.format(POST_VERIFY_CODE, phoneNumber, activeCode);
    }

    public static String getVerifyCodeUrlResend(String user, String phone) {
        return String.format(POST_VERIFY_RESEND_CODE, user, phone);
    }

    public static String getGroupByXmppId(String xmppId) {
        if (xmppId.contains("@")) {
            xmppId = xmppId.split("@")[0];
        }
        return String.format(GET_GROUP_BY_XMPP_ID, xmppId);
    }

    public static String loginUrl(String user, String pass) {
        return String.format(POST_LOGIN, user, pass);
    }


    public static String getHashStringResetPassUrl(String username, String answer01, String answer02, String answer03) {
        return String.format(GET_HASH_RESET_PASS, username, answer01, answer02, answer03);
    }

    public static String getCheckForgetPasswordSecurityQuestionsUrl(String username) {
        return String.format(POST_FORGET_PASS_CHECK_QUESTIONS, username);
    }

    private static String refreshTokenUrl(Context context) {
        String refreshToken = Store.getStringData(context, WpkToken.STORE_REFRESH_TOKEN);
        return String.format(POST_REFRESH_TOKEN, refreshToken);
    }

    public static String getPhotoReference(String s) {
        try {
            JsonObject jsonObject = (new JsonParser()).parse(s).getAsJsonObject();
            return jsonObject.get(RestAPI.PHOTO_REFERENCE).getAsString();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String sendEmailAndUsernameToGetPassUrl(String username, String pass) {
        return String.format(GET_FORGET_PASS_SEND_EMAIL, username, pass);
    }

    public static void loadImageUrl(Context context, ImageView imageView, String reference) {
        Ion.with(context).load("https://webserv-ci.proteusiondev.com:8081/8EF640C4836D96CE990B71F60E0EA1DB/kernal/asset/61dcb8b8-e152-11e7-a59a-0050569a8872").withBitmap().intoImageView(imageView);
    }

    public static Future<Bitmap> getBitmapFromUrl(Context context, String reference) {
        return Ion.with(context).load(GET_PHOTO + reference).withBitmap().asBitmap();
    }

    public static String getAvatarUrl(String reference) {
        return GET_PHOTO + reference;
    }


    public static JsonElement getData(JsonObject jsonObject) {
        return jsonObject.get("data");
    }

    private static boolean checkAuthenticationCode(int code) {
        return code == 401;
    }

    private static boolean checkExpiredToken(String s) {
        JSONObject mainObject = null;
        try {
            mainObject = new JSONObject(s);
            String errorstatus = mainObject.getString("error");
            if (errorstatus.equals("invalid_token")) {
                return true;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }


    private static boolean checkHttpCode(int code) {
        return (code == 200) || (code == 201);
    }

    private static TrustManager[] trustAllCerts = new X509TrustManager[]{new X509TrustManager() {

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

    private static SSLContext getSSLContextInst() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext inst = SSLContext.getInstance("TLS");
        inst.init(null, trustAllCerts, null);
        return inst;
    }

    public static void initIon(Context context) {
        try {
            Ion.getDefault(context).configure().createSSLContext("TLS");
            Ion.getDefault(context).getHttpClient().getSSLSocketMiddleware().setSSLContext(getSSLContextInst());
//            Ion.getDefault(context).getHttpClient().getSSLSocketMiddleware().setTrustManagers(trustAllCerts);
//            Ion.getDefault(context).getHttpClient().getSSLSocketMiddleware().setHostnameVerifier(new HostnameVerifier() {
//                @Override
//                public boolean verify(String s, SSLSession sslSession) {
//                    return true;
//                }
//            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
 /*   private static boolean checkOauth(String url) {
        String header = Store.getStringData(context, WpkToken.STORE_TOKEN_TYPE) + " " + Store.getStringData(context, WpkToken.STORE_ACCESS_TOKEN);
        if (url.contains("oauth/token?grant_type=password") || url.startsWith(POST_REGISTER)) {
            header = "Basic d3JhcHB5X2FwcDp3cmFwcHlfYXBw";
        }
        return header;
    }
*/

    private static String getHeaderHttps(Context context, String url) {
        String header = Store.getStringData(context, WpkToken.STORE_TOKEN_TYPE) + " " + Store.getStringData(context, WpkToken.STORE_ACCESS_TOKEN);
        if (url.contains("oauth/token?grant_type=password") || url.startsWith(POST_REGISTER)) {
            header = "Basic d3JhcHB5X2FwcDp3cmFwcHlfYXBw";
        }
        AppFuncs.log("Header: " + header);
        AppFuncs.log("Url: " + url);
        return header;
    }

    private static Builders.Any.B getIon(Context context, String url, String method) {
        String header = getHeaderHttps(context, url);
        return Ion.with(context).load(method, url).setTimeout(TIME_OUT).addHeader("Authorization", header);
    }

    public static Future<Response<BaseObject<DetectLanguageResponse>>> apiGETDetectLanguage(Context context, String url) {
        return getIon(context, url, "GET").as(new TypeToken<BaseObject<DetectLanguageResponse>>() {
        }).withResponse();
    }

    public static Future<Response<BaseObject<TranslateLanguageResponse>>> apiGETTranslateLanguage(Context context, String url) {
        return getIon(context, url, "GET").as(new TypeToken<BaseObject<TranslateLanguageResponse>>() {
        }).withResponse();
    }

    private static Future<Response<String>> apiGET(Context context, String url) {
        return getIon(context, url, "GET").asString().withResponse();
    }

    private static Future<Response<String>> apiPOSTArray(Context context, String url, JsonArray jsonObject) {
        return getIon(context, url, "POST").setJsonArrayBody((jsonObject == null) ? new JsonArray() : jsonObject).asString().withResponse();
    }

    private static Future<Response<String>> apiPOST(Context context, String url, JsonObject jsonObject) {
        return getIon(context, url, "POST").setJsonObjectBody((jsonObject == null) ? new JsonObject() : jsonObject).asString().withResponse();
    }

    private static Future<Response<String>> apiPUT(Context context, String url, JsonObject jsonObject) {
        return getIon(context, url, "PUT").setJsonObjectBody((jsonObject == null) ? new JsonObject() : jsonObject).asString().withResponse();
    }

    private static Future<Response<String>> apiDELETE(Context context, String url, JsonObject jsonObject) {
        return getIon(context, url, "DELETE").setJsonObjectBody((jsonObject == null) ? new JsonObject() : jsonObject).asString().withResponse();
    }


    public static void PostDataWrappyArray(final Context context, final JsonArray jsonObject, final String url, final RestAPIListener listenner) {
        apiPOSTArray(context, url, jsonObject).setCallback(new FutureCallback<Response<String>>() {
            @Override
            public void onCompleted(Exception e, Response<String> result) {
                try {
                    if ((checkAuthenticationCode(result.getHeaders().code()))) {
                        if (checkExpiredToken(result.getResult())) {
                            refreshTokenHttps(context, jsonObject, null, url, listenner, POST_METHOD);
                        }

                    } else if (checkHttpCode(result.getHeaders().code())) {
                        AppFuncs.log(result.getResult());
                        listenner.OnComplete(result.getResult());
                    } else {
                        if (result.getHeaders().code() == ErrorCode.NO_NETWORK.getErrorCode())
                            listenner.onError(ErrorCode.NO_NETWORK.getErrorCode(), url);
                        else {
                            AppFuncs.log(result.getResult());
                            listenner.onError(getErrorCodeFromResponse(result.getResult()));
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    listenner.onError(ErrorCode.NO_NETWORK);
                }
            }
        });
    }

    public static void PostDataWrappy(final Context context, final JsonObject jsonObject, final String url, final RestAPIListener listenner) {
        apiPOST(context, url, jsonObject).setCallback(new FutureCallback<Response<String>>() {
            @Override
            public void onCompleted(Exception e, Response<String> result) {
                Debug.d(result != null ? result.getResult() : "");
                try {
                    if (e != null && !TextUtils.isEmpty(e.getLocalizedMessage())) {
                        AppFuncs.log(e.getLocalizedMessage());
                    }
                    if ((checkAuthenticationCode(result.getHeaders().code()))) {
                        if (checkExpiredToken(result.getResult())) {
                            refreshTokenHttps(context, null, jsonObject, url, listenner, POST_METHOD);
                        }
                    } else if (checkHttpCode(result.getHeaders().code())) {
                        AppFuncs.log(result.getResult());
                        listenner.OnComplete(result.getResult());
                    } else {
                        if (result.getHeaders().code() == ErrorCode.NO_NETWORK.getErrorCode())
                            listenner.onError(ErrorCode.NO_NETWORK.getErrorCode(), url);
                        else {
                            AppFuncs.log(result.getResult());
                            int errorCode = getErrorCodeFromResponse(result.getResult());
                            listenner.onError(errorCode);
                        }

                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    listenner.onError(ErrorCode.NO_NETWORK);
                }

            }
        });
    }

    public static void PutDataWrappy(final Context context, final JsonObject jsonObject, final String url, final RestAPIListener listenner) {
        apiPUT(context, url, jsonObject).setCallback(new FutureCallback<Response<String>>() {
            @Override
            public void onCompleted(Exception e, Response<String> result) {
                Debug.d(result != null ? result.getResult() : "");
                try {
                    if ((checkAuthenticationCode(result.getHeaders().code()))) {
                        if (checkExpiredToken(result.getResult())) {
                            refreshTokenHttps(context, null, jsonObject, url, listenner, POST_METHOD);
                        }
                    } else if (checkHttpCode(result.getHeaders().code())) {
                        AppFuncs.log(result.getResult());
                        listenner.OnComplete(result.getResult());
                    } else {
                        if (result.getHeaders().code() == ErrorCode.NO_NETWORK.getErrorCode())
                            listenner.onError(ErrorCode.NO_NETWORK.getErrorCode(), url);
                        else {
                            AppFuncs.log(result.getResult());
                            listenner.onError(getErrorCodeFromResponse(result.getResult()));
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    listenner.onError(ErrorCode.NO_NETWORK);
                }

            }
        });
    }

    public static void DeleteDataWrappy(final Context context, final JsonObject jsonObject, final String url, final RestAPIListener listenner) {
        apiDELETE(context, url, jsonObject).setCallback(new FutureCallback<Response<String>>() {
            @Override
            public void onCompleted(Exception e, Response<String> result) {
                try {
                    if ((checkAuthenticationCode(result.getHeaders().code()))) {
                        if (checkExpiredToken(result.getResult())) {
                            refreshTokenHttps(context, null, jsonObject, url, listenner, DELETE_METHOD);
                        }
                    } else if (checkHttpCode(result.getHeaders().code())) {
                        AppFuncs.log(result.getResult());
                        listenner.OnComplete(result.getResult());
                    } else {
                        if (result.getHeaders().code() == ErrorCode.NO_NETWORK.getErrorCode())
                            listenner.onError(ErrorCode.NO_NETWORK.getErrorCode(), url);
                        else {
                            AppFuncs.log(result.getResult());
                            listenner.onError(getErrorCodeFromResponse(result.getResult()));
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    listenner.onError(ErrorCode.NO_NETWORK);
                }

            }
        });
    }

    public static void GetDataWrappy(final Context context, final String url, final RestAPIListener listenner) {
        apiGET(context, url).setCallback(new FutureCallback<Response<String>>() {
            @Override
            public void onCompleted(Exception e, Response<String> result) {
                try {
                    if (e != null && !TextUtils.isEmpty(e.getLocalizedMessage())) {
                        AppFuncs.log(e.getLocalizedMessage());
                    }
                    if (result != null && (checkAuthenticationCode(result.getHeaders().code()))) {
                        if (checkExpiredToken(result.getResult())) {
                            refreshTokenHttps(context, null, null, url, listenner, GET_METHOD);
                        }
                    } else if (checkHttpCode(result.getHeaders().code())) {
                        AppFuncs.log(result.getResult());
                        listenner.OnComplete(result.getResult());
                    } else {
                        if (result.getHeaders().code() == ErrorCode.NO_NETWORK.getErrorCode())
                            listenner.onError(ErrorCode.NO_NETWORK);
                        else {
                            AppFuncs.log(result.getResult());
                            listenner.onError(getErrorCodeFromResponse(result.getResult()), url);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    listenner.onError(ErrorCode.NO_NETWORK);
                }
            }
        });
    }

//    public static void UploadFile(Context context, String url, String type, File file, final RestAPIListener listenner) {
//        String header = getHeaderHttps(context,url);
//        Ion.with(context)
//                .load(url)
//                .addHeader("Authorization",header)
//                .setMultipartParameter("type", type)
//                .setMultipartFile("file","multipart/form-data",file)
//                .asString().withResponse()
//                .setCallback(new FutureCallback<Response<String>>() {
//                    @Override
//                    public void onCompleted(Exception e, Response<String> result) {
//                        listenner.OnComplete((result != null && result.getHeaders() != null) ? result.getHeaders().code() : 0, (e != null) ? e.getLocalizedMessage() : null, (result != null) ? result.getResult() : null);
//                    }
//                });
//    }

    public static Future<Response<String>> uploadFile(Context context, File file, String type) {
        String header = getHeaderHttps(context, RestAPI.POST_PHOTO);
        return Ion.with(context)
                .load(RestAPI.POST_PHOTO)
                .addHeader("Authorization", header)
                .setMultipartParameter("type", type)
                .setMultipartFile("file", "multipart/form-data", file)
                .asString().withResponse();
    }

    private static int numberRefreshToken = 0;

    private static void refreshTokenHttps(final Context context, final JsonArray jsonarray, final JsonObject json, final String url, final RestAPIListener listenner, final int method) {
        Ion.with(context).load("POST", refreshTokenUrl(context)).addHeader("Authorization", "Basic d3JhcHB5X2FwcDp3cmFwcHlfYXBw").asString().withResponse().setCallback(new FutureCallback<Response<String>>() {
            @Override
            public void onCompleted(Exception e, Response<String> result) {
                try {
                    int httpCode = (result != null) ? result.getHeaders().code() : 0;
                    String error = (e != null) ? e.getLocalizedMessage() : "";
                    if ((checkAuthenticationCode(httpCode))) {
                        listenner.OnComplete(null);
                        return;
                    }
                    if (httpCode == 400 /*invalid expire token*/) {
                        listenner.OnComplete(null);
                        logout(context);
                        return;
                    }
                    if (!RestAPI.checkHttpCode(httpCode) && httpCode != 0 /*no network*/) {
//                        AppFuncs.alert(context, error, true);
                        if (numberRefreshToken >= NUMBER_REQUEST_TOKEN) {
                            numberRefreshToken = 0;
                            listenner.OnComplete(null);
                            logout(context);
                        } else {
                            numberRefreshToken++;
                            refreshTokenHttps(context, jsonarray, json, url, listenner, method);
                        }
                        return;
                    }
                    JsonObject jsonObject = (new JsonParser()).parse(result.getResult()).getAsJsonObject();
                    Gson gson = new Gson();
                    WpkToken wpkToken = gson.fromJson(jsonObject, WpkToken.class);
                    wpkToken.saveToken(context);
                    if (method == POST_METHOD) {
                        if (json != null) {
                            PostDataWrappy(context, json, url, listenner);
                        } else {
                            PostDataWrappyArray(context, jsonarray, url, listenner);
                        }
                    } else if (method == DELETE_METHOD) {
                        DeleteDataWrappy(context, json, url, listenner);
                    } else if (method == GET_METHOD) {
                        GetDataWrappy(context, url, listenner);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    listenner.onError(ErrorCode.NO_NETWORK);
                }
            }
        });
    }

    private static void logout(Context context) {
        ImApp.sImApp.logout();
        if (context instanceof Activity)
            ((Activity) context).finishAffinity();
    }

    public static HashMap<String, String> jsonToMap(JsonObject object) {
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, String>>() {
        }.getType();
        return gson.fromJson(object, type);
    }

    public static int getErrorCodeFromResponse(String response) {
        try {
            JsonObject jsonObject = (new JsonParser()).parse(response).getAsJsonObject();
            if (jsonObject.has("code"))
                return jsonObject.get("code").getAsInt();
            else {
                String error_description = jsonObject.get("error_description").getAsString();
                switch (error_description) {
                    case "ERROR_CODE_LOGIN_FIRST_TIME_FAIL":
                        return ErrorCode.ERROR_CODE_LOGIN_FIRST_TIME_FAIL.getErrorCode();
                    case "ERROR_CODE_LOGIN_SECOND_TIME_FAIL":
                        return ErrorCode.ERROR_CODE_LOGIN_SECOND_TIME_FAIL.getErrorCode();
                    case "ERROR_CODE_LOGIN_THIRD_TIME_FAIL":
                        return ErrorCode.ERROR_CODE_LOGIN_THIRD_TIME_FAIL.getErrorCode();
                    case "ERROR_CODE_LOGIN_FOURTH_TIME_FAIL":
                        return ErrorCode.ERROR_CODE_LOGIN_THIRD_TIME_FAIL.getErrorCode();
                    case "ERROR_CODE_LOGIN_FIFTH_TIME_FAIL":
                        return ErrorCode.ERROR_CODE_LOGIN_THIRD_TIME_FAIL.getErrorCode();
                    case "ERROR_CODE_LOGIN_LOCKED":
                        return ErrorCode.ERROR_CODE_LOGIN_LOCKED.getErrorCode();
                    case "ERROR_CODE_LOGIN_BLOCKED":
                        return ErrorCode.ERROR_CODE_LOGIN_LOCKED.getErrorCode();
                    case "WP_K_MEMBER_NOT_FOUND":
                        return ErrorCode.WP_K_MEMBER_NOT_FOUND.getErrorCode();
                }
            }
        } catch (Exception ex) {
        }
        return 0;
    }
}
