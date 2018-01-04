package net.wrappy.im.model.translate;

import java.util.ArrayList;

/**
 * Created by hp on 1/4/2018.
 */

public class DetectLanguageResponse {
    private ArrayList<ArrayList<Detection>> detections;

    public ArrayList<ArrayList<Detection>> getDetections() {
        return detections == null ? new ArrayList<ArrayList<Detection>>() : detections;
    }

    public class Detection {
        private boolean isReliable;
        private String language;

        public boolean isReliable() {
            return isReliable;
        }

        public String getLanguage() {
            return language;
        }
    }
}
