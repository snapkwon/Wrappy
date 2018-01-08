package net.wrappy.im.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Created by ben on 08/12/2017.
 */

public class   WpErrors {

    public static final int OP_VERSION_MISMATCH = 8000;
    public static final int OP_ACCESS_IS_DENIED = 8001;
    public static final int OP_INTERNAL_SERVER_ERROR = 8002;
    public static final int OP_K_MEMBER_ALREADY_EXISTS = 8003;
    public static final int OP_K_MEMBER_NOT_EXISTS = 8004;
    public static final int OP_K_MEMBER_ALREADY_ACTIVATED = 8005;
    public static final int OP_K_MEMBER_NOT_ACTIVATED = 8006;
    public static final int OP_K_MEMBER_INVALID_EMAIL_ADDRESS = 8007;
    public static final int OP_T_VERIFICATION_CODE_EXPIRY_OR_INVALID = 8009;
    public static final int OP_K_ASSET_NOT_EXISTS = 8010;
    public static final int OP_K_ASSET_RETAIN_FAILURE_FILE_EMPTY = 8011;
    public static final int OP_K_ASSET_RETAIN_FAILURE_TTL_EXPIRED = 8012;
    public static final int OP_K_ASSET_FETCH_FAILURE_FILE_UNAVAILABLE = 8013;
    public static final int OP_K_ASSET_FETCH_FAILURE_TTL_EXPIRED = 8014;
    public static final int OP_K_ASSET_FETCH_FAILURE_FILE_UNOBTAINABLE = 8015;
    public static final int OP_K_ASSET_TYPE_NOT_DETERMINES = 8016;
    public static final int OP_K_REGISTRANT_NOT_EXISTS = 8017;
    public static final int OP_K_BILLING_NOT_EXISTS = 8018;
    public static final int OP_K_BILLING_NOT_PAYABLE = 8019;
    public static final int OP_T_MAIL_EXCEPTION = 8020;
    public static final int OP_T_SMS_EXCEPTION_PAYLOAD_LENGTH_EXCEED_LIMIT = 8021;
    public static final int OP_K_MEMBER_USERNAME_ALREADY_EXISTS = 8022;
    public static final int OP_K_MEMBER_MOBILE_ALREADY_EXISTS = 8023;
    public static final int OP_K_MEMBER_EMAIL_ALREADY_EXISTS = 8024;
    public static final int OP_K_RESOURCE_NOT_EXISTS = 8025;
    public static final int OP_K_MEMBER_SECURITY_QUESTIONS_NOT_FOUND = 8026;

    public static String getErrorMessage(String e) {
        String error = "";
        try {
            JsonObject jsonObject = (new JsonParser()).parse(e).getAsJsonObject();
            int httpErrors = jsonObject.get("code").getAsInt();

            switch (httpErrors) {
                case OP_VERSION_MISMATCH:
                    error = "Version mismatch";
                    break;
                case OP_ACCESS_IS_DENIED:
                    error = "Access is denied";
                    break;
                case OP_INTERNAL_SERVER_ERROR:
                    error = "Server is error";
                    break;
                case OP_K_MEMBER_ALREADY_EXISTS:
                    error = "Memeber already exists";
                    break;
                case OP_K_MEMBER_NOT_EXISTS:
                    error = "Member isn't exists";
                    break;
                case OP_K_MEMBER_ALREADY_ACTIVATED:
                    error = "Member already activated";
                    break;
                case OP_K_MEMBER_NOT_ACTIVATED:
                    error = "Member isn't activated";
                    break;
                case OP_K_MEMBER_INVALID_EMAIL_ADDRESS:
                    error = "Email address is invalid";
                    break;
                case OP_T_VERIFICATION_CODE_EXPIRY_OR_INVALID:
                    error = "Verification code expiry or invalid";
                    break;
                case OP_K_ASSET_NOT_EXISTS:
                    error = "OP_K_ASSET_NOT_EXISTS";
                    break;
                case OP_K_ASSET_RETAIN_FAILURE_FILE_EMPTY:
                    error = "OP_K_ASSET_RETAIN_FAILURE_FILE_EMPTY";
                    break;
                case OP_K_ASSET_RETAIN_FAILURE_TTL_EXPIRED:
                    error = "OP_K_ASSET_RETAIN_FAILURE_TTL_EXPIRED";
                    break;
                case OP_K_ASSET_FETCH_FAILURE_FILE_UNAVAILABLE:
                    error = "OP_K_ASSET_FETCH_FAILURE_FILE_UNAVAILABLE";
                    break;
                case OP_K_ASSET_FETCH_FAILURE_TTL_EXPIRED:
                    error = "OP_K_ASSET_FETCH_FAILURE_TTL_EXPIRED";
                    break;
                case OP_K_ASSET_FETCH_FAILURE_FILE_UNOBTAINABLE:
                    error = "OP_K_ASSET_FETCH_FAILURE_FILE_UNOBTAINABLE";
                    break;
                case OP_K_ASSET_TYPE_NOT_DETERMINES:
                    error = "OP_K_ASSET_TYPE_NOT_DETERMINES";
                    break;
                case OP_K_REGISTRANT_NOT_EXISTS:
                    error = "OP_K_REGISTRANT_NOT_EXISTS";
                    break;
                case OP_K_BILLING_NOT_EXISTS:
                    error = "OP_K_BILLING_NOT_EXISTS";
                    break;
                case OP_K_BILLING_NOT_PAYABLE:
                    error = "OP_K_BILLING_NOT_PAYABLE";
                    break;
                case OP_T_MAIL_EXCEPTION:
                    error = "OP_T_MAIL_EXCEPTION";
                    break;
                case OP_T_SMS_EXCEPTION_PAYLOAD_LENGTH_EXCEED_LIMIT:
                    error = "OP_T_SMS_EXCEPTION_PAYLOAD_LENGTH_EXCEED_LIMIT";
                    break;
                case OP_K_MEMBER_USERNAME_ALREADY_EXISTS:
                    error = "Username already exists";
                    break;
                case OP_K_MEMBER_MOBILE_ALREADY_EXISTS:
                    error = "Phone already exists";
                    break;
                case OP_K_MEMBER_EMAIL_ALREADY_EXISTS:
                    error = "Email already exists";
                    break;
                case OP_K_RESOURCE_NOT_EXISTS:
                    error = "OP_K_RESOURCE_NOT_EXISTS";
                    break;
                case OP_K_MEMBER_SECURITY_QUESTIONS_NOT_FOUND:
                    error = "OP_K_MEMBER_SECURITY_QUESTIONS_NOT_FOUND";
                    break;
            }
        }catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return error;
        }

    }

}
