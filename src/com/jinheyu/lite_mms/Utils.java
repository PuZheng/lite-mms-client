package com.jinheyu.lite_mms;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;

import com.jinheyu.lite_mms.data_structures.User;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by xc on 13-8-13.
 */
public class Utils {

    private static final String TAG = "Utils";
    private static final String UNLOAD_TASK_PIC_FILE_NAME = "unload-task-pic.jpeg";
    private static Uri unloadTaskPicUri;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    static public boolean isEmptyString(String s) {
        return s == null || s.isEmpty();
    }

    public static User readUserPrefs(Context c) {
        SharedPreferences preferences = c.getSharedPreferences("user", Context.MODE_PRIVATE);
        int id = preferences.getInt("id", -1);
        String username = preferences.getString("username", null);
        String token = preferences.getString("token", null);
        int groupId = preferences.getInt("groupId", -1);

        if (id == -1 || username == null || token == null || groupId == -1) {
            return null;
        }
        return new User(id, username, token, groupId);
    }

    public static void storeUserPrefs(User user, Context context) {
        SharedPreferences preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("id", user.getId());
        editor.putString("username", user.getUserName());
        editor.putString("token", user.getToken());
        editor.putInt("groupId", user.getGroupId());
        editor.commit();
    }

    public static void clearUserPrefs(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.commit();
    }


    public static void displayError(Context c, Exception ex) {
        AlertDialog.Builder builder = new AlertDialog.Builder(c);
        builder.setTitle(c.getString(R.string.error));
        builder.setMessage(ex.getMessage());
        builder.setNegativeButton(c.getString(R.string.close), null);
        builder.show();
    }

    public static Uri getUnloadTaskPicUri() {
        Uri fileUri = Uri.fromFile(new File(getStorageDir() + UNLOAD_TASK_PIC_FILE_NAME));
        return fileUri;
    }

    public static String getStorageDir() {
        return Environment.getExternalStorageDirectory() + "/lite-mms/";
    }

    public static void assertDirExists(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e(TAG, "can't create directory: " + dir);
            }
        }
    }

    public static Pair<String, Integer> getServerAddress(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String ip = sharedPreferences.getString("server_ip", MyApp.DEFAULT_SERVER_IP);
        int port = Integer.valueOf(sharedPreferences.getString("server_port",
                String.valueOf(MyApp.DEFAULT_SERVER_PORT)));
        return new Pair<String, Integer>(ip, port);
    }

    public static String join(List<String> stringList, String delimiter) {
        boolean first = true;
        StringBuilder stringBuilder = new StringBuilder();
        for (String s: stringList) {
            stringBuilder.append((first? "": delimiter) + s);
            first = false;
        }
        return stringBuilder.toString();
    }

    public static String join(String[] strings, String delimiter) {
        boolean first = true;
        StringBuilder stringBuilder = new StringBuilder();
        for (String s: strings) {
            stringBuilder.append((first? "": delimiter) + s);
            first = false;
        }
        return stringBuilder.toString();
    }

    public static CharSequence getVersion(Context context) throws IOException {
        InputStream inputStream = context.getAssets().open("version.txt");
        byte buf[] = new byte[inputStream.available()];
        inputStream.read(buf);
        return new String(buf);
    }
}
