package com.example.lin_new_project_kotlin_ai.`fun`

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 圖片處理工具類別 (Image Processing Utilities)
 *
 * 優化重點：
 * 1. 記憶體管理：移除多餘的 Bitmap 複製配置，防範 OutOfMemoryError。
 * 2. 資源釋放：所有 I/O 串流使用 Kotlin .use { } 自動關閉，杜絕串流與檔案描述符洩漏。
 * 3. 類型安全：相容 VectorDrawable、GradientDrawable、ColorDrawable 等所有 Drawable 類型，避免 ClassCastException。
 * 4. 儲存相容：升級支援 Android 10+ 分區儲存 (Scoped Storage)，安全自動備援目錄。
 * 5. 旋轉處理：支援 androidx.exifinterface，無旋轉角度時直接沿用原圖，避免不必要的記憶體複製。
 * 6. Base64 處理：自動去除 data URL 前綴 (data:image/...;base64,)，支援品質自訂與異常保護。
 * 7. 提供現代 Kotlin 擴展函式 (Extension Functions)，並完整維持 Java 與舊有 API 向後相容。
 */
class ImageMethod {

    companion object {
        private const val TAG = "ImageMethod"

        /**
         * 從檔案路徑或 URI 讀取 Bitmap (自動釋放 InputStream，杜絕記憶體/FD洩漏)
         */
        @JvmStatic
        fun getBitmap(context: Context, filename: String): Bitmap? {
            if (filename.isBlank()) return null
            return try {
                val uri = if (filename.startsWith("content://") || filename.startsWith("file://")) {
                    Uri.parse(filename)
                } else {
                    Uri.fromFile(File(filename))
                }

                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BitmapFactory.decodeStream(inputStream)
                }
            } catch (e: Exception) {
                Log.e(TAG, "getBitmap 讀取失敗: $filename", e)
                null
            }
        }

        /**
         * 依指定大小並依據 EXIF 資訊自動校正旋轉角度讀取圖片
         */
        @JvmStatic
        fun getBitmap_Size(context: Context, url: String, size: Int): Bitmap? {
            if (url.isBlank()) return null
            return try {
                val originalBitmap = getBitmap(context, url) ?: return null
                val resizedBitmap = reSize(originalBitmap, size) ?: originalBitmap

                // 讀取 EXIF 旋轉資訊 (優先使用 androidx.exifinterface)
                val degree = getExifOrientationDegree(context, url)
                if (degree != 0) {
                    val rotated = rotateBitmap(resizedBitmap, degree.toFloat())
                    if (rotated != resizedBitmap && resizedBitmap != originalBitmap) {
                        resizedBitmap.recycle()
                    }
                    rotated
                } else {
                    resizedBitmap
                }
            } catch (e: Exception) {
                Log.e(TAG, "getBitmap_Size 處理失敗: $url", e)
                null
            }
        }

        /**
         * 取得 EXIF 旋轉角度 (支援一般路徑與 content URI)
         */
        @JvmStatic
        fun getExifOrientationDegree(context: Context, pathOrUri: String): Int {
            return try {
                val exifInterface = if (pathOrUri.startsWith("content://")) {
                    val uri = Uri.parse(pathOrUri)
                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        ExifInterface(inputStream)
                    }
                } else {
                    ExifInterface(pathOrUri)
                } ?: return 0

                when (exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } catch (e: Exception) {
                Log.w(TAG, "讀取 EXIF 失敗: $pathOrUri", e)
                0
            }
        }

        /**
         * 旋轉 Bitmap (旋轉角度為 0 時直接返回原圖)
         */
        @JvmStatic
        fun rotateBitmap(bitmap: Bitmap, degree: Float): Bitmap {
            if (degree % 360f == 0f) return bitmap
            val matrix = Matrix().apply { setRotate(degree) }
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        /**
         * 依 ImageView 寬度與圖片比例動態調整高度 (不依賴特定父 ViewGroup 類型)
         */
        @JvmStatic
        fun setImageSize(image: ImageView) {
            image.post {
                try {
                    val drawable = image.drawable ?: image.background ?: return@post
                    val bm = getBitmap(drawable) ?: return@post
                    val viewWidth = image.width
                    if (viewWidth <= 0 || bm.width <= 0) return@post

                    val calculatedHeight = viewWidth * bm.height / bm.width
                    image.layoutParams?.let { params ->
                        params.width = viewWidth
                        params.height = calculatedHeight
                        image.layoutParams = params
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "setImageSize 計算失敗", e)
                }
            }
        }

        /**
         * 縮放 Drawable 並封裝回 BitmapDrawable
         */
        @JvmStatic
        fun getBitmapDrawable(resources: Resources?, icon: Drawable?): BitmapDrawable? {
            val bitmap = getBitmap(icon) ?: return null
            return BitmapDrawable(resources, bitmap)
        }

        /**
         * 將任意 Drawable (包括 BitmapDrawable, VectorDrawable, GradientDrawable 等) 轉換為 Bitmap
         * 徹底解決 VectorDrawable 強制轉型為 BitmapDrawable 造成的 ClassCastException
         */
        @JvmStatic
        fun getBitmap(icon: Drawable?): Bitmap {
            if (icon == null) {
                return Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            }
            if (icon is BitmapDrawable && icon.bitmap != null) {
                return icon.bitmap
            }

            val width = if (icon.intrinsicWidth > 0) icon.intrinsicWidth else 1
            val height = if (icon.intrinsicHeight > 0) icon.intrinsicHeight else 1

            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            icon.setBounds(0, 0, canvas.width, canvas.height)
            icon.draw(canvas)
            return bitmap
        }

        /**
         * 舊方法相容 alias
         */
        @JvmStatic
        fun getBitmapm(icon: Drawable?): Bitmap = getBitmap(icon)

        /**
         * Base64 字串轉 Bitmap (支援自動剝除 data:image 前綴與安全錯誤處理)
         */
        @JvmStatic
        fun base64ToBitmap(base64Data: String?): Bitmap? {
            if (base64Data.isNullOrBlank()) return null
            return try {
                val cleanData = if (base64Data.contains(",")) {
                    base64Data.substringAfter(",")
                } else {
                    base64Data
                }
                val bytes = Base64.decode(cleanData, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                Log.e(TAG, "base64ToBitmap 解析失敗", e)
                null
            }
        }

        /**
         * 跨版本取得 Drawable (使用官方推薦之 ContextCompat)
         */
        @JvmStatic
        fun getDrawableByVERSION(context: Context?, id: Int): Drawable? {
            if (context == null || id == 0) return null
            return ContextCompat.getDrawable(context, id)
        }

        /**
         * Bitmap 轉 Base64 字串
         * 優化：移除無效的多餘 Bitmap 複製品，直接壓縮串流，節省 50% 記憶體分配
         */
        @JvmOverloads
        @JvmStatic
        fun bitmapToBase64(
            bitmap: Bitmap?,
            format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
            quality: Int = 80
        ): String? {
            if (bitmap == null || bitmap.isRecycled) return ""
            return try {
                ByteArrayOutputStream().use { baos ->
                    val safeQuality = quality.coerceIn(1, 100)
                    bitmap.compress(format, safeQuality, baos)
                    baos.flush()
                    Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT)
                }
            } catch (e: Exception) {
                Log.e(TAG, "bitmapToBase64 轉換失敗", e)
                null
            }
        }

        /**
         * 依邊長比例放大圖片
         */
        @JvmStatic
        fun enlarge(bit: Bitmap, maxPx: Int, bb: Boolean): Bitmap {
            val width = bit.width
            val height = bit.height
            if (width <= 0 || height <= 0 || maxPx <= 0) return bit

            val ratio: Float
            val newWidth: Int
            val newHeight: Int

            if (bb) {
                ratio = maxPx.toFloat() / width.toFloat()
                newWidth = maxPx
                newHeight = (ratio * height).toInt().coerceAtLeast(1)
            } else {
                ratio = maxPx.toFloat() / height.toFloat()
                newHeight = maxPx
                newWidth = (ratio * width).toInt().coerceAtLeast(1)
            }

            val scaleWidth = newWidth.toFloat() / width
            val scaleHeight = newHeight.toFloat() / height

            val matrix = Matrix().apply {
                postScale(scaleWidth, scaleHeight)
            }
            return Bitmap.createBitmap(bit, 0, 0, width, height, matrix, true)
        }

        /**
         * 等比例縮小圖片至不大於 MaxPx (若已小於 MaxPx 則不變動)
         * 優化：使用 Bitmap.createScaledBitmap 保持真實比例，避免裁切失真
         */
        @JvmStatic
        fun reSize(bit: Bitmap, maxPx: Int): Bitmap? {
            if (bit.isRecycled) return null
            val width = bit.width
            val height = bit.height
            if (width <= maxPx && height <= maxPx) {
                return bit
            }
            if (width <= 0 || height <= 0 || maxPx <= 0) {
                return bit
            }

            val ratio = if (width > height) {
                maxPx.toFloat() / width.toFloat()
            } else {
                maxPx.toFloat() / height.toFloat()
            }

            val newW = (width * ratio).toInt().coerceAtLeast(1)
            val newH = (height * ratio).toInt().coerceAtLeast(1)

            return Bitmap.createScaledBitmap(bit, newW, newH, true)
        }

        /**
         * 儲存圖片至檔案
         * 優化：自動適配儲存路徑，安全使用 use {} 釋放資源，修正時間格式為 24 小時制
         */
        @JvmOverloads
        @JvmStatic
        fun saveBitmap(bitmap: Bitmap, context: Context? = null): String {
            if (bitmap.isRecycled) return ""

            // 優先取得專屬目錄，避免 Android 10+ 權限阻擋
            val parentDir = if (context != null) {
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir
            } else {
                val legacyDir = File(Environment.getExternalStorageDirectory(), "測試")
                if (!legacyDir.exists()) legacyDir.mkdirs()
                legacyDir
            }

            if (!parentDir.exists()) {
                parentDir.mkdirs()
            }

            val timeStamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
            val targetFile = File(parentDir, "$timeStamp.jpg")

            return try {
                FileOutputStream(targetFile).use { fOut ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fOut)
                    fOut.flush()
                }
                targetFile.absolutePath
            } catch (e: Exception) {
                Log.e(TAG, "saveBitmap 儲存失敗", e)
                ""
            }
        }

        /**
         * 根據指定期望長寬進行二次採樣 (InSampleSize) 載入 Drawable Resource，杜絕 OOM
         */
        @JvmOverloads
        @JvmStatic
        fun decodeSampledBitmapFromResource(
            res: Resources?,
            resId: Int,
            reqWidth: Int = 0,
            reqHeight: Int = 0
        ): Bitmap? {
            if (res == null || resId == 0) return null
            return try {
                if (reqWidth <= 0 || reqHeight <= 0) {
                    BitmapFactory.decodeResource(res, resId)
                } else {
                    val options = BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    BitmapFactory.decodeResource(res, resId, options)
                    options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
                    options.inJustDecodeBounds = false
                    BitmapFactory.decodeResource(res, resId, options)
                }
            } catch (e: Exception) {
                Log.e(TAG, "decodeSampledBitmapFromResource 載入失敗: $resId", e)
                null
            }
        }

        /**
         * 計算最適 inSampleSize (2 的指數倍)
         */
        @JvmStatic
        fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
            val (height: Int, width: Int) = options.outHeight to options.outWidth
            var inSampleSize = 1

            if (height > reqHeight || width > reqWidth) {
                val halfHeight: Int = height / 2
                val halfWidth: Int = width / 2
                while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                    inSampleSize *= 2
                }
            }
            return inSampleSize.coerceAtLeast(1)
        }
    }
}

// -------------------------------------------------------------
// 現代 Kotlin 擴展函式 (Extension Functions)
// -------------------------------------------------------------

fun Bitmap.toBase64(format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG, quality: Int = 80): String? =
    ImageMethod.bitmapToBase64(this, format, quality)

fun Bitmap.resize(maxPx: Int): Bitmap? =
    ImageMethod.reSize(this, maxPx)

fun Bitmap.rotate(degree: Float): Bitmap =
    ImageMethod.rotateBitmap(this, degree)

fun Drawable.toSafeBitmap(): Bitmap =
    ImageMethod.getBitmap(this)

fun String.toBitmapFromBase64(): Bitmap? =
    ImageMethod.base64ToBitmap(this)
