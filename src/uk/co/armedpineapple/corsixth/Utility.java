package uk.co.armedpineapple.corsixth;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import com.dreamdance.th.R;

import java.io.File;

/**
 * Author: dk
 * Date: 12-5-8
 */
public class Utility {
    static final int MIN_REL_WIDTH = 640;
    static final int MIN_REL_HEIGHT = 480;
    static Pair<Integer, Integer> sResolution;

    public static boolean checkPathExists(String path) {
        File f = new File(path);
        return (f.exists());
    }

    public static boolean isSuitableResolution(Activity activity) {
        Pair<Integer, Integer> rel = getResolution(activity);
        return (rel.first >= MIN_REL_WIDTH && rel.second >= MIN_REL_HEIGHT);
    }

    public static Pair<Integer, Integer> getResolution(Activity activity) {
        if (null == sResolution) {
            int width = activity.getWindowManager().getDefaultDisplay().getWidth();
            int height = activity.getWindowManager().getDefaultDisplay().getHeight();
            sResolution = new Pair<Integer, Integer>(width, height);
        }
        return sResolution;
    }

    public static String getDownloadDir() {
        String status = Environment.getExternalStorageState();
        if (status == null || !status.equals(Environment.MEDIA_MOUNTED)){
            return null;
        }

        String path = null;

        // get the sdcard directory
        File sdFile = Environment.getExternalStorageDirectory();
        if (null != sdFile) {
            path = sdFile.toString();
        } else {
            path = "/sdcard/";
        }

        path += "/download";

        File destDir = new File(path);
        if (!destDir.exists()) {
            try {
                if (!destDir.mkdirs()) {
                    Log.e("getDownloadDir", "create folder " + path + " failed");
                    return null;
                }
            } catch (SecurityException e) {
                Log.e("getDownloadDir", "create folder " + path + " failed: " + e.toString());
                return null;
            }
        }

        return path;
    }
}
