package com.example.lin_new_project_kotlin_ai.fun;



import com.tencent.mmkv.MMKV;

import java.util.Locale;

public class MmkvUtils {
    public MmkvUtils() {

    }
    public static String getLanguageCountry() {
        //key不能使用LanguageCountry，會遇到如果沒有值的情況固定回US
        return MMKV.defaultMMKV().decodeString("Language", Locale.getDefault().getCountry());//Language的Country欄位,Locale.getDefault().getCountry()
    }

    public static void setLanguageCountry(String LanguageCountry) {
        MMKV.defaultMMKV().encode("Language", LanguageCountry);//Language的Country欄位,Locale.getDefault().getCountry()
    }

//    public boolean isFormal() {
//        boolean isFormal = MMKV.defaultMMKV().decodeBool("isFormal", isMmkvFormal);
//        Log.d("版本","isFormal:"+isFormal);
//        return isFormal;
//    }
//
//    public void setFormal(boolean formal) {
//        MMKV.defaultMMKV().encode("isFormal", formal);
//        Log.d("版本","isFormal:"+formal);
//    }
//
//    public DeviceSettingBean getDeviceSettingBean() {
//        return MMKV.defaultMMKV().decodeParcelable("DeviceSettingBean", DeviceSettingBean.class, new DeviceSettingBean());
//    }
//
//    public void setDeviceSettingBean(DeviceSettingBean deviceSettingBean) {
//        MMKV.defaultMMKV().encode("DeviceSettingBean", deviceSettingBean);
//        callSync();
//    }
//
//    public TreadmillController.TreadmillSetting getDefaultSetting() {
//        String defaultSetting = MMKV.defaultMMKV().decodeString("DefaultSetting", "");
//        if (!CommonUtils.checkStr(defaultSetting)) {
//            //輪徑/最小速度/最大速度/incline 階數
//            return new TreadmillController.TreadmillSetting(300, 8, 200, 31);
//        } else {
//            return new Gson().fromJson(defaultSetting, TreadmillController.TreadmillSetting.class);
//        }
//    }
//
//    public void setDefaultSetting(TreadmillController.TreadmillSetting treadmillSetting) {
//        //電跑 給下控的設定檔
//        String defaultSetting = new Gson().toJson(treadmillSetting);
//        MMKV.defaultMMKV().encode("DefaultSetting", defaultSetting);
//        callSync();
//    }
//
//    public void removeDeviceSettingBean() {
//        MMKV.defaultMMKV().remove("DeviceSettingBean");
//    }
//
//    public int getDeviceSettingBeanVersion() {
//        return MMKV.defaultMMKV().decodeInt("DeviceSettingBeanVersion", 0);
//    }
//
//    public void setDeviceSettingBeanVersion(int deviceSettingBeanVersion) {
//        MMKV.defaultMMKV().encode("DeviceSettingBeanVersion", deviceSettingBeanVersion);
//        callSync();
//    }
//
//    public boolean isClearDatabase() {
//        return MMKV.defaultMMKV().decodeBool("ClearDatabase", true);
//    }
//
//    public void setClearDatabase(boolean ClearDatabase) {
//        MMKV.defaultMMKV().encode("ClearDatabase", ClearDatabase);
//        callSync();
//    }
//
//    public void setAudioSize(int audio) {
//        MMKV.defaultMMKV().encode("AudioSize", audio);//音量大小
//        callSync();
//    }
//
//    public int getAudioSize() {
//        return MMKV.defaultMMKV().decodeInt("AudioSize", 0);
//    }
//
//    public int getBrakeLeve() {
//        return MMKV.defaultMMKV().decodeInt("BrakeLeve", 0);
//    }


}
