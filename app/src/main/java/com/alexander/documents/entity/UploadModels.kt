package com.alexander.documents.entity

class VKSaveInfo(val id: Int,
                 val ownerId: Int) {
    fun getAttachment() = "photo${ownerId}_$id"
}

class VKFileUploadInfo(val server: String, val photo: String, val hash: String)

class VKServerUploadInfo(val uploadUrl: String)