package bef.four

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.re.sid.ual.frist.HandTool
import com.re.sid.ual.up.DaoMe

class FireBaseTool : FirebaseMessagingService() {

    override fun onCreate() {
        super.onCreate()
        HandTool.loopStart(this)

    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        DaoMe.upPoint(false, "message_get")
        HandTool.loopStart(this)
    }
}