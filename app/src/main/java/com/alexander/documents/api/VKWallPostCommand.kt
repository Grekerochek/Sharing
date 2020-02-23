package com.alexander.documents.api

import android.net.Uri
import com.alexander.documents.entity.VKFileUploadInfo
import com.alexander.documents.entity.VKSaveInfo
import com.alexander.documents.entity.VKServerUploadInfo
import com.vk.api.sdk.*
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class VKWallPostCommand(
    private val message: String? = null,
    private val photo: Uri
) : ApiCommand<Int>() {
    override fun onExecute(manager: VKApiManager): Int {
        val callBuilder = VKMethodCall.Builder()
            .method("wall.post")
            .args("friends_only", 1)
            .args("from_group", 0)
            .version(manager.config.version)
        message?.let {
            callBuilder.args("message", it)
        }

        val uploadInfo = getServerUploadInfo(manager)
        val attachments = uploadPhoto(photo, uploadInfo, manager)

        callBuilder.args("attachments", attachments)

        return manager.execute(callBuilder.build(), ResponseApiParser())
    }

    private fun getServerUploadInfo(manager: VKApiManager): VKServerUploadInfo {
        val uploadInfoCall = VKMethodCall.Builder()
            .method("photos.getWallUploadServer")
            .version(manager.config.version)
            .build()
        return manager.execute(uploadInfoCall, ServerUploadInfoParser())
    }

    private fun uploadPhoto(
        uri: Uri,
        serverUploadInfo: VKServerUploadInfo,
        manager: VKApiManager
    ): String {

        /* val requestBody = MultipartBody.Builder()
             .setType(MultipartBody.FORM)
             .addPart(
                 Headers.of("Content-Disposition", "form-data; name=\"title\""),
                 RequestBody.create(null, "Square Logo")
             )
             .addPart(
                 Headers.of("Content-Disposition", "form-data; name=\"image\""),
                 RequestBody.create(MediaType.parse("image/jpeg"),
                     HttpMultipartEntry.File(uri) as File)
                 )
             .build()

         val request = Request.Builder()
             .header("Authorization", "Client-ID ...")
             .url(serverUploadInfo.uploadUrl)
             .post(requestBody)
             .build()

         val response = OkHttpClient().newCall(request).execute()*/


        val fileUploadCall = VKHttpPostCall.Builder()
            .url(serverUploadInfo.uploadUrl)
            .args("photo", uri)
            .timeout(TimeUnit.MINUTES.toMillis(5))
            .retryCount(RETRY_COUNT)
            .build()
        val fileUploadInfo = manager.execute(fileUploadCall, null, FileUploadInfoParser())

        val saveCall = VKMethodCall.Builder()
            .method("photos.saveWallPhoto")
            .args("server", fileUploadInfo.server)
            .args("photo", fileUploadInfo.photo)
            .args("hash", fileUploadInfo.hash)
            .version(manager.config.version)
            .build()

        val saveInfo = manager.execute(saveCall, SaveInfoParser())

        return saveInfo.getAttachment()
    }

    companion object {
        const val RETRY_COUNT = 3
    }

    private class ResponseApiParser : VKApiResponseParser<Int> {
        override fun parse(response: String): Int {
            try {
                return JSONObject(response).getJSONObject("response").getInt("post_id")
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            }
        }
    }

    private class ServerUploadInfoParser : VKApiResponseParser<VKServerUploadInfo> {
        override fun parse(response: String): VKServerUploadInfo {
            try {
                val joResponse = JSONObject(response).getJSONObject("response")
                return VKServerUploadInfo(
                    uploadUrl = joResponse.getString("upload_url")
                )
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            }
        }
    }

    private class FileUploadInfoParser : VKApiResponseParser<VKFileUploadInfo> {
        override fun parse(response: String): VKFileUploadInfo {
            try {
                val joResponse = JSONObject(response)
                return VKFileUploadInfo(
                    server = joResponse.getString("server"),
                    photo = joResponse.getString("photo"),
                    hash = joResponse.getString("hash")
                )
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            }
        }
    }

    private class SaveInfoParser : VKApiResponseParser<VKSaveInfo> {
        override fun parse(response: String): VKSaveInfo {
            try {
                val joResponse = JSONObject(response).getJSONArray("response").getJSONObject(0)
                return VKSaveInfo(
                    id = joResponse.getInt("id"),
                    ownerId = joResponse.getInt("owner_id")
                )
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            }
        }
    }
}