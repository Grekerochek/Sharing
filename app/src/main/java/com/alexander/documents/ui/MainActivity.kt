package com.alexander.documents.ui

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.auth.VKAccessToken
import com.vk.api.sdk.auth.VKAuthCallback
import com.vk.api.sdk.auth.VKScope
import kotlinx.android.synthetic.main.activity_main.*
import android.net.Uri
import android.text.InputType
import android.widget.EditText
import android.widget.FrameLayout
import com.alexander.documents.R
import com.alexander.documents.api.DocumentsDelete
import com.alexander.documents.api.DocumentsEdit
import com.alexander.documents.entity.Document
import com.google.android.material.bottomsheet.BottomSheetDialog

/**
 * author alex
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            containerView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }

        if (!VK.isLoggedIn()) {
            VK.login(this, arrayListOf(VKScope.WALL))
        } else {
            selectPhotoButton.setOnClickListener { selectPhoto() }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val callback = object : VKAuthCallback {
            override fun onLogin(token: VKAccessToken) {
                selectPhotoButton.setOnClickListener { selectPhoto() }
            }

            override fun onLoginFailed(errorCode: Int) {
                showAuthError()
            }
        }
        if (data == null || !VK.onActivityResult(requestCode, resultCode, data, callback)) {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun selectPhoto() {
        val bottomSheetDialog = BottomSheetDialog(this, R.style.BottomSheetDialog)
        val sheetView = layoutInflater.inflate(R.layout.bottom_sheet_layout, null)
        bottomSheetDialog.setContentView(sheetView)
        bottomSheetDialog.show()
    }

 /*   private fun showError() {
        containerView.isRefreshing = false
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.error))
            .setMessage(getString(R.string.error_message))
            .setPositiveButton(R.string.ok) { dialog, _ -> dialog.dismiss() }
            .show()
    } */

    private fun showAuthError() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.error))
            .setMessage(getString(R.string.error_auth_message))
            .setPositiveButton(R.string.ok) { _, _ -> VK.login(this, arrayListOf(VKScope.DOCS)) }
            .setNegativeButton(R.string.cancel) { _, _ -> finish() }
            .show()
    }
/*
    private fun openDocumentDetails(document: Document) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(document.url))
        startActivity(intent)
    }

    private fun onPopUpMenuClick(documentId: Int, action: Int, position: Int) {
        when (action) {
            RENAME_ACTION -> showRenameDialog(documentId, position)
            DELETE_ACTION -> deleteDocument(documentId, position)
        }
    }

    private fun showRenameDialog(documentId: Int, position: Int) {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT
        AlertDialog.Builder(this)
            .setTitle(R.string.insert_title)
            .setView(input)
            .setPositiveButton(R.string.ok) { _, _ ->
                renameDocument(documentId, input.text.toString(), position)
            }
            .setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
            .show()
        (input.layoutParams as FrameLayout.LayoutParams).marginStart = 50
        (input.layoutParams as FrameLayout.LayoutParams).marginEnd = 50
    }

    private fun renameDocument(documentId: Int, documentTitle: String, position: Int) {
        containerView.isRefreshing = true
        VK.execute(DocumentsEdit(documentId, documentTitle), object : VKApiCallback<Int> {
            override fun success(result: Int) {
                if (!isFinishing && result == 1) {
                    containerView.isRefreshing = false
                    documentsAdapter.renameDocument(documentTitle, position)
                }
            }

            override fun fail(error: Exception) {
                showError()
            }
        })
    }

    private fun deleteDocument(documentId: Int, position: Int) {
        containerView.isRefreshing = true
        val userId = VKAccessToken.restore(getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE))?.userId
        if (userId == null) {
            showError()
            return
        }
        VK.execute(DocumentsDelete(userId, documentId), object : VKApiCallback<Int> {
            override fun success(result: Int) {
                if (!isFinishing && result == 1) {
                    containerView.isRefreshing = false
                    documentsAdapter.deleteDocument(position)
                }
            }

            override fun fail(error: Exception) {
                showError()
            }
        })
    } */

    companion object {
        const val RENAME_ACTION = 0
        const val DELETE_ACTION = 1

        private const val PREFERENCE_NAME = "com.vkontakte.android_pref_name"

        fun createIntent(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            context.startActivity(intent)
        }
    }
}
