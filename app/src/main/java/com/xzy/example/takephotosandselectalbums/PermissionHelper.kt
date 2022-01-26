

package com.xzy.example.takephotosandselectalbums

import android.Manifest
import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import pub.devrel.easypermissions.EasyPermissions

object PermissionHelper {

    private const val REQUEST_CAMERA_PERMISSION_CODE = 0x0000001
    private const val REQUEST_STORAGE_PERMISSION_CODE = 0x0000002

    fun checkStoragePermission(context: Context): Boolean {
        return EasyPermissions.hasPermissions(
            context,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    fun checkCameraPermission(context: Context): Boolean {
        return EasyPermissions.hasPermissions(
            context,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    fun requestCameraPermission(activity: Activity) {
        EasyPermissions.requestPermissions(
            activity,
            "需请求您的相机权限，以便为您提供扫码登录、图片识别等功能",
            REQUEST_CAMERA_PERMISSION_CODE,
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }

    fun requestStoragePermission(activity: Activity) {
        EasyPermissions.requestPermissions(
            activity,
            "需请求您的相机权限，以便为您提供扫码登录、图片识别等功能",
            REQUEST_STORAGE_PERMISSION_CODE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
    }
}
