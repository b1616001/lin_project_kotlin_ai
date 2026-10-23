package com.example.lin_new_project_kotlin_ai.fragment;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.lin_new_project_kotlin_ai.R;
import com.example.lin_new_project_kotlin_ai.databinding.FragmentPublicspaceBinding;
import com.example.lin_new_project_kotlin_ai.fun.AndroidQStorageSaveUtils;
import com.example.lin_new_project_kotlin_ai.fun.ImageMethod;
import com.example.lin_new_project_kotlin_ai.fun.ToastMethod;
import com.example.lin_new_project_kotlin_ai.viewBinding.BaseBindingFragment;

import java.io.IOException;


public class PublicSpaceFragment extends BaseBindingFragment<FragmentPublicspaceBinding> {
    private static final int CAMERA_WITH_DATA = 3023;
    private static final int PHOTO_PICKED_WITH_DATA = 3021;
    private int requestCode = 0;
    private String fileName = "";
    private Uri uri;
    private Uri bitmap_uri;
    private Uri txt_uri;

    @Override
    protected FragmentPublicspaceBinding onCreateViewBinding(@NonNull LayoutInflater layoutInflater) {
        return FragmentPublicspaceBinding.inflate(layoutInflater);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView();
    }

    private void initView() {
        getBinding().tvCamera.setOnClickListener(view -> onCamera());
        getBinding().tvFile.setOnClickListener(view -> onFile());
        getBinding().tvBitmap.setOnClickListener(view -> onBitmap());
        getBinding().tvText.setOnClickListener(view -> onText());
        getBinding().tvTxt.setOnClickListener(view -> onTxt());
        getBinding().tvReadTxt.setOnClickListener(view -> onReadTxt());
        getBinding().tvVideo.setOnClickListener(view -> onVideo());
        getBinding().tvAudio.setOnClickListener(view -> onAudio());

    }

    public void onCamera() {
        requestCode = CAMERA_WITH_DATA;
        uri = AndroidQStorageSaveUtils.customizedFilePath(getContext(), Environment.DIRECTORY_DCIM, "Lin");
        someActivityResultLauncher.launch(new Intent(MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, uri));
    }
    public void onFile() {
        requestCode = PHOTO_PICKED_WITH_DATA;
        Intent intent = AndroidQStorageSaveUtils.doPickPhotoFromGallery(getActivity());
        if (intent != null) {
            someActivityResultLauncher.launch(intent);
        }
    }



    public void onBitmap() {
        Bitmap bitmap = ImageMethod.getBitmap(ImageMethod.getDrawableByVERSION(getContext(), R.drawable.btn_bgd_blue));
        bitmap_uri = AndroidQStorageSaveUtils.saveBitmap(getContext(), bitmap, Environment.DIRECTORY_DCIM, "Lin");
        if (AndroidQStorageSaveUtils.fileUriIsExists(getContext(), bitmap_uri)) {
            ToastMethod.showToast(getContext(), "儲存成功");
        }
    }//

//    public void onDelete() {
//        if (bitmap_uri != null) {
//            String path = AndroidQStorageSaveUtils.Uri_to_path(getContext(), bitmap_uri);
//            if (AndroidQStorageSaveUtils.deleteFoder(new File(path))) {
//                ToastMethod.showToast(getContext(), "刪除成功");
//                bitmap_uri=null;
//
//            }
//        } else {
//            ToastMethod.showToast(getContext(), "無圖片");
//        }//
//    }

    public void onText() {
        try {
            txt_uri = AndroidQStorageSaveUtils.saveNotebookInputStream(getContext(), getResources().getAssets().open("test.txt"),
                    "txt", Environment.DIRECTORY_DOCUMENTS, "Lin");
            if (AndroidQStorageSaveUtils.fileUriIsExists(getContext(), txt_uri)) {
                ToastMethod.showToast(getContext(), "儲存成功");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onTxt() {
        txt_uri = AndroidQStorageSaveUtils.saveNotebookText(getContext(), "{ssssssssffsafasf}",
                Environment.DIRECTORY_DOCUMENTS, "Lin");
        if (AndroidQStorageSaveUtils.fileUriIsExists(getContext(), txt_uri)) {
            ToastMethod.showToast(getContext(), "儲存成功");
        }
    }

    public void onReadTxt() {
        if (txt_uri != null) {
            String tt = AndroidQStorageSaveUtils.getReadTxt(getContext(), txt_uri);
            ToastMethod.showToast(getContext(), tt);
        } else {
            ToastMethod.showToast(getContext(), "無記事本");
        }
    }

    public void onVideo() {
        try {
            Uri uri = AndroidQStorageSaveUtils.saveVideo(getContext(), getResources().getAssets().open("test.mp4"),
                    "mp4", Environment.DIRECTORY_MOVIES, "Lin");
            if (AndroidQStorageSaveUtils.fileUriIsExists(getContext(), uri)) {
                ToastMethod.showToast(getContext(), "儲存成功");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onAudio() {
        try {
            Uri uri = AndroidQStorageSaveUtils.saveAudio(getContext(), getResources().getAssets().open("六月的雨.mp3"),
                    "mp3", Environment.DIRECTORY_MUSIC, "Lin");
            Log.d("路徑", uri.getPath());
            if (AndroidQStorageSaveUtils.fileUriIsExists(getContext(), uri)) {
                ToastMethod.showToast(getContext(), "儲存成功");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {

                    if (result.getResultCode() == Activity.RESULT_OK) {
                        // There are no request codes
                        Intent data = result.getData();
                        if (requestCode == CAMERA_WITH_DATA) {
                            fileName = AndroidQStorageSaveUtils.Uri_to_path(getContext(), uri);//公有路徑轉換
//                            fileName="/storage/emulated/0/DCIM/Lin/sample_1566657297012.png";
                            Log.d("圖片抓取2A", "路徑" + fileName);
                        } else if (requestCode == PHOTO_PICKED_WITH_DATA) {
                            Log.d("圖片抓取2", "路徑" + data.getData().getPath());
                            uri = data.getData();
                            fileName = AndroidQStorageSaveUtils.Uri_to_path(getContext(), data.getData());//公有路徑轉換

                        }
                        if (fileName != null) {
                            getBinding().imageView.setImageBitmap(AndroidQStorageSaveUtils.getBitmap(getContext(), fileName));
                        } else {
                            ToastMethod.showToast(getContext(), "路徑為NULL");
                        }
                    }
                }
            });
}