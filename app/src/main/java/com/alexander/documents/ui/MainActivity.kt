package com.alexander.documents.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.vk.api.sdk.VK
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import kotlinx.android.synthetic.main.activity_main.*
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alexander.documents.PathUtils
import com.alexander.documents.R
import com.alexander.documents.api.VKWallPostCommand
import com.vk.api.sdk.VKApiCallback

/**
 * author alex
 */
class MainActivity : AppCompatActivity(), ShareContentBottomSheetDialogFragment.ShareClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            containerView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        if (!VK.isLoggedIn()) {
            VK.login(this, arrayListOf(VKScope.PHOTOS, VKScope.WALL))
        } else {
            selectPhotoButton.setOnClickListener { askPermissionReadExternalStorage() }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                selectPhotoButton.setOnClickListener { askPermissionReadExternalStorage() }
            }

            override fun onLoginFailed(errorCode: Int) {
                showAuthError()
            }
        }

        if (data == null || !VK.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data?.data != null) {
            showBottomSheetDialog(imageUri = data.data!!)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        if (
            requestCode == PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            selectPhoto()
        }
    }

    override fun onShareClick(photoUri: Uri, message: String?) {
        val userId = VKAccessToken.restore(getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE))?.userId
        if (userId == null) {
            showError()
            return
        }
        val photo  = Uri.parse(PathUtils.getPath(this, photoUri))
        VK.execute(VKWallPostCommand(
            message,
            photo
        ), object: VKApiCallback<Int> {
            override fun success(result: Int) {
                showSuccess()
            }

            override fun fail(error: Exception) {
                showError()
            }
        })
    }

    private fun askPermissionReadExternalStorage() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
            )
        } else {
            selectPhoto()
        }
    }

    private fun showBottomSheetDialog(imageUri: Uri) {
        val bottomSheetDialog = ShareContentBottomSheetDialogFragment.newInstance(imageUri)
        bottomSheetDialog.show(supportFragmentManager, null)
    }

    private fun selectPhoto() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        val mimeTypes = arrayOf("image/jpeg", "image/png")
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes)
        startActivityForResult(intent, GALLERY_REQUEST_CODE)
    }

    private fun showError() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.error))
            .setMessage(getString(R.string.error_message))
            .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showSuccess() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.success))
            .setMessage(getString(R.string.success_message))
            .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showAuthError() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.error))
            .setMessage(getString(R.string.error_auth_message))
            .setPositiveButton(R.string.ok) { _, _ -> VK.login(this, arrayListOf(VKScope.PHOTOS, VKScope.WALL)) }
            .setNegativeButton(R.string.cancel) { _, _ -> finish() }
            .show()
    }

    companion object {
        private const val PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1
        private const val PREFERENCE_NAME = "com.vkontakte.android_pref_name"
        private const val GALLERY_REQUEST_CODE = 0

        fun createIntent(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
        }
    }
}
