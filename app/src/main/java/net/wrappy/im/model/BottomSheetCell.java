package net.wrappy.im.model;

/**
 * Created by ben on 20/12/2017.
 */

public class BottomSheetCell {
    int index;
    int resId;
    String title;

    public BottomSheetCell(int index, int resId, String title) {
        this.index = index;
        this.resId = resId;
        this.title = title;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getResId() {
        return resId;
    }

    public void setResId(int resId) {
        this.resId = resId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
