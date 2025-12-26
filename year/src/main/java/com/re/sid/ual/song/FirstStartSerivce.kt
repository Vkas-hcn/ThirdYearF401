package com.re.sid.ual.song

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import bef.ice.TelMe
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import com.re.sid.ual.ben.IconBean
import com.re.sid.ual.frist.DataTool

object FirstStartSerivce {


    fun firebaseShowFun() {
        if (DataTool.have_notifa) {
            return
        }
        try {
            Firebase.messaging.subscribeToTopic(IconBean.notificationValue)
                .addOnSuccessListener {
                    DataTool.have_notifa = true
                }
                .addOnFailureListener {
                }
        } catch (e: Exception) {
        }
    }
}