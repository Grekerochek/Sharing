package com.alexander.documents.entity

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

/**
 * author alex
 */
data class Photo(
    val src: String = "",
    val width: Int = 0,
    val height: Int = 0) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt(),
        parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(src)
        parcel.writeInt(width)
        parcel.writeInt(height)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Photo> {
        override fun createFromParcel(parcel: Parcel): Photo {
            return Photo(parcel)
        }

        override fun newArray(size: Int): Array<Photo?> {
            return arrayOfNulls(size)
        }

        fun parse(json: JSONObject) = Photo(
            src = json.optString("src", ""),
            width = json.optInt("width", 0),
            height = json.optInt("height", 0)
        )
    }
}