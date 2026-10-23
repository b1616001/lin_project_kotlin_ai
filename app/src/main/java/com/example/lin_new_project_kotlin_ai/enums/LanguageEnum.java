package com.example.lin_new_project_kotlin_ai.enums;


import android.util.Log;

import com.example.lin_new_project_kotlin_ai.R;
import com.example.lin_new_project_kotlin_ai.fun.CommonUtils;

import java.util.Locale;


public enum LanguageEnum {


    Japan(getString(R.string.jp), "JP", "ja"),
    English(getString(R.string.us), "US", "en"),
    Spanish(getString(R.string.es), "ES", "es"),
    Chinese(getString(R.string.cn), "CN", "zh"),
    TW(getString(R.string.tw), "TW", "zh");

    LanguageEnum(String Language, String Country, String LanguageCode) {
        this.Language = Language;
        this.Country = Country;
        this.LanguageCode = LanguageCode;
    }

    public String Language;
    public String Country;
    public String LanguageCode;

    public String getLanguage() {
        return Language;
    }

    public Locale getLocale() {
        return new Locale(getLanguageCode(), getCountry());
    }

    public void setLanguage(String language) {
        Language = language;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }

    public String getLanguageCode() {
        return LanguageCode;
    }

    public void setLanguageCode(String languageCode) {
        LanguageCode = languageCode;
    }

    public static LanguageEnum getLanguageEnum(String key, boolean bb) {
        Log.d("多國語系","key:"+ key);

        LanguageEnum languageEnum = English;//預設英文

        if (bb) {
            //比較的是Language欄位
            if (key.equals(getString(R.string.jp))){
               languageEnum = Japan;
           }else if (key.equals(getString(R.string.us))){
               languageEnum = English;
           }else if (key.equals(getString(R.string.es))){
               languageEnum = Spanish;
           }else if (key.equals(getString(R.string.cn))){
               languageEnum = Chinese;
           }else if (key.equals(getString(R.string.tw))){
               languageEnum = TW;
           }
        } else {
            //比較的是Country欄位
            switch (key) {
                case "JP":
                    languageEnum = Japan;
                    break;
                case "US":
                    languageEnum = English;
                    break;
                case "ES":
                    languageEnum = Spanish;
                    break;
                case "CN":
                    languageEnum = Chinese;
                    break;
                case "TW":
                    languageEnum = TW;
                    break;
            }
        }
        return languageEnum;
    }

    public static String getString(int id){
        return CommonUtils.getResourcesString(id);
    }
}
