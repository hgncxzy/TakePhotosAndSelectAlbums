package com.xzy.example.takephotosandselectalbums

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.core.os.EnvironmentCompat
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CameraHelper {

    const val TAKE_PICTURE = 1001
    const val OPEN_ALBUM = 1002
    private const val PROVIDER_AUTHORITY = "com.xzy.example.takephotosandselectalbums.fileProvider"
    private const val FORMAT_PATTERN = "yyyyMMdd_HH_mm_ss"

    // 用于保存拍照图片的uri
    var mCameraUri: Uri? = null

    // 用于保存图片的文件路径，Android 10以下使用图片路径访问图片
    var mCameraImagePath: String? = null

    // 是否是Android 10以上手机
    val isAndroidQ = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    /**
     * 调起相机拍照
     */
    fun openCamera(activity: Activity) {
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // 判断是否有相机
        if (captureIntent.resolveActivity(activity.packageManager) != null) {
            var photoFile: File? = null
            var photoUri: Uri? = null
            if (isAndroidQ) {
                // 适配android 10
                photoUri = createImageUri(activity)
            } else {
                try {
                    photoFile = createImageFile(activity)
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                if (photoFile != null) {
                    mCameraImagePath = photoFile.absolutePath
                    photoUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        // 适配Android 7.0文件权限，通过 FileProvider 创建一个 content 类型的 Uri
                        FileProvider.getUriForFile(
                            activity,
                            PROVIDER_AUTHORITY,
                            photoFile
                        )
                    } else {
                        Uri.fromFile(photoFile)
                    }
                }
            }
            mCameraUri = photoUri
            if (photoUri != null) {
                if (PermissionHelper.checkCameraPermission(activity)) {
                    captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    captureIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    activity.startActivityForResult(captureIntent, TAKE_PICTURE)
                } else {
                    PermissionHelper.requestCameraPermission(activity)
                }
            }
        }
    }


    /**
     * 生成 bitmap,处理压缩，避免 OOM
     * */
    fun createBitmap(context: Context): Bitmap? {
        val options: BitmapFactory.Options = BitmapFactory.Options()
        var byteArrayOutputStream: ByteArrayOutputStream? = null
        var inputStream: InputStream? = null
        // 读取图片，此方法只有读取功能，没有缓存
        options.inJustDecodeBounds = true
        // 缩放的倍率
        options.inSampleSize = 1
        options.inPreferredConfig = Bitmap.Config.RGB_565
        options.inJustDecodeBounds = false
        val bitmap = if (isAndroidQ) {
            // Android 10 使用图片uri 加载
            inputStream = context.contentResolver.openInputStream(mCameraUri!!)
            BitmapFactory.decodeStream(inputStream, null, options)
        } else {
            // 使用图片路径加载
            BitmapFactory.decodeFile(mCameraImagePath, options)
        }
        byteArrayOutputStream = ByteArrayOutputStream()
        bitmap?.compress(Bitmap.CompressFormat.JPEG, 40, byteArrayOutputStream)
        try {
            inputStream?.close()
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            try {
                byteArrayOutputStream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return bitmap
    }

    /**
     * 创建图片地址uri,用于保存拍照后的照片 Android 10以后使用这种方法
     */
    private fun createImageUri(activity: Activity): Uri? {
        val status: String = Environment.getExternalStorageState()
        // 判断是否有 SD 卡,优先使用 SD 卡存储,当没有 SD 卡时使用手机存储
        return if (status == Environment.MEDIA_MOUNTED) {
            activity.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                ContentValues()
            )
        } else {
            activity.contentResolver.insert(
                MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                ContentValues()
            )
        }
    }

    /**
     * 创建保存图片的文件
     */
    @Throws(IOException::class)
    private fun createImageFile(activity: Activity): File? {
        val imageName: String =
            SimpleDateFormat(FORMAT_PATTERN, Locale.getDefault()).format(Date())
        val storageDir: File? = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (!storageDir?.exists()!!) {
            storageDir.mkdir()
        }
        val tempFile = File(storageDir, imageName)
        return if (Environment.MEDIA_MOUNTED != EnvironmentCompat.getStorageState(tempFile)) {
            null
        } else tempFile
    }

    fun hasSdcard(): Boolean {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
    }

    fun openAlbum(activity: Activity) {
        if (PermissionHelper.checkStoragePermission(activity)) {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            activity.startActivityForResult(intent, OPEN_ALBUM)
        } else {
            PermissionHelper.requestStoragePermission(activity)
        }
    }
}
