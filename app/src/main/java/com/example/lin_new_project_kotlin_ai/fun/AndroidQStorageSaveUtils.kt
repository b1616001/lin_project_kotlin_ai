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
import android.provider.OpenableColumns
import android.util.Log
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.charset.StandardCharsets
import kotlin.math.max

data class SavedFileMetadata(
    val uri: Uri,
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val relativePath: String
)

object AndroidQStorageSaveUtils {
    private const val TAG = "AndroidQStorage"

    fun getFileDescriptor(context: Context, uri: Uri, mode: String): FileDescriptor? {
        return try {
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, mode)
            parcelFileDescriptor?.fileDescriptor
        } catch (e: Exception) {
            Log.e(TAG, "getFileDescriptor failed: ${e.message}", e)
            null
        }
    }

    @JvmStatic
    fun fileUriIsExists(context: Context?, uri: Uri?): Boolean {
        if (context == null || uri == null) return false
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                pfd.fileDescriptor.valid()
            } ?: false
        } catch (_: Exception) {
            false
        }
    }

    @JvmStatic
    fun customizedFilePath(context: Context, dirName: String?, Folder: String?): Uri? {
        val safeDir = dirName ?: Environment.DIRECTORY_DCIM
        val safeFolder = Folder ?: "Lin"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, "photo_${System.currentTimeMillis()}")
                put(MediaStore.Images.Media.DISPLAY_NAME, "sample_${System.currentTimeMillis()}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "$safeDir/$safeFolder")
            }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        } else {
            val mediaStorageDir = getFolder("$safeDir/$safeFolder")
            val filename = "${mediaStorageDir.path}${File.separator}sample_${System.currentTimeMillis()}.png"
            FileMethod.getFile_uri(context, filename)
        }
    }

    @JvmStatic
    fun saveNotebookText(
        context: Context,
        information: String,
        dirName: String,
        Folder: String?
    ): Uri? {
        val safeFolder = Folder ?: "Lin"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentUri = if (dirName == Environment.DIRECTORY_DOWNLOADS) {
                MediaStore.Downloads.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            }
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.TITLE, "note_${System.currentTimeMillis()}")
                put(MediaStore.MediaColumns.DISPLAY_NAME, "note_${System.currentTimeMillis()}.txt")
                put(MediaStore.MediaColumns.MIME_TYPE, "text/plain")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "$dirName/$safeFolder")
            }
            val uri = context.contentResolver.insert(contentUri, values) ?: return null
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(information.toByteArray(StandardCharsets.UTF_8))
                    out.flush()
                }
            } catch (e: Exception) {
                Log.e(TAG, "saveNotebookText failed: ${e.message}", e)
            }
            return uri
        } else {
            val mediaStorageDir = getFolder("$dirName/$safeFolder")
            val filename = "${mediaStorageDir.path}${File.separator}note_${System.currentTimeMillis()}.txt"
            try {
                FileOutputStream(filename).use { fOut ->
                    fOut.write(information.toByteArray(StandardCharsets.UTF_8))
                    fOut.flush()
                }
            } catch (e: Exception) {
                Log.e(TAG, "saveNotebookText legacy failed: ${e.message}", e)
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
        val ext = suffix?.takeIf { it.isNotBlank() } ?: "txt"
        val safeFolder = Folder ?: "Lin"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentUri = if (dirName == Environment.DIRECTORY_DOWNLOADS) {
                MediaStore.Downloads.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL)
            }
            val mimeType = if (ext.equals("txt", ignoreCase = true)) "text/plain" else "application/octet-stream"
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.TITLE, "sample_${System.currentTimeMillis()}")
                put(MediaStore.MediaColumns.DISPLAY_NAME, "sample_${System.currentTimeMillis()}.$ext")
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, "$dirName/$safeFolder")
            }
            val uri = context.contentResolver.insert(contentUri, values) ?: return null
            try {
                stream.use { input ->
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        val buffer = ByteArray(4096)
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                        }
                        output.flush()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "saveNotebookInputStream failed: ${e.message}", e)
            }
            return uri
        } else {
            return saveArchives_old(context, "$dirName/$safeFolder", ext, stream)
        }
    }

    private fun saveArchives_old(
        context: Context?,
        Folder: String,
        suffix: String?,
        stream: InputStream
    ): Uri {
        val mediaStorageDir = getFolder(Folder)
        val filename = "${mediaStorageDir.path}${File.separator}sample_${System.currentTimeMillis()}.$suffix"
        try {
            stream.use { input ->
                FileOutputStream(filename).use { fOut ->
                    val buffer = ByteArray(4096)
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        fOut.write(buffer, 0, read)
                    }
                    fOut.flush()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "saveArchives_old failed: ${e.message}", e)
        }
        return FileMethod.getFile_uri(context, filename)
    }

    @JvmStatic
    fun saveBitmap(context: Context, bitmap: Bitmap, dirName: String?, Folder: String?): Uri? {
        val safeDir = dirName ?: Environment.DIRECTORY_DCIM
        val safeFolder = Folder ?: "Lin"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.TITLE, "image_${System.currentTimeMillis()}")
                put(MediaStore.Images.Media.DISPLAY_NAME, "sample_${System.currentTimeMillis()}.png")
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, "$safeDir/$safeFolder")
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                ?: return null
            try {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                }
            } catch (e: Exception) {
                Log.e(TAG, "saveBitmap failed: ${e.message}", e)
            }
            return uri
        } else {
            val mediaStorageDir = getFolder("$safeDir/$safeFolder")
            val filename = "${mediaStorageDir.path}${File.separator}sample_${System.currentTimeMillis()}.png"
            try {
                FileOutputStream(filename).use { fOut ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
                    fOut.flush()
                }
            } catch (e: Exception) {
                Log.e(TAG, "saveBitmap legacy failed: ${e.message}", e)
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
        val ext = suffix?.takeIf { it.isNotBlank() } ?: "mp4"
        val safeDir = dirName ?: Environment.DIRECTORY_MOVIES
        val safeFolder = Folder ?: "Lin"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.TITLE, "video_${System.currentTimeMillis()}")
                put(MediaStore.Video.Media.DISPLAY_NAME, "sample_${System.currentTimeMillis()}.$ext")
                put(MediaStore.Video.Media.MIME_TYPE, "video/$ext")
                put(MediaStore.Video.Media.RELATIVE_PATH, "$safeDir/$safeFolder")
            }
            val uri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
                ?: return null
            try {
                stream.use { input ->
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        val buffer = ByteArray(4096)
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                        }
                        output.flush()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "saveVideo failed: ${e.message}", e)
            }
            return uri
        } else {
            return saveArchives_old(context, "$safeDir/$safeFolder", ext, stream)
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
        val ext = suffix?.takeIf { it.isNotBlank() } ?: "mp3"
        val safeDir = dirName ?: Environment.DIRECTORY_MUSIC
        val safeFolder = Folder ?: "Lin"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val mimeType = if (ext.equals("mp3", ignoreCase = true)) "audio/mpeg" else "audio/$ext"
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.TITLE, "audio_${System.currentTimeMillis()}")
                put(MediaStore.Audio.Media.DISPLAY_NAME, "sample_${System.currentTimeMillis()}.$ext")
                put(MediaStore.Audio.Media.MIME_TYPE, mimeType)
                put(MediaStore.Audio.Media.RELATIVE_PATH, "$safeDir/$safeFolder")
            }
            val uri = context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
                ?: return null
            try {
                stream.use { input ->
                    context.contentResolver.openOutputStream(uri)?.use { output ->
                        val buffer = ByteArray(4096)
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            output.write(buffer, 0, read)
                        }
                        output.flush()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "saveAudio failed: ${e.message}", e)
            }
            return uri
        } else {
            return saveArchives_old(context, "$safeDir/$safeFolder", ext, stream)
        }
    }

    fun openSampleVideoStream(context: Context): InputStream {
        return try {
            context.resources.assets.open("test.mp4")
        } catch (_: Exception) {
            ByteArrayInputStream(createMinimalSampleMp4Bytes())
        }
    }

    fun openSampleAudioStream(context: Context): InputStream {
        return try {
            context.resources.assets.open("六月的雨.mp3")
        } catch (_: Exception) {
            context.resources.assets.open("test.mp3")
        }
    }

    /**
     * Generates a valid minimal ISO-BMFF MP4 container header (ftyp + free + mdat)
     * as a fallback when no bundled test.mp4 asset exists.
     */
    fun createMinimalSampleMp4Bytes(): ByteArray {
        return byteArrayOf(
            // ftyp box (24 bytes): isom / iso2 / mp41
            0x00, 0x00, 0x00, 0x18, 0x66, 0x74, 0x79, 0x70,
            0x69, 0x73, 0x6F, 0x6D, 0x00, 0x00, 0x02, 0x00,
            0x69, 0x73, 0x6F, 0x6D, 0x69, 0x73, 0x6F, 0x32,
            // free box (8 bytes)
            0x00, 0x00, 0x00, 0x08, 0x66, 0x72, 0x65, 0x65,
            // mdat box (16 bytes)
            0x00, 0x00, 0x00, 0x10, 0x6D, 0x64, 0x61, 0x74,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00
        )
    }

    fun loadBitmapFromUri(context: Context, uri: Uri, maxDimPx: Int = 1440): Bitmap? {
        return try {
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOpts)
            }

            var inSampleSize = 1
            val longest = max(boundsOpts.outWidth, boundsOpts.outHeight)
            if (longest > maxDimPx) {
                while ((longest / inSampleSize) > maxDimPx) {
                    inSampleSize *= 2
                }
            }

            val decodeOpts = BitmapFactory.Options().apply { this.inSampleSize = inSampleSize }
            val decoded = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOpts)
            } ?: return null

            val degree = readExifRotationDegree(context, uri)
            if (degree == 0) return decoded

            val matrix = Matrix().apply { postRotate(degree.toFloat()) }
            val rotated = Bitmap.createBitmap(
                decoded, 0, 0, decoded.width, decoded.height, matrix, true
            )
            if (rotated != decoded) {
                decoded.recycle()
            }
            rotated
        } catch (e: Exception) {
            Log.e(TAG, "loadBitmapFromUri failed: ${e.message}", e)
            null
        }
    }

    private fun readExifRotationDegree(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (_: Exception) {
            0
        }
    }

    fun queryFileMetadata(context: Context, uri: Uri): SavedFileMetadata {
        val resolver = context.contentResolver
        var displayName = uri.lastPathSegment ?: "unknown"
        var sizeBytes = -1L
        var relativePath = ""

        val projection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayOf(
                OpenableColumns.DISPLAY_NAME,
                OpenableColumns.SIZE,
                MediaStore.MediaColumns.RELATIVE_PATH
            )
        } else {
            arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        }

        try {
            resolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx != -1) {
                        cursor.getString(nameIdx)?.let { displayName = it }
                    }
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIdx != -1 && !cursor.isNull(sizeIdx)) {
                        sizeBytes = cursor.getLong(sizeIdx)
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val relIdx = cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)
                        if (relIdx != -1) {
                            relativePath = cursor.getString(relIdx).orEmpty()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "queryFileMetadata partial fallback: ${e.message}")
        }

        val mimeType = resolver.getType(uri) ?: "application/octet-stream"
        return SavedFileMetadata(
            uri = uri,
            displayName = displayName,
            mimeType = mimeType,
            sizeBytes = sizeBytes,
            relativePath = relativePath.ifBlank { uri.authority ?: "ContentResolver" }
        )
    }

    fun deleteUri(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.delete(uri, null, null) > 0
        } catch (e: Exception) {
            Log.e(TAG, "deleteUri failed: ${e.message}", e)
            false
        }
    }

    @JvmStatic
    fun Uri_to_path(context: Context, uri: Uri): String? {
        var path: String? = ""
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                context.contentResolver.query(
                    uri,
                    arrayOf(
                        MediaStore.Images.Media.DATA,
                        MediaStore.Images.Media.DISPLAY_NAME,
                        MediaStore.Images.Media.DATE_ADDED,
                        MediaStore.Images.Media._ID
                    ),
                    null,
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToNext()) {
                        path = cursor.getString(0)
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Uri_to_path failed: ${e.message}")
            }
            return path
        } else {
            return getPathFromUri(context, uri)
        }
    }

    @JvmStatic
    fun doPickPhotoFromGallery(context: Context): Intent? {
        try {
            val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            val packageManager: PackageManager = context.packageManager
            val intent = Intent(Intent.ACTION_PICK, uri)
            val list: MutableList<ResolveInfo?> =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            if (list.isNotEmpty()) {
                val picker = Intent(Intent.ACTION_PICK, uri)
                return Intent.createChooser(picker, "選取圖片")
            }
        } catch (e: ActivityNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    fun getImageContentUri(context: Context, path: String): Uri? {
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Images.Media._ID),
            "${MediaStore.Images.Media.DATA}=? ",
            arrayOf(path),
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                @SuppressLint("Range")
                val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
                val baseUri = Uri.parse("content://media/external/images/media")
                return Uri.withAppendedPath(baseUri, id.toString())
            }
        }
        return if (File(path).exists()) {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DATA, path)
            }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        } else {
            null
        }
    }

    @JvmStatic
    fun getBitmap(context: Context, path: String): Bitmap? {
        return try {
            val uu = if (path.contains("http")) Uri.parse(path) else FileMethod.getFile_uri(context, path)
            loadBitmapFromUri(context, uu)
        } catch (e: Exception) {
            Log.d("錯誤", e.toString())
            null
        }
    }

    fun deleteFoder(file: File): Boolean {
        try {
            if (file.exists()) {
                if (file.isFile) {
                    return file.delete()
                } else if (file.isDirectory) {
                    val files = file.listFiles()
                    if (files != null) {
                        for (value in files) {
                            deleteFoder(value)
                        }
                    }
                    return file.delete()
                }
            }
        } catch (e: Exception) {
            Log.d("刪除資料", e.toString())
        }
        return false
    }

    @JvmStatic
    fun getReadTxt(context: Context?, uri: Uri?): String {
        if (context == null || uri == null) return ""
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader(StandardCharsets.UTF_8).readText()
            }.orEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "getReadTxt failed: ${e.message}", e)
            ""
        }
    }

    private fun getFolder(Folder: String): File {
        @Suppress("DEPRECATION")
        val mediaStorageDir = File(Environment.getExternalStorageDirectory(), Folder)
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs()
        }
        return mediaStorageDir
    }

    fun getPathFromUri(context: Context, uri: Uri): String? {
        var path: String? = null
        if (DocumentsContract.isDocumentUri(context, uri)) {
            val docId: String = DocumentsContract.getDocumentId(uri)
            val splits = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            var type: String? = null
            var id: String? = null
            if (splits.size == 2) {
                type = splits[0]
                id = splits[1]
            }
            when (uri.authority) {
                "com.android.externalstorage.documents" -> if ("primary" == type) {
                    @Suppress("DEPRECATION")
                    path = "${Environment.getExternalStorageDirectory()}${File.separator}$id"
                }

                "com.android.providers.downloads.documents" -> if ("raw" == type) {
                    path = id
                } else {
                    val contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"),
                        docId.toLong()
                    )
                    path = getMediaPathFromUri(context, contentUri, null, null)
                }

                "com.android.providers.media.documents" -> {
                    val externalUri: Uri? = when (type) {
                        "image" -> MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        "video" -> MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                        "audio" -> MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                        else -> null
                    }
                    if (externalUri != null) {
                        path = getMediaPathFromUri(context, externalUri, "_id=?", arrayOf(id))
                    }
                }
            }
        } else if (ContentResolver.SCHEME_CONTENT.equals(uri.scheme, ignoreCase = true)) {
            path = getMediaPathFromUri(context, uri, null, null)
        } else if (ContentResolver.SCHEME_FILE.equals(uri.scheme, ignoreCase = true)) {
            path = uri.path
        }
        return if (path == null) null else (if (File(path).exists()) path else null)
    }

    private fun getMediaPathFromUri(
        context: Context,
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String?>?
    ): String? {
        var path = uri.path
        val authority = uri.authority
        @Suppress("DEPRECATION")
        val sdPath = Environment.getExternalStorageDirectory().absolutePath
        return if (authority == "${context.packageName}.myfileprovider") {
            if (path != null && !path.startsWith(sdPath)) {
                val sepIndex = path.indexOf(File.separator, 1)
                path = if (sepIndex == -1) null else sdPath + path.substring(sepIndex)
            }
            path
        } else {
            if (path == null || !File(path).exists()) {
                val resolver = context.contentResolver
                val projection = arrayOf<String?>(MediaStore.MediaColumns.DATA)
                resolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        try {
                            val index = cursor.getColumnIndexOrThrow(projection[0])
                            if (index != -1) path = cursor.getString(index)
                        } catch (_: IllegalArgumentException) {
                            path = null
                        }
                    }
                }
            }
            path
        }
    }

    fun getAudioUri(context: Context, suffix: String?, dirName: String?, Folder: String): Uri? {
        val ext = suffix ?: "mp4"
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues().apply {
                put(MediaStore.Video.Media.TITLE, "title_1")
                put(MediaStore.Video.Media.DISPLAY_NAME, "sample_${System.currentTimeMillis()}.$ext")
                put(MediaStore.Video.Media.MIME_TYPE, "video/$ext")
                put(MediaStore.Video.Media.IS_PENDING, 1)
                put(MediaStore.Video.Media.RELATIVE_PATH, "$dirName/$Folder")
            }
            context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        } else {
            val mediaStorageDir = getFolder(Folder)
            val filename = "${mediaStorageDir.path}${File.separator}sample_${System.currentTimeMillis()}.$ext"
            FileMethod.getFile_uri(context, filename)
        }
    }
}
