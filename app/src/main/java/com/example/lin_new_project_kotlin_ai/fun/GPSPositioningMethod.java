package com.example.lin_new_project_kotlin_ai.fun;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

/**
 * Created by Tim on 2017/7/6.
 */

public class GPSPositioningMethod {
    private Activity activity;
    private LocationManager lms;
    private boolean getService = false;
    private String bestProvider = LocationManager.GPS_PROVIDER;    //最佳資訊提供者
    private Double myLongitude = 0.0;
    private Double myLatitude = 0.0;
    private Context context;
    public GPSPositioningMethod(LocationManager lms, Context context) {
        this.context = context;
        this.lms = lms;
    }
    //開始取得GPS定位資料(經緯度)
    public void startGPS(){
        if (lms.isProviderEnabled(LocationManager.GPS_PROVIDER) || lms.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            getService = true;	//確認開啟定位服務
            //如果GPS或網路定位開啟，呼叫locationServiceInitial()更新位置
//            locationServiceInitial();
            getLastKnownLocation();
        } else {
            Toast.makeText(context, "請開啟定位服務", Toast.LENGTH_LONG).show();
            context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));    //開啟設定頁面
        }
    }


//    //取得定位系統
//    private void locationServiceInitial() {
//        lms = (LocationManager) this.activity.getSystemService(LOCATION_SERVICE);    //取得系統定位服務
//        if (ActivityCompat.checkSelfPermission(this.activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//        Location location = lms.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);    //使用GPS定位座標
//        if (location == null){
//            Criteria criteria = new Criteria();    //資訊提供者選取標準
//            bestProvider = lms.getBestProvider(criteria, true);    //選擇精準度最高的提供者
//            location = lms.getLastKnownLocation(bestProvider);
//        }else {
//            location = lms.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);//
//            bestProvider = LocationManager.NETWORK_PROVIDER;
//
//        }
//        getLocation(location);
//    }
    //取得經緯度
    private void getLocation(Location location) {	//將定位資訊顯示在畫面中
        if(location != null) {
            Double longitude = location.getLongitude();	//取得經度
            Double latitude = location.getLatitude();	//取得緯度
//            Log.e("mygps","經度："+String.valueOf(longitude));
//            Log.e("mygps","緯度："+String.valueOf(latitude));
//            Toast.makeText(this.activity, "經度："+String.valueOf(longitude)+"\r\n"+"緯度："+String.valueOf(latitude), Toast.LENGTH_LONG).show();
            setMyLongitude(longitude);
            setMyLatitude(latitude);
        }
        else {
            Toast.makeText(this.activity, "無法定位座標", Toast.LENGTH_LONG).show();
        }
    }




    public Location getLastKnownLocation() {
        if( ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED ) {
            Location mLoc = null;
            LocationManager mLocManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            try {
                Criteria mCriteria = new Criteria();
                mCriteria.setAccuracy(Criteria.ACCURACY_COARSE);//some cell phone not support fine
                mCriteria.setAltitudeRequired(true);
                mCriteria.setBearingRequired(true);
                mCriteria.setCostAllowed(true);
                mCriteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
                mCriteria.setPowerRequirement(Criteria.POWER_LOW);

                Location gpsLocation = null;
                Location networkLocation = null;
                if(mLocManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    gpsLocation =  mLocManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                if(mLocManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
                    networkLocation =  mLocManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
                if (gpsLocation != null && networkLocation!=null) {
                    mLoc = getBetterLocation(gpsLocation, networkLocation);
                }else if(gpsLocation !=null) {
                    mLoc = gpsLocation;
                }else if (networkLocation != null) {
                    mLoc = networkLocation;
                }
                getLocation(mLoc);
            } catch (Exception e) {
                e.getStackTrace();
            }

            return mLoc;
        }else{
            return null;
        }

    }



    public  Location getBetterLocation(Location newLocation, Location currentBestLocation) {
        final int TWO_MINUTES = 1000 * 60 * 2;
        if (currentBestLocation == null) {
            return newLocation;
        }

        long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            Log.i("isSignificantlyNewer", "isSignificantlyNewer");
            return newLocation;
        } else if (isSignificantlyOlder) {
            Log.i("isSignificantlyOlder", "isSignificantlyOlder");
            return currentBestLocation;
        }

        int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = isSameProvider(newLocation.getProvider(),
                currentBestLocation.getProvider());
        if (isMoreAccurate) {
            return newLocation;
        } else if (isNewer && !isLessAccurate) {
            return newLocation;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return newLocation;
        }
        return currentBestLocation;
    }

    public  boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

    public Double getMyLongitude() {
        return myLongitude;
    }

    public void setMyLongitude(Double myLongitude) {
        this.myLongitude = myLongitude;
    }

    public Double getMyLatitude() {
        return myLatitude;
    }

    public void setMyLatitude(Double myLatitude) {
        this.myLatitude = myLatitude;
    }

}
