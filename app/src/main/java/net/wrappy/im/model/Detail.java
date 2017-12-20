package net.wrappy.im.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by CuongDuong on 12/20/2017.
 */

public class Detail {
    @SerializedName("en_US")
    private String enUS;
    @SerializedName("zh_HK")
    private String zhHK;
    @SerializedName("zh_TW")
    private String zhTW;
    @SerializedName("zh_Hans")
    private String zhHans;

    public String getEnUS() {
        return enUS;
    }

    public void setEnUS(String enUS) {
        this.enUS = enUS;
    }

    public String getZhHK() {
        return zhHK;
    }

    public void setZhHK(String zhHK) {
        this.zhHK = zhHK;
    }

    public String getZhTW() {
        return zhTW;
    }

    public void setZhTW(String zhTW) {
        this.zhTW = zhTW;
    }

    public String getZhHans() {
        return zhHans;
    }

    public void setZhHans(String zhHans) {
        this.zhHans = zhHans;
    }
}
