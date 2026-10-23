package com.example.lin_new_project_kotlin_ai.`fun`

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.io.FileDescriptor
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream


object AndroidQStorageSaveUtils {
    fun getFileDescriptor(context: Context, uri: Uri, mode: String): FileDescriptor? {
        try {
            //r（只讀）、w（只寫）、rw（讀寫）和 rwt（覆盖）
            val parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, mode)
            val fileDescriptor = parcelFileDescriptor!!.getFileDescriptor()
            return fileDescriptor
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    @JvmStatic
    fun fileUriIsExists(context: Context, uri: Uri): Boolean {
        var isExists = false
        try {
            //r（只讀）、w（只寫）、rw（讀寫）和 rwt（覆盖）
            val parcelFileDescriptor = context.getContentResolver().openFileDescriptor(uri, "r")
            isExists = parcelFileDescriptor!!.getFileDescriptor().valid()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return isExists
    }

    @JvmStatic
    fun customizedFilePath(context: Context, dirName: String?, Folder: String?): Uri? {
        //Pictures、DCIM都是存放圖片
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "title_1")
            values.put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "sample_" + System.currentTimeMillis() + ".png"
            )
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            values.put(MediaStore.Images.Media.RELATIVE_PATH, dirName + "/" + Folder) //Folder資料夾名稱
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            return context.getContentResolver().insert(contentUri, values)
        } else {
            val mediaStorageDir = getFolder(dirName + "/" + Folder)
            val filename = mediaStorageDir.getPath() + File.separator +
                    "sample_" + System.currentTimeMillis() + ".png" //路徑
            val file = File(filename)
            Log.d("圖片抓取", file.getAbsolutePath())
            return FileMethod.getFile_uri(context, filename)
        }
    }

    @JvmStatic
    fun saveNotebookText(
        context: Context,
        information: String,
        dirName: String,
        Folder: String?
    ): Uri? {
        val contentUri: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (dirName == Environment.DIRECTORY_DOWNLOADS) {
                contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            } else {
                contentUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            }
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "title_1")
            values.put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "sample_" + System.currentTimeMillis() + ".txt"
            )
            values.put(MediaStore.Images.Media.RELATIVE_PATH, dirName + "/" + Folder)
            val uri = context.getContentResolver().insert(contentUri, values)
            try {
                val outputStream = context.getContentResolver().openOutputStream(uri!!)
                outputStream!!.write(information.toByteArray())
                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return uri
        } else {
            val mediaStorageDir = getFolder(dirName + "/" + Folder)
            val filename = mediaStorageDir.getPath() + File.separator +
                    "sample_" + System.currentTimeMillis() + ".txt" //路徑
            Log.d("txt路徑", filename)
            val fOut: FileOutputStream?
            try {
                fOut = FileOutputStream(filename)
                fOut.write(information.toByteArray())
                fOut.flush()
                fOut.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return FileMethod.getFile_uri(context, filename)
        }
    }

    @JvmStatic
    fun saveNotebookInputStream(
        context: Context,
        stream: InputStream,
        suffix: String?,
        dirName: String,
        Folder: String?
    ): Uri? {
        val contentUri: Uri
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (dirName == Environment.DIRECTORY_DOWNLOADS) {
                contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
            } else {
                contentUri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            }
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "title_1")
            values.put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "sample_" + System.currentTimeMillis() + "." + suffix
            )
            values.put(MediaStore.Images.Media.RELATIVE_PATH, dirName + "/" + Folder)
            val uri = context.getContentResolver().insert(contentUri, values)
            try {
                val outputStream = context.getContentResolver().openOutputStream(uri!!)
                var read: Int
                val buffer = ByteArray(2048)
                do {
                    read = stream.read(buffer)
                    outputStream!!.write(buffer, 0, read)
                } while (read != -1)

                outputStream.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return uri
        } else {
            return saveArchives_old(context, dirName + "/" + Folder, suffix, stream)
        }
    }

    private fun saveArchives_old(
        context: Context?,
        Folder: String,
        suffix: String?,
        stream: InputStream
    ): Uri {
        val mediaStorageDir = getFolder(Folder)
        val filename = mediaStorageDir.getPath() + File.separator +
                "sample_" + System.currentTimeMillis() + "." + suffix //路徑
        Log.d("路徑", filename)
        val fOut: FileOutputStream?
        try {
            fOut = FileOutputStream(filename)
            var read: Int
            val buffer = ByteArray(2048)
            while ((stream.read(buffer).also { read = it }) != -1) {
                fOut.write(buffer, 0, read)
            }
            fOut.flush()
            fOut.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return FileMethod.getFile_uri(context, filename)
    }

    @JvmStatic
    fun saveBitmap(context: Context, bitmap: Bitmap, dirName: String?, Folder: String?): Uri? {
        //Pictures、DCIM都是存放圖片
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "title_1")
            values.put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "sample_" + System.currentTimeMillis() + ".png"
            )
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            values.put(MediaStore.Images.Media.RELATIVE_PATH, dirName + "/" + Folder)
            val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val uri = context.getContentResolver().insert(contentUri, values)
            try {
                val parcelFileDescriptor =
                    context.getContentResolver().openFileDescriptor(uri!!, "w")
                val outputStream = FileOutputStream(parcelFileDescriptor!!.getFileDescriptor())
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            return uri
        } else {
            val mediaStorageDir = getFolder(dirName + "/" + Folder)
            val filename = mediaStorageDir.getPath() + File.separator +
                    "sample_" + System.currentTimeMillis() + ".png" //路徑
            Log.d("圖片路徑", filename)
            val fOut: FileOutputStream?
            try {
                fOut = FileOutputStream(filename)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
                fOut.flush()
                fOut.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return FileMethod.getFile_uri(context, filename)
        }
    }


    @JvmStatic
    fun saveVideo(
        context: Context,
        stream: InputStream,
        suffix: String?,
        dirName: String?,
        Folder: String?
    ): Uri? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "title_1")
            values.put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "sample_" + System.currentTimeMillis() + "." + suffix
            )
            values.put(MediaStore.Images.Media.MIME_TYPE, "video/" + suffix)
            values.put(MediaStore.Images.Media.RELATIVE_PATH, dirName + "/" + Folder)
            val contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            val uri = context.getContentResolver().insert(contentUri, values)
            try {
                val outputStream = context.getContentResolver().openOutputStream(uri!!)
                var read: Int
                val buffer = ByteArray(2048)
                while ((stream.read(buffer).also { read = it }) != -1) {
                    outputStream!!.write(buffer, 0, read)
                }
                outputStream!!.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return uri
        } else {
            return saveArchives_old(context, dirName + "/" + Folder, suffix, stream)
        }
    }

    @JvmStatic
    fun saveAudio(
        context: Context,
        stream: InputStream,
        suffix: String?,
        dirName: String?,
        Folder: String?
    ): Uri? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues()
            values.put(MediaStore.Audio.Media.TITLE, "title_1")
            values.put(
                MediaStore.Audio.Media.DISPLAY_NAME,
                "sample_" + System.currentTimeMillis() + "." + suffix
            )
            values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/" + suffix)
            values.put(MediaStore.Audio.Media.RELATIVE_PATH, dirName + "/" + Folder)
            val contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val uri = context.getContentResolver().insert(contentUri, values)
            try {
                val outputStream = context.getContentResolver().openOutputStream(uri!!)
                var read: Int
                val buffer = ByteArray(2048)
                while ((stream.read(buffer).also { read = it }) != -1) {
                    outputStream!!.write(buffer, 0, read)
                }
                outputStream!!.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return uri
        } else {
            return saveArchives_old(context, dirName + "/" + Folder, suffix, stream)
        }
    }

    /**
     * 公有Uri轉路徑
     * 
     * @param uri 图片的uri
     */
    @JvmStatic
    fun Uri_to_path(context: Context, uri: Uri): String? {
        var path: String? = ""

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val cursor = context.getContentResolver().query(
                uri, arrayOf<String>(
                    MediaStore.Images.Media.DATA,
                    MediaStore.Images.Media.DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_ADDED,
                    MediaStore.Images.Media._ID
                ), null, null, null, null
            )
            while (cursor!!.moveToNext()) {
                path = cursor.getString(0)
            }
            cursor.close()
            return path
        } else {
            return getPathFromUri(context, uri)
            //            path = uri.getPath();
//            String authroity = uri.getAuthority();
//            String sdPath = Environment.getExternalStorageDirectory().getAbsolutePath();
//            Log.d("圖片抓取N", path + "," + authroity + "," + sdPath + ",");
//            if (!path.startsWith(sdPath)) {
//                int sepIndex = path.indexOf(File.separator, 1);
//                if (sepIndex == -1) path = null;
//                else {
//                    Log.d("圖片抓取N2", path.substring(sepIndex));
//                    path = sdPath + path.substring(sepIndex);
//                }
//            }
//            return path;
        }
    }


    @JvmStatic
    fun doPickPhotoFromGallery(context: Context): Intent? {
        try {
//            final Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
//            intent.setType("image/*");


//


            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val packageManager: PackageManager = context.getPackageManager()
            val intent = Intent(Intent.ACTION_PICK, uri)
            val list: MutableList<ResolveInfo?> =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            if (list.size > 0) {
                // 如果有可用的Activity
                val picker = Intent(Intent.ACTION_PICK, uri)
                // 使用Intent Chooser
                return Intent.createChooser(picker, "選取圖片")
            }
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }

        return null
    }

    /**
     * 公有路徑轉Uri
     * 
     * @param path 图片的path
     */
    fun getImageContentUri(context: Context, path: String): Uri? {
        val cursor = context.getContentResolver().query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf<String>(MediaStore.Images.Media._ID), MediaStore.Images.Media.DATA + "=? ",
            arrayOf<String?>(path), null
        )
        if (cursor != null && cursor.moveToFirst()) {
            @SuppressLint("Range") val id =
                cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
            val baseUri = Uri.parse("content://media/external/images/media")
            cursor.close()
            return Uri.withAppendedPath(baseUri, "" + id)
        } else {
            if (File(path).exists()) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DATA, path)
                return context.getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            } else {
                return null
            }
        }
    }

    @JvmStatic
    fun getBitmap(context: Context, path: String): Bitmap? {
        try {
            var degree = 0
            val uu: Uri
            if (path.contains("http")) {
                uu = Uri.parse(path)
            } else {
                uu = FileMethod.getFile_uri(context, path) //可使用
            }
            val u1 = getImageContentUri(context, path) //可使用
            //AndroidQStorageSaveUtils.getImageContentUri()與FileMethod.getFile_uri都可以將圖片路徑轉為Uri
            val inputStream = context.getContentResolver().openInputStream(uu) //InputStream只能被讀取一次
            val exifInterface = ExifInterface(context.getContentResolver().openInputStream(uu)!!)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
            var bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
            val matrix = Matrix()
            matrix.reset()
            matrix.setRotate(degree.toFloat())
            bitmap = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                true
            )
            return bitmap
        } catch (e: Exception) {
            Log.d("錯誤", e.toString())
            return null
        }
    }

    fun deleteFoder(file: File): Boolean {
        try {
            Log.d("刪除資料", file.toString())
            if (file.exists()) { // 判断文件是否存在
                if (file.isFile()) { // 判断是否是文件
                    file.delete() // delete()方法 是删除的意思;
                } else if (file.isDirectory()) { // 否则如果它是一个目录
                    val files = file.listFiles() // 声明目录下所有的文件 files[];
                    if (files != null) {
                        for (value in files) { // 遍历目录下所有的文件
                            deleteFoder(value) // 把每个文件 用这个方法进行迭代
                        }
                    }
                }
                val isSuccess = file.delete()
                if (!isSuccess) {
                    return true
                }
            }
        } catch (e: Exception) {
            Log.d("刪除資料", e.toString())
        }
        return false
    }

    @JvmStatic
    fun getReadTxt(context: Context, uri: Uri): String {
        var ss = ""
        try {
            val inputStream = context.getContentResolver().openInputStream(uri)
            val bytes = ByteArray(10240)
            val sb = StringBuffer()

            while (inputStream!!.read(bytes) != -1) {
                sb.append(String(bytes))
            }
            inputStream.close()
            ss = sb.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ss
    }

    private fun getFolder(Folder: String): File {
        //android 10以下使用 判斷是否有資料夾
        val mediaStorageDir = File(
            Environment
                .getExternalStorageDirectory(),
            Folder
        )
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs()
        }
        return mediaStorageDir
    }

    fun getPathFromUri(context: Context, uri: Uri): String? {
        var path: String? = null
        if (DocumentsContract.isDocumentUri(context, uri)) {
            //如果是document類型的Uri，通過document id處理，內部會調用Uri.decode(docId)進行解碼
            val docId: String = DocumentsContract.getDocumentId(uri)
            //primary:Azbtrace.txt
            //video:A1283522
            val splits: Array<String?> =
                docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var type: String? = null
            var id: String? = null
            if (splits.size == 2) {
                type = splits[0]
                id = splits[1]
            }
            when (uri.getAuthority()) {
                "com.android.externalstorage.documents" -> if ("primary" == type) {
                    path =
                        Environment.getExternalStorageDirectory().toString() + File.separator + id
                }

                "com.android.providers.downloads.documents" -> if ("raw" == type) {
                    path = id
                } else {
                    val contentUri: Uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        docId.toLong()
                    )
                    path = getMediaPathFromUri(context, contentUri, null, null)
                }

                "com.android.providers.media.documents" -> {
                    var externalUri: Uri? = null
                    when (type) {
                        "image" -> externalUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> externalUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> externalUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                    }
                    if (externalUri != null) {
                        val selection = "_id=?"
                        val selectionArgs = arrayOf<String?>(id)
                        path = getMediaPathFromUri(context, externalUri, selection, selectionArgs)
                    }
                }
            }
        } else if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme(), ignoreCase = true)) {
            path = getMediaPathFromUri(context, uri, null, null)
        } else if (ContentResolver.SCHEME_FILE.equals(uri.getScheme(), ignoreCase = true)) {
            //如果是file類型的Uri(uri.fromFile)，直接獲取圖片路徑即可
            path = uri.getPath()
        }
        //確保如果返回路徑，則路徑合法
        return if (path == null) null else (if (File(path).exists()) path else null)
    }

    private fun getMediaPathFromUri(
        context: Context,
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String?>?
    ): String? {
        var path: String?
        val authroity = uri.getAuthority()
        path = uri.getPath()
        val sdPah = Environment.getExternalStorageDirectory().getAbsolutePath()
        if (authroity == context.getPackageName() + ".myfileprovider") {
            if (!path!!.startsWith(sdPah)) {
                val sepIndex = path.indexOf(File.separator, 1)
                if (sepIndex == -1) path = null
                else {
                    path = sdPah + path.substring(sepIndex)
                }
            }
            return path
        } else {
            if (path == null || !File(path).exists()) {
                val resolver: ContentResolver = context.getContentResolver()
                val projection: Array<String?> = arrayOf(MediaStore.MediaColumns.DATA)
                val cursor: Cursor? =
                    resolver.query(uri, projection, selection, selectionArgs, null)
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        try {
                            val index = cursor.getColumnIndexOrThrow(projection[0])
                            if (index != -1) path = cursor.getString(index)
                            Log.i("TAG", "getMediaPathFromUri query " + path)
                        } catch (e: IllegalArgumentException) {
                            e.printStackTrace()
                            path = null
                        } finally {
                            cursor.close()
                        }
                    }
                }
            }
            return path
        }
    }

    fun getAudioUri(context: Context, suffix: String?, dirName: String?, Folder: String): Uri? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "title_1")
            // 設定檔案名稱
            values.put(
                MediaStore.Video.Media.DISPLAY_NAME,
                "sample_" + System.currentTimeMillis() + "." + suffix
            )
            // 設定檔案名稱
            values.put(MediaStore.Video.Media.MIME_TYPE, "video/" + suffix)
            // 設定檔案類型（Android 10+ 推薦）
            values.put(MediaStore.Video.Media.IS_PENDING, 1)
            values.put(MediaStore.Video.Media.RELATIVE_PATH, dirName + "/" + Folder)
            val contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            val uri = context.getContentResolver().insert(contentUri, values)

            return uri
        } else {
            val mediaStorageDir = getFolder(Folder)
            val filename = mediaStorageDir.getPath() + File.separator +
                    "sample_" + System.currentTimeMillis() + "." + suffix //路徑
            //        File cameraFile = new File(filename);
//        Log.d("cameraFilepath", cameraFile.getAbsolutePath());
//        return cameraFile.getAbsolutePath();
            return FileMethod.getFile_uri(context, filename)
        }
    } //    public static File UriToFile(Context context,Uri uri ) {
    //        try {
    //            InputStream inputStream = context.getContentResolver().openInputStream(uri);//InputStream只能被讀取一次
    //            inputStream
    //        } catch (FileNotFoundException e) {
    //            throw new RuntimeException(e);
    //        }
    //
    //
    //    }
}

