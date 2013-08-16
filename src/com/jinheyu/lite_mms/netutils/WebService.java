package com.jinheyu.lite_mms.netutils;

import android.content.Context;
import android.util.Pair;

import com.jinheyu.lite_mms.MyApp;
import com.jinheyu.lite_mms.Utils;
import com.jinheyu.lite_mms.data_structures.Customer;
import com.jinheyu.lite_mms.data_structures.Harbor;
import com.jinheyu.lite_mms.data_structures.UnloadSession;
import com.jinheyu.lite_mms.data_structures.User;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by xc on 13-8-12.
 */
public class WebService {

    private static WebService instance;
    private List<Customer> customerList;

    public static WebService getInstance(Context c) {
        if (instance == null) {
            instance = new WebService(c);
        }
        return instance;
    }

    private Context context;

    private WebService(Context c) {
        this.context = c;
    }

    public User login(String username, String password) throws IOException, JSONException, BadRequest, ValidationError {
        Map<String, String> params = new HashMap<String, String>();
        params.put("username", username);
        params.put("password", password);
        String url = composeUrl("auth_ws", "login", params);
        HttpResponse response = sendRequest(url, "POST", "");
        int stateCode = response.getStatusLine().getStatusCode();
        String result = EntityUtils.toString(response.getEntity());
        JSONObject root = new JSONObject(result);
        if (stateCode == 200) {
            return new User(root.getInt("userID"), root.getString("username"), root.getString("token"), root.getInt("userGroup"));
        } else if (stateCode == 403) {
            throw new ValidationError(root.getString("reason"));
        } else {
            throw new BadRequest(result);
        }
    }

    public List<UnloadSession> getUnloadSessionList() throws JSONException, IOException, BadRequest {
        List<UnloadSession> ret;

        String url = composeUrl("cargo_ws", "unload-session-list");
        HttpResponse response = sendRequest(url);
        int stateCode = response.getStatusLine().getStatusCode();
        String result = EntityUtils.toString(response.getEntity());
        JSONObject root = new JSONObject(result);
        if (stateCode == 200) {
            int cnt = root.getInt("total_cnt");
            ret = new ArrayList<UnloadSession>(cnt);
            JSONArray data = root.getJSONArray("data");
            for (int i=0; i < data.length(); ++i) {
                JSONObject jo = data.getJSONObject(i);
                int id = jo.getInt("sessionID");
                String plate = jo.getString("plateNumber");
                boolean locked = jo.getInt("isLocked") == 1;
                ret.add(new UnloadSession(id, plate, locked));
            }
        } else {
            throw new BadRequest(result);
        }

        return ret;
    }


    public List<Harbor> getHarborList() throws IOException, JSONException, BadRequest {
        List<Harbor> ret;

        String url = composeUrl("cargo_ws", "harbor-list");
        HttpResponse response = sendRequest(url);

        int stateCode = response.getStatusLine().getStatusCode();
        String result = EntityUtils.toString(response.getEntity());

        if (stateCode == 200) {
            ret = new ArrayList<Harbor>();
            JSONArray data = new JSONArray(result);
            for (int i=0; i < data.length(); ++i) {
                String harborName = data.getString(i);
                ret.add(new Harbor(harborName));
            }
        } else {
            throw new BadRequest(result);
        }
        return ret;
    }

    public List<Customer> getCustomerList() throws IOException, JSONException, BadRequest {
        List<Customer> ret;

        String url = composeUrl("order_ws", "customer-list");
        HttpResponse response = sendRequest(url);

        int stateCode = response.getStatusLine().getStatusCode();
        String result = EntityUtils.toString(response.getEntity());

        if (stateCode == 200) {
            ret = new ArrayList<Customer>();
            JSONArray data = new JSONArray(result);
            for (int i=0; i < data.length(); ++i) {
                JSONObject jo = data.getJSONObject(i);
                int id = jo.getInt("id");
                String name = jo.getString("name");
                String abbr = jo.getString("abbr");
                ret.add(new Customer(id, name, abbr));
            }
        } else {
            throw new BadRequest(result);
        }
        return ret;
    }

    public void addUnloadTask(UnloadSession unloadSession, Harbor harbor, Customer customer, boolean done, String picPath) throws BadRequest, IOException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("actor_id", String.valueOf(MyApp.getCurrentUser().getId()));
        params.put("customer_id", String.valueOf(customer.getId()));
        params.put("is_finished", done? "1": "0");
        params.put("harbour", URLEncoder.encode(harbor.getName(), "UTF-8"));
        params.put("session_id", String.valueOf(unloadSession.getId()));

        URL url = new URL(composeUrl("cargo_ws", "unload-task", params));
        HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
        httpURLConnection.setRequestMethod("POST");

        String boundary = "*****";
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(true);
        httpURLConnection.setUseCaches(false);
        httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
        httpURLConnection.setRequestProperty("Charset", "UTF-8");
        httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
        DataOutputStream ds = new DataOutputStream(httpURLConnection.getOutputStream());
        ds.writeBytes("--" + boundary + "\r\n");
        ds.writeBytes("Content-Disposition: form-data;name=\"foo\";filename=\"foo.jpeg\"\r\n");
        ds.writeBytes("\r\n");
        FileInputStream fStream = new FileInputStream(picPath);
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];
        int length;
        while ((length = fStream.read(buffer)) != -1) {
            ds.write(buffer, 0, length);
        }
        ds.writeBytes("\r\n");
        ds.writeBytes("--" + boundary + "--\r\n");

        if (httpURLConnection.getResponseCode() != HttpStatus.SC_OK) {
            throw new BadRequest(httpURLConnection.getResponseMessage());
        }

    }

    private String composeUrl(String blueprint, String path) {
        return composeUrl(blueprint, path, null);
    }

    private String composeUrl(String blueprint, String path, Map<String, String> params) {
        Pair<String, Integer> pair = Utils.getServerAddress(context);
        String ret = String.format("http://%s:%d/%s/%s", pair.first, pair.second, blueprint, path);
        if (params != null) {
            ret += "?";
            boolean first = true;
            for (Map.Entry<String, String> entry: params.entrySet()) {
                ret += (first? "": "&") + entry.getKey() + "=" + entry.getValue();
                first = false;
            }
        }
        return ret;
    }

    private HttpResponse sendRequest(String url)
            throws IOException, JSONException {
        return sendRequest(url, "GET", (String) null);
    }

    private HttpResponse sendRequest(String url, String method,
                                     Map<String, String> data) throws
            IOException, JSONException {
        HttpResponse response = null;

        if (method.equals("GET")) {
            HttpGet hg = new HttpGet(url);
            response = new DefaultHttpClient().execute(hg);
        } else if (method.equals("POST")) {
            HttpPost hp = new HttpPost(url);
            if (data != null) {
                JSONObject jo = new JSONObject();
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    jo.put(entry.getKey(), entry.getValue());
                }
                hp.setHeader("Content-type", "application/json");
                hp.setEntity(new StringEntity(jo.toString(), "utf-8"));
            }
            response = new DefaultHttpClient().execute(hp);
        } else if (method.equals("PUT")) {
            HttpPut hp = new HttpPut(url);
            if (data != null) {
                JSONObject jo = new JSONObject();
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    jo.put(entry.getKey(), entry.getValue());
                }
                hp.setHeader("Content-type", "application/json");
                hp.setEntity(new StringEntity(jo.toString(), "utf-8"));
            }
            response = new DefaultHttpClient().execute(hp);
        }
        return response;
    }

    private HttpResponse sendRequest(String url, String method, String data)
            throws IOException {

        HttpResponse response = null;

        if (method.equals("GET")) {
            HttpGet hg = new HttpGet(url);
            response = new DefaultHttpClient().execute(hg);
        } else if (method.equals("POST")) {
            HttpPost hp = new HttpPost(url);
            if (data != null) {
                hp.setHeader("Content-type", "application/json");
                hp.setEntity(new StringEntity(data, "utf-8"));
            }
            response = new DefaultHttpClient().execute(hp);
        } else if (method.equals("PUT")) {
            HttpPut hp = new HttpPut(url);
            if (data != null) {
                hp.setHeader("Content-type", "application/json");
                hp.setEntity(new StringEntity(data, "utf-8"));
            }
            response = new DefaultHttpClient().execute(hp);
        }
        return response;
    }
}