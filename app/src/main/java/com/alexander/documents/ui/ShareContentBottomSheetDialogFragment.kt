package com.alexander.documents.ui

import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import com.alexander.documents.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.bottom_sheet_layout.*

/**
 * author alex
 */
class ShareContentBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private val photoUri: Uri by lazy(LazyThreadSafetyMode.NONE) {
        requireArguments()[ARG_IMAGE_URI] as Uri
    }

    private var shareClickListener: ShareClickListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        shareClickListener = context as ShareClickListener
    }

    override fun getTheme(): Int {
        return R.style.BottomSheetDialog
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setOnShowListener {
            Handler().post {
                val bottomSheet =
                    (dialog as? BottomSheetDialog)?.findViewById<View>(R.id.design_bottom_sheet) as? FrameLayout
                bottomSheet?.let {
                    BottomSheetBehavior.from(it).state = BottomSheetBehavior.STATE_EXPANDED
                }
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Glide.with(view)
            .load(photoUri)
            .apply(
                RequestOptions().transforms(
                    CenterCrop(),
                    RoundedCorners(8)
                )
            )
            .into(photoView)
        dismissView.setOnClickListener { dismiss() }
        sendButton.setOnClickListener { shareClickListener?.onShareClick(photoUri, commentView.text.toString()) }
        commentView.setOnClickListener { dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE) }
    }

    override fun onDetach() {
        super.onDetach()
        shareClickListener = null
    }

    companion object {
        private const val ARG_IMAGE_URI = "arg_image_uri"

        fun newInstance(imageUri: Uri) = ShareContentBottomSheetDialogFragment()
            .apply {
                val args = Bundle()
                args.putParcelable(ARG_IMAGE_URI, imageUri)
                arguments = args
            }
    }

    interface ShareClickListener {
        fun onShareClick(photoUri: Uri, message: String?)
    }
}