/*
 * Copyright (c) 2020 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.douya.api.info

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class React(
    @Json(name = "reaction_type")
    val reactionType: String = "",
    val text: String = "",
    val time: String = "",
    val user: UserAbstract? = null
) {
    companion object {
        const val TYPE_CANCEL_VOTE = "0"
        const val TYPE_VOTE = "1"
    }
}
