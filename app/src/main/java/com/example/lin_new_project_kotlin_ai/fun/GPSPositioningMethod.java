package com.example.lin_new_project_kotlin_ai.fun;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 優化版 GPSPositioningMethod
 * - 支援多來源定位 (GPS 衛星、網路基地台、Passive)
 * - 支援即時 LocationListener 監聽更新，不再僅依賴容易為 null 的快取座標
 * - 支援非同步反向地理編碼 (Geocoder) 取得中文地址
 * - 支援定位回呼監聽器 (OnLocationChangeListener)
 * - 完整向下相容既有方法 (startGPS, getMyLongitude, getMyLatitude, getLastKnownLocation)
 */
public class GPSPositioningMethod {
    private static final String TAG = "GPSPositioningMethod";

    private final Context context;
    private final LocationManager lms;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private Double myLongitude = 0.0;
    private Double myLatitude = 0.0;
    private Location currentLocation = null;
    private String currentAddress = "";
    private boolean isListening = false;

    private OnLocationChangeListener onLocationChangeListener;

    public interface OnLocationChangeListener {
        void onLocationChanged(Location location, String address);
        void onLocationFailed(String errorMsg);
    }

    public GPSPositioningMethod(LocationManager lms, Context context) {
        this.context = context;
        this.lms = lms != null ? lms : (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public void setOnLocationChangeListener(OnLocationChangeListener listener) {
        this.onLocationChangeListener = listener;
    }

    /**
     * 檢查是否有定位權限
     */
    public boolean hasPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 檢查是否已開啟 GPS 或網路定位服務
     */
    public boolean isLocationEnabled() {
        if (lms == null) return false;
        return lms.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lms.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    /**
     * 開始取得 GPS 定位資料 (相容原方法)
     */
    public void startGPS() {
        startGPS(null);
    }

    /**
     * 開始取得定位並接收即時回呼
     */
    public void startGPS(OnLocationChangeListener listener) {
        if (listener != null) {
            this.onLocationChangeListener = listener;
        }

        if (!hasPermission()) {
            String errorMsg = "未取得定位權限 (ACCESS_FINE_LOCATION)";
            Log.w(TAG, errorMsg);
            Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show();
            if (onLocationChangeListener != null) {
                onLocationChangeListener.onLocationFailed(errorMsg);
            }
            return;
        }

        if (!isLocationEnabled()) {
            Toast.makeText(context, "請開啟定位服務 (GPS)", Toast.LENGTH_LONG).show();
            try {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Cannot open location settings: " + e.getMessage());
            }
            if (onLocationChangeListener != null) {
                onLocationChangeListener.onLocationFailed("定位服務未開啟");
            }
            return;
        }

        // 1. 先快速嘗試取得最後已知位置
        Location cachedLoc = getLastKnownLocation();
        if (cachedLoc != null) {
            dispatchLocation(cachedLoc);
        }

        // 2. 註冊即時定位監聽更新 (提升至最新精準座標)
        requestLocationUpdates();
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                if (currentLocation == null || getBetterLocation(location, currentLocation) == location) {
                    dispatchLocation(location);
                }
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}
    };

    /**
     * 註冊定位監聽
     */
    public void requestLocationUpdates() {
        if (!hasPermission() || lms == null || isListening) return;

        try {
            if (lms.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lms.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 1.0f, locationListener, Looper.getMainLooper());
                isListening = true;
            }
            if (lms.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lms.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000L, 1.0f, locationListener, Looper.getMainLooper());
                isListening = true;
            }
        } catch (SecurityException e) {
            Log.e(TAG, "requestLocationUpdates SecurityException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "requestLocationUpdates error: " + e.getMessage());
        }
    }

    /**
     * 停止監聽定位，節省電池電量
     */
    public void stopGPS() {
        if (lms != null && isListening) {
            try {
                lms.removeUpdates(locationListener);
            } catch (Exception e) {
                Log.e(TAG, "stopGPS error: " + e.getMessage());
            } finally {
                isListening = false;
            }
        }
    }

    private void dispatchLocation(Location location) {
        this.currentLocation = location;
        this.myLatitude = location.getLatitude();
        this.myLongitude = location.getLongitude();

        // 觸發反向地理編碼解析地址
        reverseGeocode(location);
    }

    private void reverseGeocode(Location location) {
        executor.execute(() -> {
            String addressText = "";
            try {
                if (Geocoder.isPresent()) {
                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses != null && !addresses.isEmpty()) {
                        Address addr = addresses.get(0);
                        StringBuilder sb = new StringBuilder();
                        if (addr.getAdminArea() != null) sb.append(addr.getAdminArea());
                        if (addr.getLocality() != null) sb.append(addr.getLocality());
                        if (addr.getSubLocality() != null) sb.append(addr.getSubLocality());
                        if (addr.getThoroughfare() != null) sb.append(addr.getThoroughfare());
                        if (addr.getSubThoroughfare() != null) sb.append(addr.getSubThoroughfare());
                        if (sb.length() == 0 && addr.getAddressLine(0) != null) {
                            sb.append(addr.getAddressLine(0));
                        }
                        addressText = sb.toString();
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Geocoder failed: " + e.getMessage());
            }

            final String finalAddr = addressText.isEmpty() ? "無法取得街道地址" : addressText;
            currentAddress = finalAddr;

            mainHandler.post(() -> {
                if (onLocationChangeListener != null) {
                    onLocationChangeListener.onLocationChanged(location, finalAddr);
                }
            });
        });
    }

    /**
     * 取得最後已知位置
     */
    public Location getLastKnownLocation() {
        if (!hasPermission() || lms == null) return null;

        Location bestLoc = null;
        try {
            if (lms.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Location loc = lms.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (loc != null) bestLoc = loc;
            }
            if (lms.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                Location netLoc = lms.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (netLoc != null) {
                    bestLoc = getBetterLocation(netLoc, bestLoc);
                }
            }
            if (lms.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                Location passLoc = lms.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);
                if (passLoc != null) {
                    bestLoc = getBetterLocation(passLoc, bestLoc);
                }
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getLastKnownLocation SecurityException: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "getLastKnownLocation error: " + e.getMessage());
        }

        if (bestLoc != null) {
            this.currentLocation = bestLoc;
            this.myLatitude = bestLoc.getLatitude();
            this.myLongitude = bestLoc.getLongitude();
        }
        return bestLoc;
    }

    public Location getBetterLocation(Location newLocation, Location currentBestLocation) {
        final int TWO_MINUTES = 1000 * 60 * 2;
        if (currentBestLocation == null) {
            return newLocation;
        }

        long timeDelta = newLocation.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return newLocation;
        } else if (isSignificantlyOlder) {
            return currentBestLocation;
        }

        int accuracyDelta = (int) (newLocation.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = isSameProvider(newLocation.getProvider(), currentBestLocation.getProvider());
        if (isMoreAccurate) {
            return newLocation;
        } else if (isNewer && !isLessAccurate) {
            return newLocation;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return newLocation;
        }
        return currentBestLocation;
    }

    public boolean isSameProvider(String provider1, String provider2) {
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

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public String getCurrentAddress() {
        return currentAddress;
    }
}
