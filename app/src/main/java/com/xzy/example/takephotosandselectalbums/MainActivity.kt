package com.xzy.example.takephotosandselectalbums

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import com.xzy.example.takephotosandselectalbums.CameraHelper.OPEN_ALBUM
import com.xzy.example.takephotosandselectalbums.CameraHelper.TAKE_PICTURE
import com.xzy.example.takephotosandselectalbums.CameraHelper.hasSdcard
import com.xzy.example.takephotosandselectalbums.CameraHelper.isAndroidQ
import com.xzy.example.takephotosandselectalbums.CameraHelper.mCameraImagePath
import com.xzy.example.takephotosandselectalbums.CameraHelper.mCameraUri
import com.xzy.example.takephotosandselectalbums.CameraHelper.openAlbum
import com.xzy.example.takephotosandselectalbums.CameraHelper.openCamera
import com.xzy.example.takephotosandselectalbums.PermissionHelper.checkCameraPermission
import com.xzy.example.takephotosandselectalbums.PermissionHelper.checkStoragePermission
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {
    private var intentType = 0
    var bitmap: Bitmap? = null
    private var imageView: ImageView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.iv_test)

        findViewById<Button>(R.id.btn_test).setOnClickListener {
            ActionSheetDialog(this)
                .builder()
                .setCancelable(true)
                .setCanceledOnTouchOutside(true)
                .addSheetItem(
                    "拍照",
                    ActionSheetDialog.SheetItemColor.BLACK,
                    object : ActionSheetDialog.OnSheetItemClickListener {
                        override fun onClick(which: Int) {
                            intentType = TAKE_PICTURE
                            requestPermission()
                        }
                    }
                )
                .addSheetItem(
                    "选择相册",
                    ActionSheetDialog.SheetItemColor.BLACK,
                    object : ActionSheetDialog.OnSheetItemClickListener {
                        override fun onClick(which: Int) {
                            intentType = OPEN_ALBUM
                            requestPermission()
                        }
                    }
                )
                .show()
        }

    }

    private fun requestPermission() {
        when (intentType) {
            TAKE_PICTURE -> {
                if (checkCameraPermission(this)) {
                    handle()
                } else {
                    PermissionHelper.requestCameraPermission(this)
                }
            }

            OPEN_ALBUM -> {
                if (checkStoragePermission(this)) {
                    handle()
                } else {
                    PermissionHelper.requestStoragePermission(this)
                }
            }
        }
    }

    private fun handle() {
        when (intentType) {
            TAKE_PICTURE -> {
                openCamera(this)
            }
            OPEN_ALBUM -> {
                openAlbum(this)
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE -> {
                // Do something after user returned from app settings screen, like showing a Toast.
                if (checkCameraPermission(this)) {
                    handle()
                } else {
                    Toast.makeText(this, "Please apply for permission first", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            TAKE_PICTURE -> {
                if (hasSdcard()) {
                    // 点击了确定
                    if (resultCode == RESULT_OK) {
                        val options: BitmapFactory.Options = BitmapFactory.Options()
                        var baos: ByteArrayOutputStream? = null
                        var inputStream: InputStream? = null
                        // 读取图片，此方法只有读取功能，没有缓存
                        options.inJustDecodeBounds = true
                        // 缩放的倍率
                        options.inSampleSize = 1
                        options.inPreferredConfig = Bitmap.Config.RGB_565
                        options.inJustDecodeBounds = false
                        bitmap = if (isAndroidQ) {
                            // Android 10 使用图片uri 加载
                            inputStream = contentResolver.openInputStream(mCameraUri!!)
                            BitmapFactory.decodeStream(inputStream, null, options)
                        } else {
                            // 使用图片路径加载
                            BitmapFactory.decodeFile(mCameraImagePath, options)
                        }
                        baos = ByteArrayOutputStream()
                        bitmap?.compress(Bitmap.CompressFormat.JPEG, 40, baos)
                        imageView?.setImageBitmap(bitmap)
                        try {
                            inputStream?.close()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        } finally {
                            try {
                                baos.close()
                            } catch (e: IOException) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        // 点击了取消
                        Toast.makeText(this, "Cancel", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "No SD card", Toast.LENGTH_SHORT).show()
                }
            }
            OPEN_ALBUM -> {
                val imageUri: Uri? = data?.data
                imageView?.setImageURI(imageUri)
            }
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (intentType == TAKE_PICTURE && perms.size == 3) {
            handle()
        }
        if (intentType == OPEN_ALBUM && perms.size == 2) {
            handle()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
    }
}