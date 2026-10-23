package com.example.lin_new_project_kotlin_ai.`fun`

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.ExifInterface
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date

class ImageMethod {
    fun getBitmap(context: Context, filename: String): Bitmap? {
        var imagbitmap: Bitmap? = null
        val cameraFile = File(filename)
        Log.d("cameraFilepath", cameraFile.getAbsolutePath())

        val imageUri = Uri.fromFile(cameraFile)
        try {
            Log.d("cameraFilepath", filename)
            imagbitmap = BitmapFactory.decodeStream(
                context.getContentResolver().openInputStream(imageUri)
            )
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return imagbitmap
    }

    fun getBitmap_Size(context: Context, url: String, size: Int): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            bitmap = Companion.reSize(getBitmap(context, url)!!, size)
            var degree = 0
            val exifInterface = ExifInterface(url)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
            val matrix = Matrix()
            matrix.reset()
            matrix.setRotate(degree.toFloat())
            bitmap = Bitmap.createBitmap(
                bitmap!!,
                0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                true
            )
        } catch (e: Exception) {
        }

        return bitmap
    }

    companion object {
        fun setImageSize(image: ImageView) {
            image.post(object : Runnable {
                override fun run() {
                    try {
                        val bd = image.getBackground() as BitmapDrawable
                        val bm = bd.getBitmap()
                        Log.d("ImageView大小", bm.getWidth().toString() + "," + bm.getHeight())
                        Log.d(
                            "ImageView大小",
                            image.getWidth().toString() + "," + image.getHeight()
                        )

                        val Height = image.getWidth() * bm.getHeight() / bm.getWidth()
                        val layoutParams = LinearLayout.LayoutParams(image.getLayoutParams())
                        layoutParams.width = image.getWidth()
                        layoutParams.height = Height
                        image.setLayoutParams(layoutParams)
                        //                    image.setMinimumHeight(Height);
//                    image.setMaxHeight(Height);
                        Log.d("ImageView大小", Height.toString() + "")
                    } catch (e: Exception) {
                    }
                }
            })
        }

        fun getBitmapDrawable(resources: Resources?, icon: Drawable?): BitmapDrawable {
            //縮放圖片
            val bd = icon as BitmapDrawable
            var bitmap = bd.getBitmap()
            val matrix = Matrix()

            // 縮放尺吋
            bitmap = Bitmap.createBitmap(
                bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(),
                matrix, true
            )
            val dd = BitmapDrawable(resources, bitmap)
            return dd
        }

        @JvmStatic
        fun getBitmap(icon: Drawable?): Bitmap {
            //縮放圖片
            val bd = icon as BitmapDrawable
            val bitmap = bd.getBitmap()

            //        Matrix matrix = new Matrix();
//        matrix.reset();
//        // 縮放尺吋
//        bitmap = Bitmap.createBitmap(bitmap, 0, 0,
//                bitmap.getWidth(), bitmap.getHeight(),
//                matrix, true);
            return bitmap
        }

        fun base64ToBitmap(base64Data: String?): Bitmap? {
            val bytes = Base64.decode(base64Data, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }

        @JvmStatic
        fun getDrawableByVERSION(context: Context?, id: Int): Drawable? {
            //會根據Android 版本來抓取圖片-----------------------
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return context!!.getResources().getDrawable(id, context?.getTheme())
            } else {
                return context!!.getResources().getDrawable(id)
            }
        }

        fun getBitmapm(icon: Drawable?): Bitmap {
            //縮放圖片
            val bd = icon as BitmapDrawable
            var bitmap = bd.getBitmap()
            val matrix = Matrix()
            // 縮放尺吋
            bitmap = Bitmap.createBitmap(
                bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(),
                matrix, true
            )

            return bitmap
        }

        fun bitmapToBase64(bitmap: Bitmap?): String? {
            if (bitmap == null) {
                return ""
            }
            var result: String? = null
            var baos: ByteArrayOutputStream? = null
            val matrix = Matrix()
            val bitmap2 = Bitmap.createBitmap(
                bitmap,
                0,
                0,
                bitmap.getWidth(),
                bitmap.getHeight(),
                matrix,
                true
            )
            try {
                if (bitmap2 != null) {
                    baos = ByteArrayOutputStream()
                    bitmap2.compress(Bitmap.CompressFormat.JPEG, 20, baos)

                    baos.flush()
                    baos.close()

                    val bitmapBytes = baos.toByteArray()
                    result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT)
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    if (baos != null) {
                        baos.flush()
                        baos.close()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
            return result
        }


        fun enlarge(bit: Bitmap, MaxPx: Int, bb: Boolean): Bitmap {
            val width = bit.getWidth()
            val height = bit.getHeight()
            Log.d("圖片大小", width.toString() + "," + height + "," + MaxPx)
            // 设置想要的大小
            var newWidth = 0
            var newHeight = 0
            var ratio = 1f // 縮放比例
            if (bb) {
                ratio = MaxPx.toFloat() / width.toFloat()
                newWidth = MaxPx
                newHeight = (ratio * height).toInt()
                Log.d("圖片大小", "寬" + newWidth + "," + newHeight + "," + ratio)
            } else {
                ratio = MaxPx.toFloat() / height.toFloat()
                newHeight = MaxPx
                newWidth = (ratio * width).toInt()
                Log.d("圖片大小", "高" + newWidth + "," + newHeight + "," + ratio)
            }

            // 计算缩放比例
            val scaleWidth = (newWidth.toFloat()) / width
            val scaleHeight = (newHeight.toFloat()) / height
            // 取得想要缩放的matrix参数
            val matrix = Matrix()
            matrix.reset()
            matrix.postScale(scaleWidth, scaleHeight)
            // 得到新的图片
            val newbm = Bitmap.createBitmap(
                bit, 0, 0, width, height, matrix,
                true
            )
            //
//        Bitmap marker = ThumbnailUtils.extractThumbnail(bit, newWidth, newHeight);
//        Matrix matrix2 = new Matrix();
//        matrix2.reset();
//        Bitmap newbm2 = Bitmap.createBitmap(newbm, 0, 0, newbm.getWidth(), newbm.getHeight(), matrix2,
//                true);
//        Log.d("圖片大小","newbm"+newbm.getWidth()+","+newbm.getHeight());
//        Log.d("圖片大小","marker"+marker.getWidth()+","+marker.getHeight());
//        Log.d("圖片大小","newbm2"+newbm2.getWidth()+","+newbm2.getHeight());
            return newbm
        }

        fun reSize(bit: Bitmap, MaxPx: Int): Bitmap? {
            val width = bit.getWidth()
            val height = bit.getHeight()
            if (width < MaxPx && height < MaxPx) {
                return bit
                // 傳進來的圖比要的小，不處理
            }
            var ratio = 1f // 縮放比例
            if (width > height && width > MaxPx) {
                //這張圖比較寬，依寬度進行等比例縮放
                ratio = MaxPx.toFloat() / width.toFloat()
            } else if (height > MaxPx) {
                //這張圖比較高，依高度進行等比例縮放
                ratio = MaxPx.toFloat() / height.toFloat()
            }
            if (ratio >= 1) {
                //如果比例不需要縮小，返回原圖，如果你有放大需求，可以修改這邊
                return bit
            } else {
                var newW = (width * ratio).toInt()
                var newH = (height * ratio).toInt()
                if (newW <= 0 || newH <= 0) {
                    if (width > height && width > MaxPx) {
                        ratio = MaxPx.toFloat() / width.toFloat()
                    } else if (height > MaxPx) {
                        ratio = MaxPx.toFloat() / height.toFloat()
                    }
                    newW = (width * ratio).toInt()
                    newH = (height * ratio).toInt()
                }

                val marker = ThumbnailUtils.extractThumbnail(bit, newW, newH)

                return marker
            }
        }

        fun saveBitmap(bitmap: Bitmap): String {
//        File mediaStorageDir = new File(Environment
//                .getExternalStorageDirectory().getAbsolutePath(),
            var filename = ""
            val mediaStorageDir = File(
                Environment
                    .getExternalStorageDirectory(),
                "測試"
            )
            if (!mediaStorageDir.exists()) {
                mediaStorageDir.mkdirs()
            }
            Log.d("dirpath", mediaStorageDir.getAbsolutePath())
            // filename = mediaStorageDir.getPath() + File.separator
            // + "OP_GET_" + getRequest_id() + ".jpg";
            val sf = SimpleDateFormat("yyyyMMddhhmmss")
            filename = (mediaStorageDir.getPath() + File.separator
                    + sf.format(Date()) + ".jpg") //路徑

            val fOut: FileOutputStream?
            try {
                fOut = FileOutputStream(filename)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)

                try {
                    fOut.flush()
                    fOut.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
            return filename
        }

        fun decodeSampledBitmapFromResource(res: Resources?, resId: Int): Bitmap? {
            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            //加载图片
            BitmapFactory.decodeResource(res, resId, options)
            //计算缩放比
            options.inSampleSize = 1
            //重新加载图片
            options.inJustDecodeBounds = false
            return BitmapFactory.decodeResource(res, resId, options)
        }
    }
}
