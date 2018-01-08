package net.wrappy.im.model.translate;

import net.wrappy.im.model.T;

import java.util.ArrayList;

/**
 * Created by hp on 1/4/2018.
 */

public class TranslateLanguageResponse extends T{
    private ArrayList<Translation> translations;

    public ArrayList<Translation> getTranslations() {
        return translations == null ? new ArrayList<Translation>() : translations;
    }

    public class Translation {
        private String translatedText;
        private String detectedSourceLanguage;

        public String getTranslatedText() {
            return translatedText;
        }

        public String getDetectedSourceLanguage() {
            return detectedSourceLanguage;
        }
    }
}
