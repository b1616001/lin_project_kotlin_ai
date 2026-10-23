package com.example.lin_new_project_kotlin_ai.webService;


import java.util.Map;

import io.reactivex.Observable;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IWebApiPath {
    //2.4.1.會員上傳裝置ID
    @FormUrlEncoded
    @POST("getUid")
    Observable<String> apiUid(@FieldMap Map<String, Object> params);

//    //2.4.2.專用機取得使用者資料 (QRCode 用)
//    @FormUrlEncoded
//    @POST("Member/Sync_DeviceSyncUser")
//    Observable<SyncDeviceSyncUserBean> apiSyncDeviceSyncUser(@FieldMap Map<String, Object> params);
//
//    //2.4.3.專用機會員資料更新雲端會員資料
//    @FormUrlEncoded
//    @POST("Member/Sync_UpdateUserProfile")
//    Observable<SyncUpdateUserProfileBean> apiSyncUpdateUserProfile(@FieldMap Map<String, Object> params);//.
//
//    //2.4.4.專用機運動資料上傳
//    @FormUrlEncoded
//    @POST("TrainingDC/Sync_UploadTrainingData")
//    Observable<SyncUploadTrainingDataBean> apiSyncUploadTrainingData(@FieldMap Map<String, Object> params);//.
//
//    //2.4.5.專用機取得使用者資料
//    @FormUrlEncoded
//    @POST("Member/Sync_GetUserInfo")
//    Observable<SyncGetUserInfoBean> apiSyncGetUserInfo(@FieldMap Map<String, Object> params);//.
//
//    //2.4.6.專用機主動移除裝置與會員連結
//    @FormUrlEncoded
//    @POST("Member/DeleteSyncLink")
//    Observable<DeleteSyncLinkBean> apiDeleteSyncLink(@FieldMap Map<String, Object> params);//.
//
//    //2.4.7.取得target region server網址
//    @Headers("Content-Type: application/json")
//    @POST("api/GetLoginServerAddressByQRCode")
//    Observable<GetLoginServerAddressByQRCodeBean> apiGetLoginServerAddressByQRCode(@Body String params);
//
//
//    //舊有雲端帳號->升級GDPR版本->呼叫ConvertUserByOldAccountNo 刷新資料->網路異常刷新失敗->登入帳號->
//    @Headers("Content-Type: application/json")
//    @POST("api/ConvertUserByOldAccountNo")
//    Observable<ConvertUserByOldAccountNoBean> apiConvertUserByOldAccountNo(@Body String params);
}
