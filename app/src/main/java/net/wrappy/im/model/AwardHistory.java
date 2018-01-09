package net.wrappy.im.model;

import java.util.ArrayList;

/**
 * Created by ben on 09/01/2018.
 */

public class AwardHistory {

    PromotionLevel level0;
    ArrayList<PromotionLevel> levels;

    public AwardHistory(PromotionLevel level0, ArrayList<PromotionLevel> levels) {
        this.level0 = level0;
        this.levels = levels;
    }

    public AwardHistory() {}

    public PromotionLevel getLevel0() {
        return level0;
    }

    public void setLevel0(PromotionLevel level0) {
        this.level0 = level0;
    }

    public ArrayList<PromotionLevel> getLevels() {
        return levels;
    }

    public void setLevels(ArrayList<PromotionLevel> levels) {
        this.levels = levels;
    }
}
