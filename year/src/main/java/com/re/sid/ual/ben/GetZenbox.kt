package com.re.sid.ual.ben

import android.app.Activity

object GetZenbox {
    var soCanFell = false


    val activityStack = mutableListOf<Activity>()

    fun getActivityList(): List<Activity> {
        return activityStack
    }

    var upUrl =  "https://test-luncheon.spacemanzenbox.com/balkan/cypriot"
    var getUrl =  "https://bnb.spacemanzenbox.com/apitest/cypriot/"
    var upvaule =  "5MiZBZBjzzChyhaowfLpyR"
    var pangKey =  "8580262"
}