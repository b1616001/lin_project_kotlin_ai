package com.example.lin_new_project_kotlin_ai.`fun`

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.LocaleList
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.lin_new_project_kotlin_ai.MainActivity.Companion.mainActivity
import com.example.lin_new_project_kotlin_ai.MyApplication
import com.example.lin_new_project_kotlin_ai.enums.LanguageEnum
import com.google.gson.Gson
import java.util.Locale

object LanguageUtils {
    @JvmStatic
    fun attachBaseContext(context: Context): Context? {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            return updateResources(context)
//        } else {
//            return context
//        }
        return updateResources(context)
    }

    private fun updateResources(context: Context): Context? {
        val resources = context.getResources()
        val languageEnum = LanguageEnum.getLanguageEnum(MmkvUtils.getLanguageCountry(), false)
        val locale = Locale(languageEnum.getLanguageCode(), languageEnum.getCountry())
        val configuration = resources.getConfiguration()
        configuration.setLocale(locale)
        configuration.setLocales(LocaleList(locale))
        return context.createConfigurationContext(configuration)
    }

    fun setLanguage(activity: Activity, languageEnum: LanguageEnum?) {
        Log.d("多國語系", "setLanguage:" + Gson().toJson(languageEnum!!))
        val newConfig = Configuration()
        newConfig.setLocales(LocaleList(Locale.FRENCH))
        activity.createConfigurationContext(newConfig)
    }

    fun refreshApp(activity: Activity, languageEnum: LanguageEnum?) {
        MmkvUtils.setLanguageCountry(languageEnum!!.getCountry())
        setLanguage(mainActivity!!, languageEnum)
        ActivityCompat.recreate(activity) //當前頁面直接刷新無須重啟Activity

        //刷新Activity
//        Intent intent = new Intent(activity, com.example.lin_new_project_kotlin.mainActivity!!.class);
//        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//        activity.startActivity(intent);
    }

    fun saveLanguage(languageEnum: LanguageEnum) {
        MmkvUtils.setLanguageCountry(languageEnum.getCountry())
    }

    fun updateAppLanguage(languageEnum: LanguageEnum) {
        val config: Configuration = MyApplication.instance.getResources().getConfiguration()
        config.locale = languageEnum.getLocale()
        MyApplication.instance.getResources()
            .updateConfiguration(config, MyApplication.instance.getResources().getDisplayMetrics())

        saveLanguage(languageEnum)
    }
}
