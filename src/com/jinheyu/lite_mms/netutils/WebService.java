package com.jinheyu.lite_mms.netutils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Pair;
import com.jinheyu.lite_mms.MyApp;
import com.jinheyu.lite_mms.Utils;
import com.jinheyu.lite_mms.data_structures.*;
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
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by xc on 13-8-12.
 */
public class WebService {

    private static final int MILLSECONDS_PER_SECOND = 1000;
    private static WebService instance;
    private Context context;

    private WebService(Context c) {
        this.context = c;
    }

    public static WebService getInstance(Context c) {
        if (instance == null) {
            instance = new WebService(c);
        }
        return instance;
    }

    /**
     * @param username username to authenticate
     * @param password password to authenticate
     * @return com.jinheyu.lite_mms.data_structures.User
     * @throws IOException     send request fails
     * @throws JSONException   parse fails or doesn't yield a JSONObject.
     * @throws BadRequest      an unexpected response status code
     * @throws ValidationError login failed because of authentication fails
     */
    public User login(String username, String password) throws IOException, JSONException, BadRequest, ValidationError, NumberFormatException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("username", username);
        params.put("password", password);
        String url = composeUrl("auth_ws", "login", params);
        HttpResponse response = sendRequest(url, "POST", "");
        int stateCode = response.getStatusLine().getStatusCode();
        String result = EntityUtils.toString(response.getEntity());
        JSONObject root = new JSONObject(result);
        if (stateCode == HttpStatus.SC_OK) {
            int[] teamIdList = Utils.parse2IntegerArray(root.getString("teamID"));
            int[] departmentIdList = Utils.parse2IntegerArray(root.getString("departmentID"));
            User user = new User(root.getInt("userID"), root.getString("username"), root.getString("token"), root.getInt("userGroup"), teamIdList, departmentIdList);
            user.setTeamList(getTeamListByIds(teamIdList));
            return user;
        } else if (stateCode == HttpStatus.SC_FORBIDDEN) {
            throw new ValidationError(root.getString("reason"));
        } else {
            throw new BadRequest(result);
        }
    }

    private List<Team> getTeamListByIds(int[] teamIdList) throws JSONException, IOException, BadRequest {
        List<Team> teamList = new ArrayList<Team>();
        for (Team team : getTeamList()) {
            if (Arrays.binarySearch(teamIdList, team.getId()) > -1) {
                teamList.add(team);
            }
        }
        return teamList;
    }

    public List<UnloadSession> getUnloadSessionList() throws JSONException, IOException, BadRequest {
        List<UnloadSession> ret;

        String url = composeUrl("cargo_ws", "unload-session-list");
        HttpResponse response = sendRequest(url);
        int stateCode = response.getStatusLine().getStatusCode();
        String result = EntityUtils.toString(response.getEntity());
        JSONObject root = new JSONObject(result);
        if (stateCode == HttpStatus.SC_OK) {
            int cnt = root.getInt("total_cnt");
            ret = new ArrayList<UnloadSession>(cnt);
            JSONArray data = root.getJSONArray("data");
            for (int i = 0; i < data.length(); ++i) {
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
        List<DeliverySession> deliverySessionList;
        String url = composeUrl("delivery_ws", "delivery-session-list");
        HttpResponse response = sendRequest(url);
        int stateCode = response.getStatusLine().getStatusCode();
        String result = EntityUtils.toString(response.getEntity());
        if (stateCode == HttpStatus.SC_OK) {
            deliverySessionList = new ArrayList<DeliverySession>();
            JSONArray jsonArray = new JSONArray(result);
            for (int i = 0; i < jsonArray.length(); ++i) {
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

        if (stateCode == HttpStatus.SC_OK) {
            ret = new ArrayList<Harbor>();
            JSONArray data = new JSONArray(result);
            for (int i = 0; i < data.length(); ++i) {
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

        if (stateCode == HttpStatus.SC_OK) {
            ret = new ArrayList<Customer>();
            JSONArray data = new JSONArray(result);
            for (int i = 0; i < data.length(); ++i) {
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
        params.put("is_finished", done ? "1" : "0");
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
        if (stateCode == HttpStatus.SC_OK) {
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
                    for (int i = 0; i < _storeBills_.length(); ++i) {
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

    public void createDeliveryTask(DeliverySession deliverySession, boolean finished, int actorId,
                                   List<Pair<StoreBill, Boolean>> storeBillPairList, int remainWeight) throws JSONException, IOException, BadRequest {
        Map<String, String> params = new HashMap<String, String>();
        params.put("sid", String.valueOf(deliverySession.getId()));
        params.put("is_finished", finished ? "1" : "0");
        params.put("actor_id", String.valueOf(actorId));
        if (!finished) {
            params.put("remain", String.valueOf(remainWeight));
        }
        String url = composeUrl("delivery_ws", "delivery-task", params);
        JSONArray jsonArray = new JSONArray();
        for (Pair<StoreBill, Boolean> pair : storeBillPairList) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("store_bill_id", pair.first.getId());
            jsonObject.put("is_finished", pair.second ? "1" : "0");
            jsonArray.put(jsonObject);
        }
        HttpResponse httpResponse = sendRequest(url, "POST", jsonArray.toString());
        int stateCode = httpResponse.getStatusLine().getStatusCode();
        if (stateCode != HttpStatus.SC_OK) {
            throw new BadRequest(EntityUtils.toString(httpResponse.getEntity()));
        }
    }

    public List<WorkCommand> getWorkCommandList(int department_id, int team_id, int[] status) throws IOException, JSONException, BadRequest {
        List<WorkCommand> list;
        Map<String, String> params = new LinkedHashMap<String, String>();
        List<String> statusList = new ArrayList<String>();
        for (int i : status) {
            statusList.add(String.valueOf(i));
        }
        params.put("status", Utils.join(statusList, ","));
        if (department_id != Integer.MIN_VALUE) {
            params.put("department_id", String.valueOf(department_id));
        }
        if (team_id != Integer.MIN_VALUE) {
            params.put("team_id", String.valueOf(team_id));
        }
        String url = composeUrl("manufacture_ws", "work-command-list", params);
        HttpResponse response = sendRequest(url);
        int stateCode = response.getStatusLine().getStatusCode();
        if (stateCode != HttpStatus.SC_OK) {
            throw new BadRequest(EntityUtils.toString(response.getEntity()));
        } else {
            list = getWorkCommandListFromResp(response);
        }
        return list;
    }

    public List<WorkCommand> getWorkCommandListByDepartmentId(int department_id, int[] status) throws JSONException, IOException, BadRequest {
        return getWorkCommandList(department_id, Integer.MIN_VALUE, status);
    }

    public List<WorkCommand> getWorkCommandListByTeamId(int team_id, int[] status) throws JSONException, IOException, BadRequest {
        return getWorkCommandList(Integer.MIN_VALUE, team_id, status);
    }

    private List<Team> _getTeamList(Map<String, String> params) throws IOException, JSONException, BadRequest {
        String url = composeUrl("manufacture_ws", "team-list", params);
        HttpResponse response = sendRequest(url);
        String result = EntityUtils.toString(response.getEntity());
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new BadRequest(result);
        } else {
            return _parseTeamList(result);
        }
    }

    private List<Team> _parseTeamList(String result) throws JSONException {
        List<Team> teamList = new ArrayList<Team>();
        JSONArray objs = new JSONArray(result);
        for (int i = 0; i < objs.length(); i++) {
            JSONObject obj = objs.getJSONObject(i);
            teamList.add(new Team(obj.getInt("id"), obj.getString("name")));
        }
        return teamList;
    }

    public List<Team> getTeamList(int department_id) throws JSONException, IOException, BadRequest {
        Map<String, String> params = new LinkedHashMap<String, String>();
        params.put("department_id", String.valueOf(department_id));
        return _getTeamList(params);
    }

    public List<Team> getTeamList() throws JSONException, IOException, BadRequest {
        return _getTeamList(null);
    }

    private List<WorkCommand> getWorkCommandListFromResp(HttpResponse response) throws IOException, JSONException {
        List<WorkCommand> list = new ArrayList<WorkCommand>();
        String result = EntityUtils.toString(response.getEntity());
        JSONObject object = new JSONObject(result);
        JSONArray array = new JSONArray(object.getString("data"));
        for (int i = 0; i < array.length(); i++) {
            JSONObject data = array.getJSONObject(i);
            list.add(parse2WorkCommand(data));
        }
        return list;
    }

    private WorkCommand parse2WorkCommand(JSONObject o) throws JSONException {
        int id = o.getInt("id");
        String customerName = o.getString("customerName");
        boolean urgent = o.getInt("isUrgent") == 1;
        String spec = o.getString("spec");
        String type = o.getString("type");
        String lastMod = o.getString("lastMod");
        int orderID = o.getInt("orderID");
        int orderNum = o.getInt("orderNum");
        int orderType = o.getInt("orderType");
        int orgCount = o.getInt("orgCount");
        int orgWeight = o.getInt("orgWeight");
        String picPath = o.getString("picPath");
        Date last_mod = new Date(o.getLong("lastMod") * MILLSECONDS_PER_SECOND);
        Date orderCreateTime = new Date(o.getLong("orderCreateTime") * MILLSECONDS_PER_SECOND);
        int subOrderId = o.getInt("subOrderId");
        int processedCount = o.getInt("processedCount");
        int processedWeight = o.getInt("processedWeight");
        String productName = o.getString("productName");
        int status = o.getInt("status");
        String tech_req = o.getString("technicalRequirements");
        int deduction = o.getInt("deduction");
        boolean rejected = o.getInt("rejected") == 1;
        String unit = o.getString("unit");
        String procedure = o.getString("procedure");
        String previousProcedure = o.getString("previousProcedure");
        WorkCommand wc = new WorkCommand(id, orgCount, orgWeight, status);
        wc.setPicPath(picPath);
        wc.setProcessedWeight(processedWeight);
        wc.setProcessedCnt(processedCount);
        wc.setOrderType(orderType);
        if (!Utils.isEmptyString(o.getString("team"))) {
            JSONObject team = o.getJSONObject("team");
            String teamName = team.getString("name");
            wc.setTeamId(team.getInt("id"));
        }
        if (!Utils.isEmptyString(o.getString("department"))) {
            JSONObject department = o.getJSONObject("department");
            String departmentName = department.getString("name");
            wc.setDepartmentId(department.getInt("id"));
        }
        return wc;
    }

    public Bitmap getImageFromUrl(String pirUrl) throws IOException {
        Pair<String, Integer> pair = Utils.getServerAddress(context);
        URL url = new URL(String.format("http://%s:%d%s", pair.first, pair.second, pirUrl));
        InputStream is = url.openStream();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inPurgeable = true;
        options.inInputShareable = true;
        return BitmapFactory.decodeStream(is, null, options);
    }

    private String composeUrl(String blueprint, String path) {
        return composeUrl(blueprint, path, null);
    }

    private String composeUrl(String blueprint, String path, Map<String, String> params) {
        Pair<String, Integer> pair = Utils.getServerAddress(context);
        StringBuilder ret = new StringBuilder();
        ret.append(String.format("http://%s:%d/%s/%s", pair.first, pair.second, blueprint, path));
        if (params != null) {
            boolean first = true;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                ret.append(first ? "?" : "&").append(entry.getKey()).append("=").append(entry.getValue());
                first = false;
            }
        }
        return ret.toString();
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

    public String updateWorkCommand(long workCommandId, int action_code, HashMap<String, String> params) throws IOException, JSONException, BadRequest {
        if (params == null) {
            params = new HashMap<String, String>();
        }
        params.put("actor_id", String.valueOf(MyApp.getCurrentUser().getId()));
        params.put("work_command_id", String.valueOf(workCommandId));
        params.put("action", String.valueOf(action_code));
        String url = composeUrl("manufacture_ws", "work-command", params);
        HttpResponse response = sendRequest(url, "PUT", (String) null);
        String result = EntityUtils.toString(response.getEntity(), "utf-8");
        if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
            throw new BadRequest(result);
        } else {
            return result;
        }
    }
}
