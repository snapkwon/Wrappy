package net.wrappy.im.GethService.utils;

/**
 * Created by sonntht on 21/10/2017.
 */
import java.io.File;

public class Utils {
    public static void deleteDirIfExists(File file) {
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                deleteDirIfExists(f);
            }
        }
        file.delete();
    }
}
