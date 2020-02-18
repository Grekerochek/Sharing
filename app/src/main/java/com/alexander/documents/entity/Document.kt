package com.alexander.documents.entity

import android.annotation.SuppressLint
import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

/**
 * author alex
 */
data class Document(
    val id: Int = 0,
    var title: String = "",
    val photo: Photo?,
    val size: String = "",
    val ext: String = "",
    val url: String = "",
    val date: Long = 0,
    val type: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readParcelable<Photo>(Photo::class.java.classLoader),
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(title)
        parcel.writeParcelable(photo, Parcelable.PARCELABLE_WRITE_RETURN_VALUE)
        parcel.writeString(size)
        parcel.writeString(ext)
        parcel.writeString(url)
        parcel.writeLong(date)
        parcel.writeString(type)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Document> {
        override fun createFromParcel(parcel: Parcel): Document {
            return Document(parcel)
        }

        override fun newArray(size: Int): Array<Document?> {
            return arrayOfNulls(size)
        }

        @SuppressLint("DefaultLocale")
        fun parse(json: JSONObject) = Document(
            id = json.optInt("id", 0),
            title = json.optString("title", ""),
            photo = if (json.optJSONObject("preview") != null) {
                Photo.parse(
                    json.getJSONObject("preview")
                        .getJSONObject("photo").getJSONArray("sizes").getJSONObject(0)
                )
            } else {
                null
            },
            size = sizeMatcher(
                json.optInt(
                    "size",
                    0
                )
            ),
            ext = json.optString("ext", "").toUpperCase(),
            url = json.optString("url", ""),
            date = json.optLong("date", 0),
            type = typeMatcher(
                json.optInt(
                    "type",
                    0
                )
            )
        )

        private fun typeMatcher(type: Int): String {
            return when (type) {
                1 -> "текстовый документ"
                2 -> "архив"
                3 -> "gif"
                4 -> "изображение"
                5 -> "аудио"
                6 -> "видео"
                7 -> "электронная книга"
                else -> ""
            }
        }

        private fun sizeMatcher(size: Int): String {
            return if (size / 1000 == 0) {
                "$size KB"
            } else {
                (size / 1000).toString() + " MB"
            }
        }
    }
}