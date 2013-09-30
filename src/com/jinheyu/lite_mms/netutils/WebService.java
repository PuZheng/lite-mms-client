package com.jinheyu.lite_mms.netutils;

import android.content.Context;
import android.util.Pair;

import com.jinheyu.lite_mms.MyApp;
import com.jinheyu.lite_mms.Utils;
import com.jinheyu.lite_mms.data_structures.Customer;
import com.jinheyu.lite_mms.data_structures.DeliverySession;
import com.jinheyu.lite_mms.data_structures.DeliverySessionDetail;
import com.jinheyu.lite_mms.data_structures.Harbor;
import com.jinheyu.lite_mms.data_structures.Order;
import com.jinheyu.lite_mms.data_structures.StoreBill;
import com.jinheyu.lite_mms.data_structures.SubOrder;
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
import java.util.Iterator;
import java.util.LinkedHashMap;
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

    public List<DeliverySession> getDeliverySessionList() throws IOException, JSONException, BadRequest {
        List<DeliverySession> deliverySessionList = null;

        String url = composeUrl("delivery_ws", "delivery-session-list");
        HttpResponse response = sendRequest(url);
        int stateCode = response.getStatusLine().getStatusCode();
        String result = EntityUtils.toString(response.getEntity());
        if (stateCode == 200) {
            deliverySessionList = new ArrayList<DeliverySession>();
            JSONArray jsonArray = new JSONArray(result);
            for (int i=0; i < jsonArray.length(); ++i) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                int id = jsonObject.getInt("sessionID");
                String plate = jsonObject.getString("plateNumber");
                boolean locked = (jsonObject.getInt("isLocked") == 1);
                deliverySessionList.add(new DeliverySession(id, plate, locked));
            }
        } else {
            throw new BadRequest(result);
        }

        return deliverySessionList;
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

    public void createUnloadTask(UnloadSession unloadSession, Harbor harbor, Customer customer,
                                 boolean done, String picPath) throws BadRequest, IOException {
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

    public DeliverySessionDetail getDeliverySessionDetail(int id) throws IOException, JSONException, BadRequest {

        DeliverySessionDetail ret;
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("id", String.valueOf(id));
        String url = composeUrl("delivery_ws", "delivery-session", params);

        HttpResponse response = sendRequest(url);

        int stateCode = response.getStatusLine().getStatusCode();
        String result = EntityUtils.toString(response.getEntity());
        if (stateCode == 200) {
            JSONObject root = new JSONObject(result);
            String plate = root.getString("plate");
            ret = new DeliverySessionDetail(id, plate);

            JSONObject jsonObject = new JSONObject(result).getJSONObject("store_bills");
            Iterator iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String customerOrderNumber = (String) iterator.next();
                Order order = new Order();
                order.setCustomerOrderNumber(customerOrderNumber);

                JSONObject _subOrderMap_ = jsonObject.getJSONObject(customerOrderNumber);
                Iterator iterator1 = _subOrderMap_.keys();
                while (iterator1.hasNext()) {
                    String subOrderId = (String) iterator1.next();
                    SubOrder subOrder = new SubOrder(Integer.valueOf(subOrderId));
                    JSONArray _storeBills_ = _subOrderMap_.getJSONArray(subOrderId);
                    for (int i=0; i < _storeBills_.length(); ++i) {
                        JSONObject _storeBill_ = _storeBills_.getJSONObject(i);
                        int storeBillId = _storeBill_.getInt("id");
                        String harborName = _storeBill_.getString("harbor");
                        String productName = _storeBill_.getString("product_name");
                        String customerName = _storeBill_.getString("customer_name");
                        String picUrl = _storeBill_.getString("pic_url");
                        String unit = _storeBill_.getString("unit");
                        int weight = _storeBill_.getInt("weight");
                        String spec = _storeBill_.getString("spec");
                        String type = _storeBill_.getString("type");
                        subOrder.addStoreBill(new StoreBill(storeBillId, harborName, productName, customerName, picUrl, unit, weight, spec, type));
                    }
                    order.addSubOrder(subOrder);
                }
                ret.addOrder(order);
            }
        } else {
            throw new BadRequest(result);
        }
        return ret;
    }

    public void createDeliveryTask(DeliverySession deliverySession, boolean finished, User user,
                                   List<Pair<StoreBill, Boolean>> storeBillPairList, int remainWeight) throws JSONException, IOException, BadRequest, TaskFlowDelayed {
        Map<String, String> params = new HashMap<String, String>();
        params.put("sid", String.valueOf(deliverySession.getId()));
        params.put("is_finished", finished? "1": "0");
        params.put("auth_token", user.getToken());
        params.put("remain", String.valueOf(remainWeight));
        String url = composeUrl("delivery_ws", "delivery-task", params);
        JSONArray jsonArray = new JSONArray();
        for (Pair<StoreBill, Boolean> pair: storeBillPairList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("store_bill_id", pair.first.getId());
            jsonObject.put("is_finished", pair.second? true: false);
            jsonArray.put(jsonObject);
        }
        HttpResponse httpResponse = sendRequest(url, "POST", jsonArray.toString());
        int stateCode = httpResponse.getStatusLine().getStatusCode();
        if (stateCode == 201) {
            throw new TaskFlowDelayed("您提交的剩余重量异常，已经生成一个工作流，请催促收发员处理！");
        }
        if (stateCode != 200) {
            throw new BadRequest(EntityUtils.toString(httpResponse.getEntity()));
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
