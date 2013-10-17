package com.jinheyu.lite_mms;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;
import android.view.View;

import com.jinheyu.lite_mms.data_structures.QualityInspectionReport;
import com.jinheyu.lite_mms.data_structures.User;
import com.jinheyu.lite_mms.data_structures.WorkCommand;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;

/**
 * Created by xc on 13-8-13.
 */
public class Utils {

    private static final String TAG = "Utils";
    private static final String UNLOAD_TASK_PIC_FILE_NAME = "unload-task-pic.jpeg";
    private static final String TEMP_QI_REPORT_FILE_NAME = "temp-qi-report-pic.jpeg";

    public static void assertDirExists(String dir) {
        File file = new File(dir);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e(TAG, "can't create directory: " + dir);
            }
        }
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

    public static File getExternalCacheDir(Context context) {
        if (hasExternalCacheDir()) {
            return context.getExternalCacheDir();
        }
        return new File(getStorageDir() + " cache/");
    }

    public static int getMaxTimes(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        return Integer.parseInt(sharedPreferences.getString("max_times", String.valueOf(MyApp.MAX_TIMES_PROCESSED_TO_ORG)));
    }

    public static Pair<String, Integer> getServerAddress(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String ip = sharedPreferences.getString("server_ip", MyApp.DEFAULT_SERVER_IP);
        int port = Integer.valueOf(sharedPreferences.getString("server_port",
                String.valueOf(MyApp.DEFAULT_SERVER_PORT)));
        return new Pair<String, Integer>(ip, port);
    }

    public static String getStorageDir() {
        return Environment.getExternalStorageDirectory() + "/lite-mms/";
    }

    public static Uri getUnloadTaskPicUri() {
        return Uri.fromFile(new File(getStorageDir() + UNLOAD_TASK_PIC_FILE_NAME));
    }

    public static CharSequence getVersion(Context context) throws IOException {
        InputStream inputStream = null;
        try {
            inputStream = context.getAssets().open("version.txt");
            byte[] buf = new byte[inputStream.available()];
            inputStream.read(buf);
            return new String(buf);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }

    public static boolean hasExternalCacheDir() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static boolean isEmptyString(String s) {
        return s == null || s.isEmpty();
    }

    public static boolean isExternalStorageRemovable() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.GINGERBREAD || Environment.isExternalStorageRemovable();
    }

    public static String join(List<String> stringList, String delimiter) {
        boolean first = true;
        StringBuilder stringBuilder = new StringBuilder();
        for (String s : stringList) {
            stringBuilder.append(first ? "" : delimiter).append(s);
            first = false;
        }
        return stringBuilder.toString();
    }

    public static String join(String[] strings, String delimiter) {
        return join(Arrays.asList(strings), delimiter);
    }

    public static int[] parse2IntegerArray(String s) throws NumberFormatException {
        return parse2IntegerArray(s, ",");
    }

    public static int[] parse2IntegerArray(String s, String regularExpression) throws NumberFormatException {
        String[] strings = s.split(regularExpression);
        int[] result = new int[isEmptyString(s) ? 0 : strings.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = Integer.parseInt(strings[i].trim());
        }
        return result;
    }

    public static int parseInt(String string, int defaultValue) {
        try {
            return Integer.parseInt(string);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static User readUserPrefs(Context c) {
        SharedPreferences preferences = c.getSharedPreferences("user", Context.MODE_PRIVATE);
        int id = preferences.getInt("id", -1);
        String username = preferences.getString("username", null);
        String token = preferences.getString("token", null);
        int groupId = preferences.getInt("groupId", -1);
        int[] teamIdList;
        int[] departmentIdList;
        try {
            teamIdList = parse2IntegerArray(preferences.getString("teamIds", ""), ", ");
            departmentIdList = parse2IntegerArray(preferences.getString("departmentIds", ""), ", ");
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return null;
        }

        if (id == -1 || username == null || token == null || groupId == -1) {
            return null;
        }
        return new User(id, username, token, groupId, teamIdList, departmentIdList);
    }

    public static void storeUserPrefs(User user, Context context) {
        SharedPreferences preferences = context.getSharedPreferences("user", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("id", user.getId());
        editor.putString("username", user.getUserName());
        editor.putString("token", user.getToken());
        editor.putInt("groupId", user.getGroupId());
        String teamIdString = Arrays.toString(user.getTeamIds());
        editor.putString("teamIds", teamIdString.substring(1, teamIdString.length() - 1));
        String departmentIds = Arrays.toString(user.getDepartmentIds());
        editor.putString("departmentIds", departmentIds.substring(1, departmentIds.length() - 1));
        editor.commit();
    }

    public static String getMd5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            BigInteger number = new BigInteger(1, messageDigest);
            String md5 = number.toString(16);

            while (md5.length() < 32)
                md5 = "0" + md5;

            return md5;
        } catch (NoSuchAlgorithmException e) {
            Log.e("MD5", e.getLocalizedMessage());
            return null;
        }
    }

    public static Uri getTempQIReportPicUri() {
        return Uri.fromFile(new File(getStorageDir() + TEMP_QI_REPORT_FILE_NAME));
    }

    public static PullToRefreshAttacher initPullToRereshAttacher(Activity activity, PullToRefreshAttacher.OnRefreshListener listener) {
        PullToRefreshAttacher.Options options = new PullToRefreshAttacher.Options();
        options.refreshScrollDistance = 0.4f;
        PullToRefreshAttacher pullToRefreshAttacher = PullToRefreshAttacher.get(activity, options);
        View view = activity.findViewById(R.id.ptr_layout);
        if (view != null) {
            PullToRefreshLayout pullToRefreshLayout = (PullToRefreshLayout) view;
            pullToRefreshLayout.setPullToRefreshAttacher(pullToRefreshAttacher, listener);
            return pullToRefreshAttacher;
        }
        return null;
    }

    public static PullToRefreshAttacher initPullToRereshAttacher(Activity activity) {
        if (activity instanceof PullToRefreshAttacher.OnRefreshListener) {
            return initPullToRereshAttacher(activity, (PullToRefreshAttacher.OnRefreshListener) activity);
        }else {
            throw new IllegalArgumentException();
        }
    }

    public static String getQIRWeightAndQuantity(QualityInspectionReport report, WorkCommand workCommand) {
        return getWeightAndQuantity(report.getWeight(), report.getQuantity(), workCommand);
    }

    public static String getWeightAndQuantity(int weight, int quantity, WorkCommand workCommand) {
        return workCommand.measuredByWeight() ? String.format("%d 公斤", weight) :
                String.format("%d 公斤/%d %s", weight, quantity, workCommand.getUnit());
    }

}
