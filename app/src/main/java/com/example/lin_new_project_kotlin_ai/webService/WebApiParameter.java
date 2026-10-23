package com.example.lin_new_project_kotlin_ai.webService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class WebApiParameter {
    public static String getApi_GetLoginServerAddressByQRCode_Parameter(String qrcode) {
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("qrcode", qrcode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
    public static String getApi_ConvertUserByOldAccount_Parameter(String account_no,String synce_password,String device_id) {
        JSONObject jsonObject=new JSONObject();
        try {
            jsonObject.put("account_no", account_no);
            jsonObject.put("synce_password", synce_password);
            jsonObject.put("device_id", device_id);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }
    public static  Map<String, Object> getApi_Uid_Parameter(String CHKTOKEN) {
        Map<String, Object> param = new HashMap<>();
        param.put("Sou", "android");
        param.put("CHKTOKEN", CHKTOKEN);
        return param;
    }
}
