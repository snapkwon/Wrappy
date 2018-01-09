package net.wrappy.im.model;

/**
 * Created by ben on 09/01/2018.
 */

public class PromotionSetting {
    private String content;
    private boolean enablePromotion;
    private String subTitle;
    private String title;

    public PromotionSetting() {}

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isEnablePromotion() {
        return enablePromotion;
    }

    public void setEnablePromotion(boolean enablePromotion) {
        this.enablePromotion = enablePromotion;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
