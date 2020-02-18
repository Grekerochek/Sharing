package com.alexander.documents.api

import com.alexander.documents.entity.Document
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONException
import org.json.JSONObject

/**
 * author alex
 */
class DocumentsRequest : ApiCommand<List<Document>>() {
    override fun onExecute(manager: VKApiManager): List<Document> {
        val call = VKMethodCall.Builder()
            .method("docs.get")
            .version(manager.config.version)
            .build()
        return manager.execute(call, ResponseApiParser())
    }
}

class DocumentsEdit(
    private val documentId: Int,
    private val documentTitle: String
) : ApiCommand<Int>() {
    override fun onExecute(manager: VKApiManager): Int {
        val call = VKMethodCall.Builder()
            .method("docs.edit")
            .args("doc_id", documentId)
            .args("title", documentTitle)
            .version(manager.config.version)
            .build()
        return manager.execute(call, ResponseApiParserEditOrDelete())
    }
}

class DocumentsDelete(
    private val userId: Int,
    private val documentId: Int
) : ApiCommand<Int>() {
    override fun onExecute(manager: VKApiManager): Int {
        val call = VKMethodCall.Builder()
            .method("docs.delete")
            .args("owner_id", userId)
            .args("doc_id", documentId)
            .version(manager.config.version)
            .build()
        return manager.execute(call, ResponseApiParserEditOrDelete())
    }
}

private class ResponseApiParser : VKApiResponseParser<List<Document>> {
    override fun parse(response: String): List<Document> {
        try {
            val documentsResponse = JSONObject(response).getJSONObject("response")
            val items = documentsResponse.getJSONArray("items")
            val documents = ArrayList<Document>(items.length())
            for (i in 0 until items.length()) {
                val document = Document.parse(items.getJSONObject(i))
                documents.add(document)
            }
            return documents
        } catch (ex: JSONException) {
            throw VKApiIllegalResponseException(ex)
        }
    }
}

private class ResponseApiParserEditOrDelete : VKApiResponseParser<Int> {
    override fun parse(response: String): Int {
        try {
            return JSONObject(response).optInt("response")
        } catch (ex: JSONException) {
            throw VKApiIllegalResponseException(ex)
        }
    }
}